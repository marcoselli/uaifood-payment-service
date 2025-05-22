package br.edu.uaifood.payment.service

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.repository.PaymentRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class MercadoPagoService(
    private val paymentRepository: PaymentRepository,
    @Value("\${mercadopago.success-url}")
    private val successUrl: String,
    @Value("\${mercadopago.failure-url}")
    private val failureUrl: String,
    @Value("\${mercadopago.pending-url}")
    private val pendingUrl: String
) {
    fun createPaymentIntent(payment: Payment): Map<String, String> {
        // Simulate API delay
        Thread.sleep(500)

        val paymentId = "mock_mp_${UUID.randomUUID()}"
        val checkoutUrl = "https://mock-mercadopago.com/checkout/$paymentId"

        // Update payment with mock payment ID
        val entity = paymentRepository.findById(payment.id)
            .orElseThrow { RuntimeException("Payment not found: ${payment.id}") }
        val updatedEntity = entity.copy(
            paymentId = paymentId,
            qrCode = checkoutUrl,
            updatedAt = LocalDateTime.now()
        )
        paymentRepository.save(updatedEntity)

        return mapOf(
            "id" to paymentId,
            "checkoutUrl" to checkoutUrl
        )
    }

    fun handlePaymentNotification(paymentId: String, status: String): Payment {
        val entity = paymentRepository.findByPaymentId(paymentId)
            ?: throw RuntimeException("Payment not found with payment ID: $paymentId")

        val paymentStatus = when (status.lowercase()) {
            "approved" -> PaymentStatus.APPROVED
            "rejected" -> PaymentStatus.REJECTED
            "pending" -> PaymentStatus.PENDING
            else -> throw RuntimeException("Invalid payment status: $status")
        }

        val updatedEntity = entity.copy(
            status = paymentStatus,
            updatedAt = LocalDateTime.now()
        )
        return paymentRepository.save(updatedEntity).toDomain()
    }

    fun simulatePaymentProcessing(paymentId: String): Payment {
        val entity = paymentRepository.findByPaymentId(paymentId)
            ?: throw RuntimeException("Payment not found with payment ID: $paymentId")

        // Simulate random success/failure (80% success rate)
        val isSuccess = Math.random() < 0.8
        val status = if (isSuccess) PaymentStatus.APPROVED else PaymentStatus.REJECTED

        val updatedEntity = entity.copy(
            status = status,
            updatedAt = LocalDateTime.now(),
            errorMessage = if (!isSuccess) "Mock payment rejection" else null
        )
        return paymentRepository.save(updatedEntity).toDomain()
    }
} 