package br.edu.uaifood.payment.bdd

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.service.PaymentService
import br.edu.uaifood.payment.service.paymentprocessor.PaymentProcessor
import br.edu.uaifood.payment.service.paymentprocessor.PaymentProcessorFactory
import br.edu.uaifood.payment.service.paymentprocessor.PaymentResult
import io.cucumber.java.en.Given
import io.cucumber.java.en.When
import io.cucumber.java.en.Then
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.*

@SpringBootTest
@CucumberContextConfiguration
@ActiveProfiles("test")
class PaymentProcessingSteps {

    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var paymentProcessorFactory: PaymentProcessorFactory

    private lateinit var payment: Payment
    private lateinit var paymentProcessor: PaymentProcessor
    private var paymentResult: PaymentResult? = null
    private var exception: Exception? = null

    @Given("a new payment of {bigdecimal} for order {string} using {string}")
    fun createNewPayment(amount: BigDecimal, orderId: String, method: String) {
        payment = Payment(
            id = UUID.randomUUID(),
            orderId = UUID.fromString(orderId),
            amount = amount,
            paymentMethod = PaymentMethod.valueOf(method.uppercase()),
            status = PaymentStatus.PENDING,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        paymentProcessor = paymentProcessorFactory.getProcessor(payment.paymentMethod)
    }

    @When("the payment is processed")
    fun processPayment() {
        try {
            val savedPayment = paymentService.createPayment(
                orderId = payment.orderId,
                amount = payment.amount,
                paymentMethod = payment.paymentMethod
            )
            paymentResult = paymentProcessor.processPayment(savedPayment.id, savedPayment.paymentId!!)
        } catch (e: Exception) {
            exception = e
        }
    }

    @Then("the payment should be {string}")
    fun verifyPaymentStatus(expectedStatus: String) {
        assertNotNull(paymentResult, "Payment result should not be null")
        assertEquals(
            PaymentStatus.valueOf(expectedStatus.uppercase()),
            paymentResult?.status,
            "Payment status should be $expectedStatus"
        )
    }

    @Then("the payment should fail with error {string}")
    fun verifyPaymentError(expectedError: String) {
        assertNotNull(exception, "Exception should not be null")
        assertEquals(expectedError, exception?.message, "Error message should match")
    }

    @Then("the payment should be saved in the database")
    fun verifyPaymentSaved() {
        val savedPayment = paymentService.getPaymentByPaymentId(payment.paymentId!!)
        assertNotNull(savedPayment, "Payment should be saved in database")
        assertEquals(payment.id, savedPayment?.id, "Saved payment ID should match")
        assertEquals(paymentResult?.status, savedPayment?.status, "Saved payment status should match")
    }
} 