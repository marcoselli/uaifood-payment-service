package br.edu.uaifood.payment.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class PaymentEventTest {
    @Test
    fun `should create PaymentEvent`() {
        val event = PaymentEvent(
            eventType = PaymentEventType.PAYMENT_CREATED,
            paymentId = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            status = PaymentStatus.PENDING,
            amount = BigDecimal("100.00")
        )
        assertEquals(PaymentEventType.PAYMENT_CREATED, event.eventType)
    }
    @Test
    fun `should valueOf PaymentEventType`() {
        assertEquals(PaymentEventType.PAYMENT_CREATED, PaymentEventType.valueOf("PAYMENT_CREATED"))
    }
    @Test
    fun `should instantiate PaymentEvent`() {
        val event = PaymentEvent(
            id = UUID.randomUUID(),
            paymentId = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            eventType = PaymentEventType.PAYMENT_CREATED,
            status = PaymentStatus.PENDING,
            amount = BigDecimal.TEN,
            createdAt = LocalDateTime.now()
        )
        assertNotNull(event)
        assertEquals(PaymentEventType.PAYMENT_CREATED, event.eventType)
    }
} 