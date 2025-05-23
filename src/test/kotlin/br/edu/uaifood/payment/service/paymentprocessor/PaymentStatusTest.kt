package br.edu.uaifood.payment.service.paymentprocessor

import br.edu.uaifood.payment.service.paymentprocessor.PaymentStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import br.edu.uaifood.payment.domain.PaymentStatus as DomainPaymentStatus

class PaymentStatusTest {
    @Test
    fun `should have all PaymentStatus values`() {
        val values = PaymentStatus.values()
        assertTrue(values.contains(PaymentStatus.PENDING))
        assertTrue(values.contains(PaymentStatus.APPROVED))
        assertTrue(values.contains(PaymentStatus.REJECTED))
        assertTrue(values.contains(PaymentStatus.CANCELLED))
        assertTrue(values.contains(PaymentStatus.REFUNDED))
    }

    @Test
    fun `should convert string to enum value`() {
        val status = PaymentStatus.valueOf("APPROVED")
        assertEquals(PaymentStatus.APPROVED, status)
    }

    @Test
    fun `should throw exception for invalid status`() {
        assertThrows(IllegalArgumentException::class.java) {
            PaymentStatus.valueOf("INVALID_STATUS")
        }
    }

    @Test
    fun `should convert domain status to processor status`() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.fromDomain(DomainPaymentStatus.PENDING))
        assertEquals(PaymentStatus.PROCESSING, PaymentStatus.fromDomain(DomainPaymentStatus.PROCESSING))
        assertEquals(PaymentStatus.APPROVED, PaymentStatus.fromDomain(DomainPaymentStatus.APPROVED))
        assertEquals(PaymentStatus.REJECTED, PaymentStatus.fromDomain(DomainPaymentStatus.REJECTED))
        assertEquals(PaymentStatus.CANCELLED, PaymentStatus.fromDomain(DomainPaymentStatus.CANCELLED))
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.fromDomain(DomainPaymentStatus.REFUNDED))
    }

    @Test
    fun `should convert processor status to domain status`() {
        assertEquals(DomainPaymentStatus.PENDING, PaymentStatus.PENDING.toDomain())
        assertEquals(DomainPaymentStatus.PROCESSING, PaymentStatus.PROCESSING.toDomain())
        assertEquals(DomainPaymentStatus.APPROVED, PaymentStatus.APPROVED.toDomain())
        assertEquals(DomainPaymentStatus.REJECTED, PaymentStatus.REJECTED.toDomain())
        assertEquals(DomainPaymentStatus.CANCELLED, PaymentStatus.CANCELLED.toDomain())
        assertEquals(DomainPaymentStatus.REFUNDED, PaymentStatus.REFUNDED.toDomain())
    }

    @Test
    fun `should check if status is final`() {
        assertFalse(PaymentStatus.PENDING.isFinal())
        assertFalse(PaymentStatus.PROCESSING.isFinal())
        assertTrue(PaymentStatus.APPROVED.isFinal())
        assertTrue(PaymentStatus.REJECTED.isFinal())
        assertTrue(PaymentStatus.CANCELLED.isFinal())
        assertTrue(PaymentStatus.REFUNDED.isFinal())
    }
} 