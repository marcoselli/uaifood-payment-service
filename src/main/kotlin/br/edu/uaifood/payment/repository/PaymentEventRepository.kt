package br.edu.uaifood.payment.repository

import br.edu.uaifood.payment.domain.PaymentEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PaymentEventRepository : JpaRepository<PaymentEvent, UUID> {
    fun findByPaymentIdOrderByCreatedAtDesc(paymentId: UUID): List<PaymentEvent>
    fun findByPaymentId(paymentId: UUID): List<PaymentEvent>
} 