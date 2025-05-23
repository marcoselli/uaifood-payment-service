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
import br.edu.uaifood.payment.service.paymentprocessor.PaymentIntent
import br.edu.uaifood.payment.service.paymentprocessor.PaymentProcessor
import br.edu.uaifood.payment.service.paymentprocessor.PaymentProcessorFactory
import br.edu.uaifood.payment.service.paymentprocessor.PaymentResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import org.junit.jupiter.api.Assertions.*

class PaymentServiceTest {

    private lateinit var paymentRepository: PaymentRepository
    private lateinit var paymentEventRepository: PaymentEventRepository
    private lateinit var rabbitTemplate: RabbitTemplate
    private lateinit var paymentProcessorFactory: PaymentProcessorFactory
    private lateinit var paymentProcessor: PaymentProcessor
    private lateinit var paymentService: PaymentService

    @BeforeEach
    fun setup() {
        paymentRepository = mockk()
        paymentEventRepository = mockk()
        rabbitTemplate = mockk(relaxed = true)
        paymentProcessorFactory = mockk()
        paymentProcessor = mockk()
        paymentService = PaymentService(
            paymentRepository = paymentRepository,
            paymentEventRepository = paymentEventRepository,
            rabbitTemplate = rabbitTemplate,
            paymentProcessorFactory = paymentProcessorFactory
        )
        every { paymentProcessorFactory.getProcessor(any()) } returns paymentProcessor
    }

    @Test
    fun `should create payment successfully`() {
        // Given
        val orderId = UUID.randomUUID()
        val amount = BigDecimal("100.00")
        val paymentMethod = PaymentMethod.CREDIT_CARD
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = amount,
            status = PaymentStatus.PENDING,
            paymentMethod = paymentMethod,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val paymentEntity = PaymentEntity.from(payment)
        val paymentIntent = PaymentIntent(
            id = "mock_intent_123",
            status = PaymentStatus.PENDING,
            amount = amount,
            clientSecret = "mock_secret",
            paymentMethod = paymentMethod
        )

        every { paymentRepository.save(any()) } returns paymentEntity
        every { paymentProcessor.createPaymentIntent(any()) } returns paymentIntent

        // When
        val result = paymentService.createPayment(orderId, amount, paymentMethod)

        // Then
        assertNotNull(result)
        assertEquals(orderId, result.orderId)
        assertEquals(amount, result.amount)
        assertEquals(paymentMethod, result.paymentMethod)
        assertEquals(PaymentStatus.PENDING, result.status)
        verify { 
            paymentRepository.save(any())
            paymentProcessor.createPaymentIntent(any())
        }
    }

    @Test
    fun `should process payment successfully`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            paymentId = "mock_intent_123",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val paymentEntity = PaymentEntity.from(payment)
        val paymentResult = PaymentResult(
            status = PaymentStatus.APPROVED,
            metadata = mapOf("transaction_id" to "mock_transaction_123")
        )

        every { paymentRepository.findById(paymentId) } returns Optional.of(paymentEntity)
        every { paymentProcessor.processPayment(any(), any()) } returns paymentResult
        every { paymentRepository.save(any()) } returns paymentEntity.copy(status = PaymentStatus.APPROVED)

        // When
        val result = paymentService.processPayment(paymentId)

