package br.edu.uaifood.payment.service.paymentprocessor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import br.edu.uaifood.payment.domain.PaymentMethod

class PaymentProcessorFactoryTest {
    @Test
    fun `should return MockPaymentProcessor for PIX`() {
        val factory = PaymentProcessorFactory()
        val processor = factory.getProcessor(PaymentMethod.PIX)
        assertTrue(processor is MockPaymentProcessor)
    }
    @Test
    fun `should return MockPaymentProcessor for CREDIT_CARD`() {
        val factory = PaymentProcessorFactory()
        val processor = factory.getProcessor(PaymentMethod.CREDIT_CARD)
        assertTrue(processor is MockPaymentProcessor)
    }
} 