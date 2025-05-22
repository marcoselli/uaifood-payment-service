package br.edu.uaifood.payment.repository.entity

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payments")
data class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @Column(nullable = false)
    val orderId: UUID,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val paymentMethod: PaymentMethod,
    
    @Column
    var paymentId: String? = null,
    
    @Column
    var qrCode: String? = null,
    
    @Column
    var errorMessage: String? = null,
    
    @Column
    var externalPaymentId: String? = null,
    
    @Column
    var processedAt: LocalDateTime? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun from(payment: Payment): PaymentEntity {
            return PaymentEntity(
                id = payment.id,
                orderId = payment.orderId,
                amount = payment.amount,
                status = payment.status,
                paymentMethod = payment.paymentMethod,
                paymentId = payment.paymentId,
                qrCode = payment.qrCode,
                errorMessage = payment.errorMessage,
                externalPaymentId = payment.paymentId,
                processedAt = payment.processedAt,
                createdAt = payment.createdAt,
                updatedAt = payment.updatedAt
            )
        }
    }

    fun toDomain(): Payment {
        return Payment(
            id = id,
            orderId = orderId,
            amount = amount,
            status = status,
            paymentMethod = paymentMethod,
            paymentId = paymentId,
            qrCode = qrCode,
            errorMessage = errorMessage,
            processedAt = processedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

enum class PaymentStatus {
    PENDING,
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