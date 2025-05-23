package br.edu.uaifood.payment.service.paymentprocessor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import br.edu.uaifood.payment.domain.PaymentStatus

class PaymentResultTest {
    @Test
    fun `should instantiate PaymentResult`() {
        val result = PaymentResult(
            status = PaymentStatus.APPROVED,
            errorMessage = null,
            metadata = mapOf("key" to "value")
        )
        assertNotNull(result)
        assertEquals(PaymentStatus.APPROVED, result.status)
        assertEquals("value", result.metadata["key"])
    }
} 