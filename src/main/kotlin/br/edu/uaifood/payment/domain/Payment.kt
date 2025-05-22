package br.edu.uaifood.payment.domain

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Payment(
    val id: UUID = UUID.randomUUID(),
    val orderId: UUID,
    val amount: BigDecimal,
    var status: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val paymentId: String? = null,
    val qrCode: String? = null,
    val errorMessage: String? = null,
    var processedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    APPROVED,
    REJECTED,
    CANCELLED,
    REFUNDED
}

enum class PaymentMethod {
    MERCADO_PAGO,
    STRIPE,
    PIX,
    CREDIT_CARD,
    DEBIT_CARD
} 