        // Then
        assertNotNull(result)
        assertEquals(PaymentStatus.APPROVED, result.status)
        verify { 
            paymentRepository.findById(paymentId)
            paymentProcessor.processPayment(any(), any())
            paymentRepository.save(any())
        }
    }

    @Test
    fun `should get all payments successfully`() {
        // Given
        val payments = listOf(
            Payment(
                id = UUID.randomUUID(),
                orderId = UUID.randomUUID(),
                amount = BigDecimal("100.00"),
                status = PaymentStatus.PENDING,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Payment(
                id = UUID.randomUUID(),
                orderId = UUID.randomUUID(),
                amount = BigDecimal("200.00"),
                status = PaymentStatus.APPROVED,
                paymentMethod = PaymentMethod.PIX,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        val entities = payments.map { PaymentEntity.from(it) }

        every { paymentRepository.findAll() } returns entities

        // When
        val result = paymentService.getAllPayments()

        // Then
        assertEquals(2, result.size)
        assertEquals(payments[0].id, result[0].id)
        assertEquals(payments[1].id, result[1].id)
        verify { paymentRepository.findAll() }
    }

    @Test
    fun `should get payments by status successfully`() {
        // Given
        val status = PaymentStatus.APPROVED
        val payments = listOf(
            Payment(
                id = UUID.randomUUID(),
                orderId = UUID.randomUUID(),
                amount = BigDecimal("100.00"),
                status = status,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        val entities = payments.map { PaymentEntity.from(it) }

        every { paymentRepository.findAllByStatus(status) } returns entities

        // When
        val result = paymentService.getPaymentsByStatus(status)

        // Then
        assertEquals(1, result.size)
        assertEquals(status, result[0].status)
        verify { paymentRepository.findAllByStatus(status) }
    }

    @Test
    fun `should get payment events successfully`() {
        // Given
        val paymentId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val events = listOf(
            PaymentEvent(
                id = UUID.randomUUID(),
                paymentId = paymentId,
                orderId = orderId,
                eventType = PaymentEventType.PAYMENT_CREATED,
                status = PaymentStatus.PENDING,
                amount = BigDecimal("100.00"),
                createdAt = LocalDateTime.now()
            ),
            PaymentEvent(
                id = UUID.randomUUID(),
                paymentId = paymentId,
                orderId = orderId,
                eventType = PaymentEventType.PAYMENT_APPROVED,
                status = PaymentStatus.APPROVED,
                amount = BigDecimal("100.00"),
                createdAt = LocalDateTime.now()
            )
        )

        every { paymentEventRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId) } returns events

        // When
        val result = paymentService.getPaymentEvents(paymentId)

        // Then
        assertEquals(2, result.size)
        assertEquals(PaymentEventType.PAYMENT_CREATED, result[0].eventType)
        assertEquals(PaymentEventType.PAYMENT_APPROVED, result[1].eventType)
        verify { paymentEventRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId) }
    }

    @Test
    fun `should get order payments successfully`() {
        // Given
        val orderId = UUID.randomUUID()
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = BigDecimal("100.00"),
            status = PaymentStatus.APPROVED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val entity = PaymentEntity.from(payment)

        every { paymentRepository.findByOrderId(orderId) } returns entity

        // When
        val result = paymentService.getOrderPayments(orderId)

        // Then
        assertEquals(1, result.size)
        assertEquals(orderId, result[0].orderId)
        verify { paymentRepository.findByOrderId(orderId) }
    }

    @Test
    fun `should return empty list when no order payments found`() {
        // Given
        val orderId = UUID.randomUUID()
        every { paymentRepository.findByOrderId(orderId) } returns null

        // When
        val result = paymentService.getOrderPayments(orderId)

        // Then
        assertTrue(result.isEmpty())
        verify { paymentRepository.findByOrderId(orderId) }
    }

    @Test
    fun `should check if payment exists for order`() {
        // Given
        val orderId = UUID.randomUUID()
        val statuses = listOf(PaymentStatus.APPROVED, PaymentStatus.PENDING)
        every { paymentRepository.existsByOrderIdAndStatusIn(orderId, statuses) } returns true

        // When
        val result = paymentService.existsPaymentForOrder(orderId, statuses)

        // Then
        assertTrue(result)
        verify { paymentRepository.existsByOrderIdAndStatusIn(orderId, statuses) }
    }

    @Test
    fun `should get payment status successfully`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            paymentId = "mock_intent_123",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val entity = PaymentEntity.from(payment)

        every { paymentRepository.findById(paymentId) } returns Optional.of(entity)
        every { paymentProcessor.getPaymentStatus(any()) } returns PaymentStatus.APPROVED

        // When
        val result = paymentService.getPaymentStatus(paymentId)

        // Then
        assertEquals(PaymentStatus.APPROVED, result)
        verify { 
            paymentRepository.findById(paymentId)
            paymentProcessor.getPaymentStatus(any())
        }
    }

    @Test
    fun `should throw PaymentNotFoundException when payment not found`() {
        // Given
        val paymentId = UUID.randomUUID()
        every { paymentRepository.findById(paymentId) } returns Optional.empty()

        // When/Then
        val exception = assertThrows<PaymentNotFoundException> {
            paymentService.getPayment(paymentId)
        }
        assertEquals("Payment not found: $paymentId", exception.message)
    }

    @Test
    fun `should throw PaymentNotFoundException when updating non-existent payment status`() {
        // Given
        val paymentId = UUID.randomUUID()
        every { paymentRepository.findById(paymentId) } returns Optional.empty()

        // When/Then
        val exception = assertThrows<PaymentNotFoundException> {
            paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED)
        }
        assertEquals("Payment not found: $paymentId", exception.message)
    }

    @Test
    fun `should throw PaymentProcessingException when payment processing fails`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            paymentId = "mock_intent_123",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val paymentEntity = PaymentEntity.from(payment)
        val paymentResult = PaymentResult(
            status = PaymentStatus.REJECTED,
            errorMessage = "Payment processing failed",
            metadata = mapOf("error_code" to "PROCESSING_ERROR")
        )

        every { paymentRepository.findById(paymentId) } returns Optional.of(paymentEntity)
        every { paymentProcessor.processPayment(any(), any()) } returns paymentResult
        every { paymentRepository.save(any()) } returns paymentEntity.copy(
            status = PaymentStatus.REJECTED,
            errorMessage = "Payment processing failed"
        )

        // When
        val result = paymentService.processPayment(paymentId)

        // Then
        assertEquals(PaymentStatus.REJECTED, result.status)
        assertEquals("Payment processing failed", result.errorMessage)
        verify { 
            paymentRepository.findById(paymentId)
            paymentProcessor.processPayment(any(), any())
            paymentRepository.save(any())
        }
    }

    @Test
    fun `should handle payment status transitions correctly`() {
        // Given
        val paymentId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = orderId,
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val entity = PaymentEntity.from(payment)
        every { paymentRepository.findById(paymentId) } returns Optional.of(entity)
        every { paymentRepository.save(any()) } answers { 
            val updatedEntity = firstArg<PaymentEntity>()
            updatedEntity
        }
        every { paymentEventRepository.save(any()) } returns PaymentEvent(
            id = UUID.randomUUID(),
            paymentId = paymentId,
            orderId = orderId,
            eventType = PaymentEventType.PAYMENT_APPROVED,
            status = PaymentStatus.APPROVED,
            amount = payment.amount,
            metadata = mapOf(
                "previousStatus" to PaymentStatus.PENDING.toString(),
                "updatedAt" to LocalDateTime.now().toString()
            ),
            createdAt = LocalDateTime.now()
        )

        // When
        val result = paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED)

        // Then
        assertEquals(PaymentStatus.APPROVED, result.status)
        verify(exactly = 1) { paymentRepository.findById(paymentId) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { paymentEventRepository.save(any()) }
    }

    @Test
    fun `should create payment events for status changes`() {
        // Given
        val paymentId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        var currentEntity = PaymentEntity.from(
            Payment(
                id = paymentId,
                orderId = orderId,
                amount = BigDecimal("100.00"),
                status = PaymentStatus.PENDING,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        // Define as transições de status válidas em sequência
        val statusTransitions = listOf(
            PaymentStatus.PENDING to PaymentStatus.PROCESSING,
            PaymentStatus.PROCESSING to PaymentStatus.APPROVED,
            PaymentStatus.APPROVED to PaymentStatus.REFUNDED
        )

        for ((fromStatus, toStatus) in statusTransitions) {
            // Atualiza o status atual da entidade
            currentEntity = currentEntity.copy(status = fromStatus)
            
            val eventType = when (toStatus) {
                PaymentStatus.APPROVED -> PaymentEventType.PAYMENT_APPROVED
                PaymentStatus.REJECTED -> PaymentEventType.PAYMENT_REJECTED
                PaymentStatus.CANCELLED -> PaymentEventType.PAYMENT_CANCELLED
                PaymentStatus.REFUNDED -> PaymentEventType.PAYMENT_REFUNDED
                PaymentStatus.PROCESSING -> PaymentEventType.PAYMENT_PROCESSING
                else -> PaymentEventType.PAYMENT_PROCESSING
            }

            // Reconfigura os mocks para esta iteração
            paymentRepository = mockk()
            paymentEventRepository = mockk()
            paymentProcessor = mockk()
            paymentProcessorFactory = mockk()
            paymentService = PaymentService(
                paymentRepository = paymentRepository,
                paymentEventRepository = paymentEventRepository,
                rabbitTemplate = rabbitTemplate,
                paymentProcessorFactory = paymentProcessorFactory
            )

            every { paymentRepository.findById(paymentId) } returns Optional.of(currentEntity)
            every { paymentRepository.save(any()) } answers { 
                val updatedEntity = firstArg<PaymentEntity>()
                currentEntity = updatedEntity.copy(status = toStatus)
                currentEntity
            }
            every { paymentEventRepository.save(any()) } returns PaymentEvent(
                paymentId = paymentId,
                orderId = orderId,
                eventType = eventType,
                status = toStatus,
                amount = currentEntity.amount,
                metadata = mapOf(
                    "previousStatus" to fromStatus.toString(),
                    "updatedAt" to LocalDateTime.now().toString()
                ),
                createdAt = LocalDateTime.now()
            )

            // When
            val result = paymentService.updatePaymentStatus(paymentId, toStatus)

            // Then
            assertEquals(toStatus, result.status)
            verify { paymentRepository.findById(paymentId) }
            verify { paymentRepository.save(any()) }
            verify { paymentEventRepository.save(any()) }
        }
    }

    @Test
    fun `should check payment existence for order with multiple statuses`() {
        // Given
        val orderId = UUID.randomUUID()
        val statuses = listOf(PaymentStatus.APPROVED, PaymentStatus.PENDING)
        
        every { paymentRepository.existsByOrderIdAndStatusIn(orderId, statuses) } returns true

        // When
        val result = paymentService.existsPaymentForOrder(orderId, statuses)

        // Then
        assertTrue(result)
        verify { paymentRepository.existsByOrderIdAndStatusIn(orderId, statuses) }
    }

    @Test
    fun `should return empty list when no payments found for order`() {
        // Given
        val orderId = UUID.randomUUID()
        every { paymentRepository.findByOrderId(orderId) } returns null

        // When
        val result = paymentService.getOrderPayments(orderId)

        // Then
        assertTrue(result.isEmpty())
        verify { paymentRepository.findByOrderId(orderId) }
    }

    @Test
    fun `should get payment by payment id successfully`() {
        // Given
        val paymentId = "mock_payment_123"
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            paymentId = paymentId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val paymentEntity = PaymentEntity.from(payment)

        every { paymentRepository.findByPaymentId(paymentId) } returns paymentEntity

        // When
        val result = paymentService.getPaymentByPaymentId(paymentId)

        // Then
        assertNotNull(result)
        assertEquals(paymentId, result?.paymentId)
        verify { paymentRepository.findByPaymentId(paymentId) }
    }

    @Test
    fun `should return null when payment not found by payment id`() {
        // Given
        val paymentId = "non_existent_payment"
        every { paymentRepository.findByPaymentId(paymentId) } returns null

        // When
        val result = paymentService.getPaymentByPaymentId(paymentId)

        // Then
        assertNull(result)
        verify { paymentRepository.findByPaymentId(paymentId) }
    }

    @Test
    fun `should handle payment cancellation`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val entity = PaymentEntity.from(payment)
        every { paymentRepository.findById(paymentId) } returns Optional.of(entity)
        every { paymentRepository.save(any()) } answers { firstArg() }
        every { paymentEventRepository.save(any()) } returns PaymentEvent(
            id = UUID.randomUUID(),
            paymentId = paymentId,
            orderId = payment.orderId,
            eventType = PaymentEventType.PAYMENT_CANCELLED,
            status = PaymentStatus.CANCELLED,
            amount = payment.amount,
            metadata = mapOf(
                "previousStatus" to PaymentStatus.PENDING.toString(),
                "updatedAt" to LocalDateTime.now().toString()
            ),
            createdAt = LocalDateTime.now()
        )

        // When
        val result = paymentService.updatePaymentStatus(paymentId, PaymentStatus.CANCELLED)

        // Then
        assertEquals(PaymentStatus.CANCELLED, result.status)
        verify(exactly = 1) { paymentRepository.findById(paymentId) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { paymentEventRepository.save(any()) }
    }

    @Test
    fun `should handle payment refund`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.APPROVED, // Começa com APPROVED para permitir REFUND
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val entity = PaymentEntity.from(payment)
        every { paymentRepository.findById(paymentId) } returns Optional.of(entity)
        every { paymentRepository.save(any()) } answers { firstArg() }
        every { paymentEventRepository.save(any()) } returns PaymentEvent(
            id = UUID.randomUUID(),
            paymentId = paymentId,
            orderId = payment.orderId,
            eventType = PaymentEventType.PAYMENT_REFUNDED,
            status = PaymentStatus.REFUNDED,
            amount = payment.amount,
            metadata = mapOf(
                "previousStatus" to PaymentStatus.APPROVED.toString(),
                "updatedAt" to LocalDateTime.now().toString()
            ),
            createdAt = LocalDateTime.now()
        )

        // When
        val result = paymentService.updatePaymentStatus(paymentId, PaymentStatus.REFUNDED)

        // Then
        assertEquals(PaymentStatus.REFUNDED, result.status)
        verify(exactly = 1) { paymentRepository.findById(paymentId) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { paymentEventRepository.save(any()) }
    }

    @Test
    fun `should handle payment rejection`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PROCESSING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val entity = PaymentEntity.from(payment)
        every { paymentRepository.findById(paymentId) } returns Optional.of(entity)
        every { paymentRepository.save(any()) } answers { firstArg() }
        every { paymentEventRepository.save(any()) } returns PaymentEvent(
            id = UUID.randomUUID(),
            paymentId = paymentId,
            orderId = payment.orderId,
            eventType = PaymentEventType.PAYMENT_REJECTED,
            status = PaymentStatus.REJECTED,
            amount = payment.amount,
            metadata = mapOf(
                "previousStatus" to PaymentStatus.PROCESSING.toString(),
                "updatedAt" to LocalDateTime.now().toString()
            ),
            createdAt = LocalDateTime.now()
        )

        // When
        val result = paymentService.updatePaymentStatus(paymentId, PaymentStatus.REJECTED)

        // Then
        assertEquals(PaymentStatus.REJECTED, result.status)
        verify(exactly = 1) { paymentRepository.findById(paymentId) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { paymentEventRepository.save(any()) }
    }

    @Test
    fun `should handle payment processing`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val entity = PaymentEntity.from(payment)
        every { paymentRepository.findById(paymentId) } returns Optional.of(entity)
        every { paymentRepository.save(any()) } answers { firstArg() }
        every { paymentEventRepository.save(any()) } returns PaymentEvent(
            id = UUID.randomUUID(),
            paymentId = paymentId,
            orderId = payment.orderId,
            eventType = PaymentEventType.PAYMENT_PROCESSING,
            status = PaymentStatus.PROCESSING,
            amount = payment.amount,
            metadata = mapOf(
                "previousStatus" to PaymentStatus.PENDING.toString(),
                "updatedAt" to LocalDateTime.now().toString()
            ),
            createdAt = LocalDateTime.now()
        )

        // When
        val result = paymentService.updatePaymentStatus(paymentId, PaymentStatus.PROCESSING)

        // Then
        assertEquals(PaymentStatus.PROCESSING, result.status)
        verify(exactly = 1) { paymentRepository.findById(paymentId) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { paymentEventRepository.save(any()) }
    }

    @Test
    fun `should throw PaymentProcessingException when processor throws exception`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            paymentId = "mock_intent_123",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val paymentEntity = PaymentEntity.from(payment)

        every { paymentRepository.findById(paymentId) } returns Optional.of(paymentEntity)
        every { paymentProcessorFactory.getProcessor(payment.paymentMethod) } returns paymentProcessor
        every { paymentProcessor.processPayment(paymentId, payment.paymentId!!) } throws RuntimeException("Processor error")
        every { paymentEventRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId) } returns emptyList()

        // When/Then
        assertThrows<PaymentProcessingException> {
            paymentService.processPayment(paymentId)
        }.apply {
            assertEquals("Failed to process payment: Processor error", message)
        }
    }

    @Test
    fun `should throw IllegalStateException when updating payment to invalid status`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.APPROVED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val entity = PaymentEntity.from(payment)

        every { paymentRepository.findById(paymentId) } returns Optional.of(entity)
        every { paymentEventRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId) } returns emptyList()

        // When/Then
        assertThrows<IllegalStateException> {
            paymentService.updatePaymentStatus(paymentId, PaymentStatus.PENDING)
        }.apply {
            assertEquals("Cannot update payment status from APPROVED to PENDING", message)
        }
    }

    @Test
    fun `should throw IllegalStateException when updating payment in final state`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.REFUNDED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val entity = PaymentEntity.from(payment)

        every { paymentRepository.findById(paymentId) } returns Optional.of(entity)
        every { paymentEventRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId) } returns emptyList()

        // When/Then
        assertThrows<IllegalStateException> {
            paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED)
        }.apply {
            assertEquals("Cannot update payment in final state REFUNDED", message)
        }
    }

    @Test
    fun `should throw PaymentProcessingException when payment is in final state`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.REFUNDED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            paymentId = "mock_intent_123",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val paymentEntity = PaymentEntity.from(payment)

        every { paymentRepository.findById(paymentId) } returns Optional.of(paymentEntity)
        every { paymentEventRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId) } returns emptyList()

        // When/Then
        assertThrows<PaymentProcessingException> {
            paymentService.processPayment(paymentId)
        }.apply {
            assertEquals("Cannot process payment in final state REFUNDED", message)
        }
    }

    @Test
    fun `should throw PaymentProcessingException when payment is already processing`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PROCESSING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            paymentId = "mock_intent_123",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val paymentEntity = PaymentEntity.from(payment)

        every { paymentRepository.findById(paymentId) } returns Optional.of(paymentEntity)
        every { paymentEventRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId) } returns emptyList()

        // When/Then
        assertThrows<PaymentProcessingException> {
            paymentService.processPayment(paymentId)
        }.apply {
            assertEquals("Payment is already being processed", message)
        }
    }
} 