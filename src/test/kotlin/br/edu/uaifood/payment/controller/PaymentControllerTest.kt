package br.edu.uaifood.payment.controller

import br.edu.uaifood.payment.config.PaymentServiceApplication
import br.edu.uaifood.payment.controller.dto.CreatePaymentRequest
import br.edu.uaifood.payment.controller.dto.PaymentResponse
import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.service.PaymentService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.mockito.Mockito.`when`
import org.junit.jupiter.api.Assertions.*
import org.springframework.test.context.ContextConfiguration

@WebMvcTest(PaymentController::class)
@ContextConfiguration(classes = [PaymentServiceApplication::class])
class PaymentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var paymentService: PaymentService

    @Test
    fun `should create payment successfully`() {
        // Given
        val orderId = UUID.randomUUID()
        val amount = BigDecimal("100.00")
        val paymentMethod = PaymentMethod.CREDIT_CARD
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = amount,
            status = PaymentStatus.PENDING,
            paymentMethod = paymentMethod,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.createPayment(orderId, amount, paymentMethod)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            post("/api/v1/payments")
                .param("orderId", orderId.toString())
                .param("amount", amount.toString())
                .param("paymentMethod", paymentMethod.toString())
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(payment.id.toString()))
            .andExpect(jsonPath("$.orderId").value(payment.orderId.toString()))
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.status").value(payment.status.toString()))
            .andExpect(jsonPath("$.paymentMethod").value(payment.paymentMethod.toString()))
    }

    @Test
    fun `should get payment by id successfully`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPayment(paymentId)).thenReturn(payment)

        // When/Then
        mockMvc.perform(get("/api/v1/payments/$paymentId"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(payment.id.toString()))
            .andExpect(jsonPath("$.orderId").value(payment.orderId.toString()))
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.status").value(payment.status.toString()))
            .andExpect(jsonPath("$.paymentMethod").value(payment.paymentMethod.toString()))
    }

    @Test
    fun `should get payment by order id successfully`() {
        // Given
        val orderId = UUID.randomUUID()
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByOrderId(orderId)).thenReturn(payment)

        // When/Then
        mockMvc.perform(get("/api/v1/payments/order/$orderId"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(payment.id.toString()))
            .andExpect(jsonPath("$.orderId").value(payment.orderId.toString()))
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.status").value(payment.status.toString()))
            .andExpect(jsonPath("$.paymentMethod").value(payment.paymentMethod.toString()))
    }

    @Test
    fun `should get all payments successfully`() {
        // Given
        val payments = listOf(
            Payment(
                id = UUID.randomUUID(),
                orderId = UUID.randomUUID(),
                amount = BigDecimal("100.00"),
                status = PaymentStatus.PENDING,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Payment(
                id = UUID.randomUUID(),
                orderId = UUID.randomUUID(),
                amount = BigDecimal("200.00"),
                status = PaymentStatus.APPROVED,
                paymentMethod = PaymentMethod.PIX,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        `when`(paymentService.getAllPayments()).thenReturn(payments)

        // When/Then
        mockMvc.perform(get("/api/v1/payments"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(payments[0].id.toString()))
            .andExpect(jsonPath("$[0].orderId").value(payments[0].orderId.toString()))
            .andExpect(jsonPath("$[0].amount").value(100.00))
            .andExpect(jsonPath("$[0].status").value(payments[0].status.toString()))
            .andExpect(jsonPath("$[0].paymentMethod").value(payments[0].paymentMethod.toString()))
            .andExpect(jsonPath("$[1].id").value(payments[1].id.toString()))
            .andExpect(jsonPath("$[1].orderId").value(payments[1].orderId.toString()))
            .andExpect(jsonPath("$[1].amount").value(200.00))
            .andExpect(jsonPath("$[1].status").value(payments[1].status.toString()))
            .andExpect(jsonPath("$[1].paymentMethod").value(payments[1].paymentMethod.toString()))
    }

    @Test
    fun `should get payments by status successfully`() {
        // Given
        val status = PaymentStatus.PENDING
        val payments = listOf(
            Payment(
                id = UUID.randomUUID(),
                orderId = UUID.randomUUID(),
                amount = BigDecimal("100.00"),
                status = status,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        `when`(paymentService.getPaymentsByStatus(status)).thenReturn(payments)

        // When/Then
        mockMvc.perform(get("/api/v1/payments/status/$status"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(payments[0].id.toString()))
            .andExpect(jsonPath("$[0].orderId").value(payments[0].orderId.toString()))
            .andExpect(jsonPath("$[0].amount").value(100.00))
            .andExpect(jsonPath("$[0].status").value(payments[0].status.toString()))
            .andExpect(jsonPath("$[0].paymentMethod").value(payments[0].paymentMethod.toString()))
    }

    @Test
    fun `should process payment successfully`() {
        // Given
        val paymentId = UUID.randomUUID()
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.APPROVED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.processPayment(paymentId)).thenReturn(payment)

        // When/Then
        mockMvc.perform(post("/api/v1/payments/$paymentId/process"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(payment.id.toString()))
            .andExpect(jsonPath("$.orderId").value(payment.orderId.toString()))
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.status").value(payment.status.toString()))
            .andExpect(jsonPath("$.paymentMethod").value(payment.paymentMethod.toString()))
    }

    @Test
    fun `should update payment status successfully`() {
        // Given
        val paymentId = UUID.randomUUID()
        val newStatus = PaymentStatus.APPROVED
        val payment = Payment(
            id = paymentId,
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = newStatus,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.updatePaymentStatus(paymentId, newStatus)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            put("/api/v1/payments/$paymentId/status")
                .param("status", newStatus.toString())
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(payment.id.toString()))
            .andExpect(jsonPath("$.orderId").value(payment.orderId.toString()))
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.status").value(payment.status.toString()))
            .andExpect(jsonPath("$.paymentMethod").value(payment.paymentMethod.toString()))
    }
} 