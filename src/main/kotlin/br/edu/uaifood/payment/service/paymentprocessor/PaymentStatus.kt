package br.edu.uaifood.payment.service.paymentprocessor

import br.edu.uaifood.payment.domain.PaymentStatus as DomainPaymentStatus

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    APPROVED,
    REJECTED,
    CANCELLED,
    REFUNDED;

    fun toDomain(): DomainPaymentStatus {
        return when (this) {
            PENDING -> DomainPaymentStatus.PENDING
            PROCESSING -> DomainPaymentStatus.PROCESSING
            APPROVED -> DomainPaymentStatus.APPROVED
            REJECTED -> DomainPaymentStatus.REJECTED
            CANCELLED -> DomainPaymentStatus.CANCELLED
            REFUNDED -> DomainPaymentStatus.REFUNDED
        }
    }

    companion object {
        fun fromDomain(status: DomainPaymentStatus): PaymentStatus {
            return when (status) {
                DomainPaymentStatus.PENDING -> PENDING
                DomainPaymentStatus.PROCESSING -> PROCESSING
                DomainPaymentStatus.APPROVED -> APPROVED
                DomainPaymentStatus.REJECTED -> REJECTED
                DomainPaymentStatus.CANCELLED -> CANCELLED
                DomainPaymentStatus.REFUNDED -> REFUNDED
            }
        }
    }

    fun isFinal(): Boolean {
        return when (this) {
            PENDING, PROCESSING -> false
            APPROVED, REJECTED, CANCELLED, REFUNDED -> true
        }
    }
} 