package br.edu.uaifood.payment.controller.dto

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

class PaymentResponseTest {
    private val mapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    @Test
    fun `should create PaymentResponse from Payment`() {
        val payment = Payment(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            amount = BigDecimal("123.45"),
            status = PaymentStatus.APPROVED,
            paymentMethod = PaymentMethod.PIX,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            paymentId = "pid123",
            qrCode = "qrcode"
        )
        val response = PaymentResponse.from(payment)
        assertEquals(payment.id, response.id)
        assertEquals(payment.orderId, response.orderId)
        assertEquals(payment.amount, response.amount)
        assertEquals(payment.status, response.status)
        assertEquals(payment.paymentMethod, response.paymentMethod)
        assertEquals(payment.paymentId, response.paymentId)
        assertEquals(payment.qrCode, response.qrCode)
        assertEquals(payment.createdAt, response.createdAt)
        assertEquals(payment.updatedAt, response.updatedAt)
    }

    @Test
    fun `should instantiate PaymentResponse`() {
        val response = PaymentResponse(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            amount = BigDecimal.TEN,
            status = PaymentStatus.APPROVED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            paymentId = "pid",
            qrCode = "qr",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        assertNotNull(response)
        assertEquals(PaymentStatus.APPROVED, response.status)
    }

    @Test
    fun `should serialize and deserialize PaymentResponse`() {
        val response = PaymentResponse(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            amount = BigDecimal.TEN,
            status = PaymentStatus.APPROVED,
            paymentMethod = PaymentMethod.PIX,
            paymentId = "pid",
            qrCode = "qr",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val json = mapper.writeValueAsString(response)
        val deserialized = mapper.readValue(json, PaymentResponse::class.java)
        assertEquals(response.status, deserialized.status)
        assertEquals(response.amount, deserialized.amount)
        assertEquals(response.id, deserialized.id)
        assertEquals(response.orderId, deserialized.orderId)
        assertEquals(response.paymentId, deserialized.paymentId)
        assertEquals(response.qrCode, deserialized.qrCode)
        assertEquals(response.createdAt, deserialized.createdAt)
        assertEquals(response.updatedAt, deserialized.updatedAt)
    }
} 