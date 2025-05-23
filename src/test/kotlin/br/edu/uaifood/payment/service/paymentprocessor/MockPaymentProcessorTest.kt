package br.edu.uaifood.payment.service.paymentprocessor

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.exception.PaymentProcessingException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.assertThrows

class MockPaymentProcessorTest {
    private lateinit var processor: MockPaymentProcessor
    private lateinit var payment: Payment

    @BeforeEach
    fun setup() {
        processor = MockPaymentProcessor()
        payment = Payment(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `should create payment intent successfully`() {
        val result = processor.createPaymentIntent(payment)
        assertNotNull(result)
        assertEquals(payment.amount, result.amount)
        assertEquals("BRL", result.currency)
        assertEquals(payment.paymentMethod, result.paymentMethod)
        assertNotNull(result.id)
        assertNotNull(result.clientSecret)
        assertTrue(result.metadata.isNotEmpty())
        assertTrue(result.metadata.containsKey("orderId") || result.metadata.containsKey("paymentId"))
    }

    @Test
    fun `should create payment intent with different payment methods`() {
        val methods = PaymentMethod.values()
        for (method in methods) {
            val paymentWithMethod = payment.copy(paymentMethod = method)
            val result = processor.createPaymentIntent(paymentWithMethod)
            assertEquals(method, result.paymentMethod)
            assertNotNull(result.id)
            assertNotNull(result.clientSecret)
        }
    }

    @Test
    fun `should process payment successfully`() {
        val paymentId = UUID.randomUUID()
        val paymentIntentId = "mock_intent_123"
        val result = processor.processPayment(paymentId, paymentIntentId)
        assertNotNull(result)
        assertTrue(result.status in listOf(PaymentStatus.APPROVED, PaymentStatus.REJECTED))
        if (result.status == PaymentStatus.REJECTED) {
            assertNotNull(result.errorMessage)
        }
        assertTrue(result.metadata.isNotEmpty())
    }

    @Test
    fun `should throw exception when payment intent not found`() {
        val paymentId = UUID.randomUUID()
        val nonExistentIntentId = "non_existent_intent"
        val exception = assertThrows<PaymentProcessingException> {
            processor.processPayment(paymentId, nonExistentIntentId)
        }
        assertTrue(exception.message?.contains("Payment intent not found") == true)
    }

    @Test
    fun `should throw exception when payment processing fails`() {
        val paymentId = UUID.randomUUID()
        val intentId = "mock_intent_123"
        processor.setShouldFail(true)
        val exception = assertThrows<PaymentProcessingException> {
            processor.processPayment(paymentId, intentId)
        }
        assertTrue(exception.message?.contains("Payment processing failed") == true)
        processor.setShouldFail(false)
    }

    @Test
    fun `should get payment status with random success rate`() {
        val paymentIntentId = "mock_intent_123"
        var successCount = 0
        val totalAttempts = 1000
        repeat(totalAttempts) {
            val status = processor.getPaymentStatus(paymentIntentId)
            if (status == PaymentStatus.APPROVED) {
                successCount++
            }
        }
        val successRate = successCount.toDouble() / totalAttempts
        assertTrue(successRate > 0.5, "Success rate should be above 50%")
        assertTrue(successRate < 0.95, "Success rate should be below 95%")
    }

    @Test
    fun `should have correct payment method`() {
        assertEquals(PaymentMethod.CREDIT_CARD, processor.paymentMethod)
    }
} 