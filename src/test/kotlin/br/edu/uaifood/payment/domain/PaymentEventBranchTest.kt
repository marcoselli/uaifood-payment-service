package br.edu.uaifood.payment.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class PaymentEventBranchTest {
    @Test
    fun `should create PaymentEvent with all params`() {
        val event = PaymentEvent(
            eventType = PaymentEventType.PAYMENT_APPROVED,
            paymentId = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            status = PaymentStatus.APPROVED,
            amount = BigDecimal("200.00")
        )
        assertEquals(PaymentEventType.PAYMENT_APPROVED, event.eventType)
        assertEquals(PaymentStatus.APPROVED, event.status)
    }
} 