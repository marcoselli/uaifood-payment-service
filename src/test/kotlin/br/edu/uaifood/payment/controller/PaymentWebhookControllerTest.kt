package br.edu.uaifood.payment.controller

import br.edu.uaifood.payment.config.PaymentServiceApplication
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.mockito.Mockito.`when`
import org.junit.jupiter.api.Assertions.*
import org.springframework.test.context.ContextConfiguration

@WebMvcTest(PaymentWebhookController::class)
@ContextConfiguration(classes = [PaymentServiceApplication::class])
class PaymentWebhookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var paymentService: PaymentService

    @Test
    fun `should process webhook successfully`() {
        // Given
        val paymentId = "mock_payment_123"
        val orderId = UUID.randomUUID()
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = BigDecimal("100.00"),
            status = PaymentStatus.APPROVED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(payment)
        `when`(paymentService.updatePaymentStatus(payment.id, PaymentStatus.APPROVED)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            get("/webhooks/payment/success")
                .param("payment_id", paymentId)
                .param("order_id", orderId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.paymentId").value(paymentId))
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
    }

    @Test
    fun `should return 404 when payment not found in webhook`() {
        // Given
        val paymentId = "non_existent_payment"
        val orderId = UUID.randomUUID()

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(null)

        // When/Then
        mockMvc.perform(
            get("/webhooks/payment/success")
                .param("payment_id", paymentId)
                .param("order_id", orderId.toString())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 400 when webhook data is invalid`() {
        // When/Then
        mockMvc.perform(
            get("/webhooks/payment/success")
                .param("payment_id", "") // Empty payment ID
                .param("order_id", "invalid-uuid") // Invalid UUID
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should handle payment failure webhook`() {
        // Given
        val paymentId = "mock_payment_123"
        val orderId = UUID.randomUUID()
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = BigDecimal("100.00"),
            status = PaymentStatus.REJECTED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(payment)
        `when`(paymentService.updatePaymentStatus(payment.id, PaymentStatus.REJECTED)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            get("/webhooks/payment/failure")
                .param("payment_id", paymentId)
                .param("order_id", orderId.toString())
                .param("error", "Payment failed")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("failure"))
            .andExpect(jsonPath("$.paymentId").value(paymentId))
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.error").value("Payment failed"))
    }

    @Test
    fun `should handle payment cancellation webhook`() {
        // Given
        val paymentId = "mock_payment_123"
        val orderId = UUID.randomUUID()
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = BigDecimal("100.00"),
            status = PaymentStatus.CANCELLED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(payment)
        `when`(paymentService.updatePaymentStatus(payment.id, PaymentStatus.CANCELLED)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            get("/webhooks/payment/cancel")
                .param("payment_id", paymentId)
                .param("order_id", orderId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("cancelled"))
            .andExpect(jsonPath("$.paymentId").value(paymentId))
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
    }

    @Test
    fun `should handle payment refund webhook`() {
        // Given
        val paymentId = "mock_payment_123"
        val orderId = UUID.randomUUID()
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = BigDecimal("100.00"),
            status = PaymentStatus.REFUNDED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(payment)
        `when`(paymentService.updatePaymentStatus(payment.id, PaymentStatus.REFUNDED)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            get("/webhooks/payment/refund")
                .param("payment_id", paymentId)
                .param("order_id", orderId.toString())
                .param("refund_amount", "100.00")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("refunded"))
            .andExpect(jsonPath("$.paymentId").value(paymentId))
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.refundAmount").value("100.00"))
    }

    @Test
    fun `should handle payment status update successfully`() {
        // Given
        val paymentId = "mock_payment_123"
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.APPROVED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(payment)
        `when`(paymentService.updatePaymentStatus(payment.id, PaymentStatus.APPROVED)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            post("/webhooks/payment/$paymentId/status")
                .param("status", "approved")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.paymentId").value(payment.id.toString()))
            .andExpect(jsonPath("$.newStatus").value(PaymentStatus.APPROVED.toString()))
    }

    @Test
    fun `should handle payment status update with succeeded status`() {
        // Given
        val paymentId = "mock_payment_123"
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.APPROVED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(payment)
        `when`(paymentService.updatePaymentStatus(payment.id, PaymentStatus.APPROVED)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            post("/webhooks/payment/$paymentId/status")
                .param("status", "succeeded")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.newStatus").value(PaymentStatus.APPROVED.toString()))
    }

    @Test
    fun `should handle payment status update with failed status`() {
        // Given
        val paymentId = "mock_payment_123"
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.REJECTED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(payment)
        `when`(paymentService.updatePaymentStatus(payment.id, PaymentStatus.REJECTED)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            post("/webhooks/payment/$paymentId/status")
                .param("status", "failed")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.newStatus").value(PaymentStatus.REJECTED.toString()))
    }

    @Test
    fun `should return 400 for invalid status in status update`() {
        // Given
        val paymentId = "mock_payment_123"
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            post("/webhooks/payment/$paymentId/status")
                .param("status", "invalid_status")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.message").value("Invalid status: invalid_status"))
    }

    @Test
    fun `should handle payment failure webhook with empty error message`() {
        // Given
        val paymentId = "mock_payment_123"
        val orderId = UUID.randomUUID()
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = BigDecimal("100.00"),
            status = PaymentStatus.REJECTED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(payment)
        `when`(paymentService.updatePaymentStatus(payment.id, PaymentStatus.REJECTED)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            get("/webhooks/payment/failure")
                .param("payment_id", paymentId)
                .param("order_id", orderId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("failure"))
            .andExpect(jsonPath("$.error").value("Payment failed"))
    }

    @Test
    fun `should handle payment refund webhook with invalid amount`() {
        // Given
        val paymentId = "mock_payment_123"
        val orderId = UUID.randomUUID()
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = orderId,
            amount = BigDecimal("100.00"),
            status = PaymentStatus.REFUNDED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(payment)
        `when`(paymentService.updatePaymentStatus(payment.id, PaymentStatus.REFUNDED)).thenReturn(payment)

        // When/Then
        mockMvc.perform(
            get("/webhooks/payment/refund")
                .param("payment_id", paymentId)
                .param("order_id", orderId.toString())
                .param("refund_amount", "invalid_amount")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("refunded"))
            .andExpect(jsonPath("$.refundAmount").value("invalid_amount"))
    }

    @Test
    fun `should return 400 for invalid payment_id in all webhooks`() {
        val endpoints = listOf("/success", "/failure", "/cancel", "/refund")
        val orderId = UUID.randomUUID()

        for (endpoint in endpoints) {
            mockMvc.perform(
                get("/webhooks/payment$endpoint")
                    .param("payment_id", "")
                    .param("order_id", orderId.toString())
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Invalid payment_id or order_id"))
        }
    }

    @Test
    fun `should return 400 for invalid order_id in all webhooks`() {
        val endpoints = listOf("/success", "/failure", "/cancel", "/refund")
        val paymentId = "mock_payment_123"

        for (endpoint in endpoints) {
            mockMvc.perform(
                get("/webhooks/payment$endpoint")
                    .param("payment_id", paymentId)
                    .param("order_id", "invalid-uuid")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Invalid payment_id or order_id"))
        }
    }

    @Test
    fun `should return 404 for non-existent payment in all webhooks`() {
        val endpoints = listOf("/success", "/failure", "/cancel", "/refund")
        val paymentId = "non_existent_payment"
        val orderId = UUID.randomUUID()

        `when`(paymentService.getPaymentByPaymentId(paymentId)).thenReturn(null)

        for (endpoint in endpoints) {
            mockMvc.perform(
                get("/webhooks/payment$endpoint")
                    .param("payment_id", paymentId)
                    .param("order_id", orderId.toString())
            )
                .andExpect(status().isNotFound)
        }
    }
} 