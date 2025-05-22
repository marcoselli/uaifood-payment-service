package br.edu.uaifood.payment.repository

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.repository.entity.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PaymentRepository : JpaRepository<PaymentEntity, UUID> {
    fun findByPaymentId(paymentId: String): PaymentEntity?
    fun findByOrderId(orderId: UUID): PaymentEntity?
    fun findAllByStatus(status: PaymentStatus): List<PaymentEntity>
    fun existsByOrderIdAndStatusIn(orderId: UUID, statuses: List<PaymentStatus>): Boolean
} 