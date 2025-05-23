package br.edu.uaifood.payment.service

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentEvent
import br.edu.uaifood.payment.domain.PaymentEventType
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.exception.PaymentNotFoundException
import br.edu.uaifood.payment.exception.PaymentProcessingException
import br.edu.uaifood.payment.repository.PaymentEventRepository
import br.edu.uaifood.payment.repository.PaymentRepository
import br.edu.uaifood.payment.repository.entity.PaymentEntity
import br.edu.uaifood.payment.service.paymentprocessor.PaymentProcessor
import br.edu.uaifood.payment.service.paymentprocessor.PaymentProcessorFactory
import br.edu.uaifood.payment.service.paymentprocessor.PaymentStatus as ProcessorPaymentStatus
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentEventRepository: PaymentEventRepository,
    private val rabbitTemplate: RabbitTemplate,
    private val paymentProcessorFactory: PaymentProcessorFactory
) {
    companion object {
        const val PAYMENT_EXCHANGE = "payment.exchange"
        const val PAYMENT_STATUS_ROUTING_KEY = "payment.status"
        const val ORDER_STATUS_ROUTING_KEY = "order.status"
    }

    @Transactional
    fun createPayment(orderId: UUID, amount: BigDecimal, paymentMethod: PaymentMethod): Payment {
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = amount,
            status = PaymentStatus.PENDING,
            paymentMethod = paymentMethod,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val entity = paymentRepository.save(PaymentEntity.from(payment))
        val processor = paymentProcessorFactory.getProcessor(paymentMethod)
        val paymentIntent = processor.createPaymentIntent(payment)

        val updatedEntity = entity.copy(
            paymentId = paymentIntent.id,
            qrCode = paymentIntent.clientSecret,
            updatedAt = LocalDateTime.now()
        )
        return paymentRepository.save(updatedEntity).toDomain()
    }

    @Transactional
    fun processPayment(paymentId: UUID): Payment {
        val entity = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentNotFoundException("Payment not found: $paymentId") }

        // Validate payment state
        if (entity.status.isFinal()) {
            throw PaymentProcessingException("Cannot process payment in final state ${entity.status}")
        }
        if (entity.status == PaymentStatus.PROCESSING) {
            throw PaymentProcessingException("Payment is already being processed")
        }

        try {
            val processor = paymentProcessorFactory.getProcessor(entity.paymentMethod)
            val result = processor.processPayment(paymentId, entity.paymentId!!)

            // Validate processor result
            if (result.status == PaymentStatus.PROCESSING) {
                throw PaymentProcessingException("Invalid payment status returned by processor")
            }

            val updatedEntity = entity.copy(
                status = result.status,
                errorMessage = result.errorMessage,
                updatedAt = LocalDateTime.now()
            )
            return paymentRepository.save(updatedEntity).toDomain()
        } catch (e: Exception) {
            throw PaymentProcessingException("Failed to process payment: ${e.message}", e)
        }
    }

    @Transactional(readOnly = true)
    fun getPayment(paymentId: UUID): Payment {
        val entity = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentNotFoundException("Payment not found: $paymentId") }
        return entity.toDomain()
    }

    @Transactional(readOnly = true)
    fun getPaymentByPaymentId(paymentId: String): Payment? {
        return paymentRepository.findByPaymentId(paymentId)?.toDomain()
    }

    @Transactional(readOnly = true)
    fun getPaymentByOrderId(orderId: UUID): Payment? {
        return paymentRepository.findByOrderId(orderId)?.toDomain()
    }

    @Transactional(readOnly = true)
    fun getAllPayments(): List<Payment> {
        return paymentRepository.findAll().map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    fun getPaymentsByStatus(status: PaymentStatus): List<Payment> {
        return paymentRepository.findAllByStatus(status).map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    fun existsPaymentForOrder(orderId: UUID, statuses: List<PaymentStatus>): Boolean {
        return paymentRepository.existsByOrderIdAndStatusIn(orderId, statuses)
    }

    @Transactional
    fun updatePaymentStatus(paymentId: UUID, status: PaymentStatus): Payment {
        val entity = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentNotFoundException("Payment not found: $paymentId") }

        // Validate status transition
        if (entity.status == PaymentStatus.APPROVED && status == PaymentStatus.PENDING) {
            throw IllegalStateException("Cannot update payment status from ${entity.status} to $status")
        }

        // Allow refund only for approved payments
        if (status == PaymentStatus.REFUNDED && entity.status != PaymentStatus.APPROVED) {
            throw IllegalStateException("Cannot refund payment in status ${entity.status}")
        }

        // Validate payment state for other transitions
        if (entity.status.isFinal() && status != PaymentStatus.REFUNDED) {
            throw IllegalStateException("Cannot update payment in final state ${entity.status}")
        }

        val updatedEntity = entity.copy(
            status = status,
            updatedAt = LocalDateTime.now()
        )
        val savedEntity = paymentRepository.save(updatedEntity)

        // Create payment event
        val eventType = when (status) {
            PaymentStatus.APPROVED -> PaymentEventType.PAYMENT_APPROVED
            PaymentStatus.REJECTED -> PaymentEventType.PAYMENT_REJECTED
            PaymentStatus.CANCELLED -> PaymentEventType.PAYMENT_CANCELLED
            PaymentStatus.REFUNDED -> PaymentEventType.PAYMENT_REFUNDED
            else -> PaymentEventType.PAYMENT_PROCESSING
        }

        val event = PaymentEvent(
            paymentId = savedEntity.id,
            orderId = savedEntity.orderId,
            eventType = eventType,
            status = status,
            amount = savedEntity.amount,
            metadata = mapOf(
                "previousStatus" to entity.status.toString(),
                "updatedAt" to savedEntity.updatedAt.toString()
            )
        )
        paymentEventRepository.save(event)

        return savedEntity.toDomain()
    }

    @Transactional(readOnly = true)
    fun getPaymentEvents(paymentId: UUID): List<PaymentEvent> {
        return paymentEventRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId)
    }

    @Transactional(readOnly = true)
    fun getOrderPayments(orderId: UUID): List<Payment> {
        return paymentRepository.findByOrderId(orderId)?.let { listOf(it.toDomain()) } ?: emptyList()
    }

    fun getPaymentStatus(paymentId: UUID): PaymentStatus {
        val entity = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentNotFoundException("Payment not found: $paymentId") }

        val processor = paymentProcessorFactory.getProcessor(entity.paymentMethod)
        return processor.getPaymentStatus(entity.paymentId!!)
    }
} 