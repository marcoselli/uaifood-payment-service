package br.edu.uaifood.payment.repository.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentEnumsTest {
    @Test
    fun `should valueOf PaymentStatus and PaymentMethod`() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.valueOf("PENDING"))
        assertEquals(PaymentStatus.APPROVED, PaymentStatus.valueOf("APPROVED"))
        assertEquals(PaymentStatus.REJECTED, PaymentStatus.valueOf("REJECTED"))
        assertEquals(PaymentMethod.PIX, PaymentMethod.valueOf("PIX"))
        assertEquals(PaymentMethod.CREDIT_CARD, PaymentMethod.valueOf("CREDIT_CARD"))
    }
} 