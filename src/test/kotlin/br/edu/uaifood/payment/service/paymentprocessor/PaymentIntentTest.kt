package br.edu.uaifood.payment.service.paymentprocessor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus

class PaymentIntentTest {
    @Test
    fun `should instantiate PaymentIntent`() {
        val intent = PaymentIntent(
            id = "id",
            status = PaymentStatus.PENDING,
            amount = BigDecimal.TEN,
            clientSecret = "secret",
            paymentMethod = PaymentMethod.PIX
        )
        assertNotNull(intent)
        assertEquals(PaymentStatus.PENDING, intent.status)
        assertEquals(PaymentMethod.PIX, intent.paymentMethod)
    }
} 