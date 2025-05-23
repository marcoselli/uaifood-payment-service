package br.edu.uaifood.payment.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PaymentEventTypeTest {
    @Test
    fun `should have all PaymentEventType values`() {
        val values = PaymentEventType.values()
        assertTrue(values.isNotEmpty())
        assertNotNull(PaymentEventType.valueOf("PAYMENT_CREATED"))
    }
} 