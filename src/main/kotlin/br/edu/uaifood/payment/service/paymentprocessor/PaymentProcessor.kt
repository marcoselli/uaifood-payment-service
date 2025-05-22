package br.edu.uaifood.payment.service.paymentprocessor

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import java.math.BigDecimal
import java.util.UUID

interface PaymentProcessor {
    val paymentMethod: PaymentMethod
    
    fun createPaymentIntent(payment: Payment): PaymentIntent
    fun processPayment(paymentId: UUID, paymentIntentId: String): PaymentResult
    fun getPaymentStatus(paymentIntentId: String): PaymentStatus
}

data class PaymentIntent(
    val id: String,
    val status: PaymentStatus,
    val amount: BigDecimal,
    val currency: String = "BRL",
    val clientSecret: String? = null,
    val paymentMethod: PaymentMethod,
    val metadata: Map<String, String> = emptyMap()
)

data class PaymentResult(
    val status: PaymentStatus,
    val errorMessage: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

enum class PaymentStatus {
    REQUIRES_PAYMENT_METHOD,
    REQUIRES_CONFIRMATION,
    REQUIRES_ACTION,
    PROCESSING,
    SUCCEEDED,
    CANCELED,
    FAILED
} 