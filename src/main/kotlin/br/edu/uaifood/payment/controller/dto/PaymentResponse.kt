package br.edu.uaifood.payment.controller.dto

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PaymentResponse(
    val id: UUID,
    val orderId: UUID,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val paymentId: String?,
    val qrCode: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(payment: Payment): PaymentResponse {
            return PaymentResponse(
                id = payment.id,
                orderId = payment.orderId,
                amount = payment.amount,
                status = payment.status,
                paymentMethod = payment.paymentMethod,
                paymentId = payment.paymentId,
                qrCode = payment.qrCode,
                createdAt = payment.createdAt,
                updatedAt = payment.updatedAt
            )
        }
    }
} 