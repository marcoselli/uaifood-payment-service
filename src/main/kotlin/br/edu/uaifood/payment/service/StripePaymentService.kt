package br.edu.uaifood.payment.service

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.repository.PaymentRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class StripePaymentService(
    private val paymentRepository: PaymentRepository,
    @Value("\${stripe.secret-key}")
    private val secretKey: String
) {
    fun createPaymentIntent(payment: Payment): Map<String, String> {
        // Simulate API delay
        Thread.sleep(500)

        val paymentId = "mock_stripe_${UUID.randomUUID()}"
        val clientSecret = "mock_secret_${UUID.randomUUID()}"

        // Update payment with mock payment ID
        val entity = paymentRepository.findById(payment.id)
            .orElseThrow { RuntimeException("Payment not found: ${payment.id}") }
        val updatedEntity = entity.copy(
            paymentId = paymentId,
            updatedAt = LocalDateTime.now()
        )
        paymentRepository.save(updatedEntity)

        return mapOf(
            "id" to paymentId,
            "clientSecret" to clientSecret,
            "status" to "requires_payment_method",
            "amount" to payment.amount.toString(),
            "currency" to "brl"
        )
    }

    fun handlePaymentNotification(paymentIntentId: String, status: String): Payment {
        val entity = paymentRepository.findByPaymentId(paymentIntentId)
            ?: throw RuntimeException("Payment not found with payment ID: $paymentIntentId")

        val paymentStatus = when (status.lowercase()) {
            "succeeded" -> PaymentStatus.APPROVED
            "canceled" -> PaymentStatus.REJECTED
            "requires_payment_method", "requires_confirmation", "requires_action" -> PaymentStatus.PENDING
            else -> throw RuntimeException("Invalid payment status: $status")
        }

        val updatedEntity = entity.copy(
            status = paymentStatus,
            updatedAt = LocalDateTime.now()
        )
        return paymentRepository.save(updatedEntity).toDomain()
    }

    fun cancelPayment(paymentIntentId: String): Payment {
        val entity = paymentRepository.findByPaymentId(paymentIntentId)
            ?: throw RuntimeException("Payment not found with payment ID: $paymentIntentId")

        val updatedEntity = entity.copy(
            status = PaymentStatus.REJECTED,
            updatedAt = LocalDateTime.now()
        )
        return paymentRepository.save(updatedEntity).toDomain()
    }

    fun simulatePaymentProcessing(paymentIntentId: String): Payment {
        val entity = paymentRepository.findByPaymentId(paymentIntentId)
            ?: throw RuntimeException("Payment not found with payment ID: $paymentIntentId")

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