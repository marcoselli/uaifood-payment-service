package br.edu.uaifood.payment.service.paymentprocessor

import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.exception.PaymentProcessingException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class MockPaymentProcessorBranchTest {
    @Test
    fun `should process payment with rejection`() {
        val processor = MockPaymentProcessor()
        // Forçar rejeição: como é random, rodar até rejeitar
        var rejected = false
        repeat(20) {
            val result = processor.processPayment(UUID.randomUUID(), "mock_intent_123")
            if (result.status == PaymentStatus.REJECTED) {
                rejected = true
                assertNotNull(result.errorMessage)
                return
            }
        }
        assertTrue(rejected, "Deveria ter pelo menos uma rejeição em 20 tentativas")
    }
    @Test
    fun `should throw exception when shouldFail is true`() {
        val processor = MockPaymentProcessor()
        processor.setShouldFail(true)
        assertThrows(PaymentProcessingException::class.java) {
            processor.processPayment(UUID.randomUUID(), "mock_intent_123")
        }
    }
} 