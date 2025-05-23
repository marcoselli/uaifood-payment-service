package br.edu.uaifood.payment.service.paymentprocessor

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.exception.PaymentProcessingException
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID
import java.lang.Math

@Component
class PaymentProcessorFactory {
    fun getProcessor(paymentMethod: PaymentMethod): PaymentProcessor {
        return MockPaymentProcessor()
    }
}

@Component
class MockPaymentProcessor : PaymentProcessor {
    private var shouldFail = false

    fun setShouldFail(value: Boolean) {
        shouldFail = value
    }

    override val paymentMethod: PaymentMethod
        get() = PaymentMethod.CREDIT_CARD // Default payment method, but we'll handle all methods

    override fun createPaymentIntent(payment: Payment): PaymentIntent {
        val paymentId = "mock_${payment.paymentMethod.name.lowercase()}_${UUID.randomUUID()}"
        val checkoutUrl = "https://mock-payment.com/checkout/$paymentId"
        
        return PaymentIntent(
            id = paymentId,
            status = PaymentStatus.PENDING,
            amount = payment.amount,
            currency = "BRL",
            clientSecret = checkoutUrl,
            paymentMethod = payment.paymentMethod,
            metadata = mapOf(
                "orderId" to payment.orderId.toString(),
                "paymentId" to payment.id.toString(),
                "isMock" to "true"
            )
        )
    }

    override fun processPayment(paymentId: UUID, paymentIntentId: String): PaymentResult {
        if (shouldFail) {
            throw PaymentProcessingException("Payment processing failed")
        }

        if (!paymentIntentId.startsWith("mock_")) {
            throw PaymentProcessingException("Payment intent not found")
        }

        // Simulate random success/failure (80% success rate)
        val isSuccess = Math.random() < 0.8
        val status = if (isSuccess) PaymentStatus.APPROVED else PaymentStatus.REJECTED
        
        return PaymentResult(
            status = status,
            errorMessage = if (!isSuccess) "Mock payment rejection" else null,
            metadata = mapOf(
                "paymentIntentId" to paymentIntentId,
                "isMock" to "true"
            )
        )
    }

    override fun getPaymentStatus(paymentIntentId: String): PaymentStatus {
        // Simulate random success/failure (80% success rate)
        val isSuccess = Math.random() < 0.8
        return if (isSuccess) PaymentStatus.APPROVED else PaymentStatus.REJECTED
    }
} 