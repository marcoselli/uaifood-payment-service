package br.edu.uaifood.payment.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payment_events")
data class PaymentEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val paymentId: UUID,

    @Column(nullable = false)
    val orderId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val eventType: PaymentEventType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus,

    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val metadata: Map<String, String> = emptyMap(),

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentEventType {
    PAYMENT_CREATED,
    PAYMENT_PROCESSING,
    PAYMENT_APPROVED,
    PAYMENT_REJECTED,
    PAYMENT_ERROR,
    PAYMENT_CANCELLED,
    PAYMENT_REFUNDED
} 