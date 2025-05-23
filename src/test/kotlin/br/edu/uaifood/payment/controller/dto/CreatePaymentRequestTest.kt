package br.edu.uaifood.payment.controller.dto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID

class CreatePaymentRequestTest {
    @Test
    fun `should instantiate CreatePaymentRequest`() {
        val req = CreatePaymentRequest(
            orderId = UUID.randomUUID(),
            amount = 10.0
        )
        assertNotNull(req)
        assertEquals(10.0, req.amount)
    }

    @Test
    fun `should serialize and deserialize CreatePaymentRequest`() {
        val mapper = jacksonObjectMapper()
        val req = CreatePaymentRequest(
            orderId = UUID.randomUUID(),
            amount = 20.5
        )
        val json = mapper.writeValueAsString(req)
        val deserialized = mapper.readValue(json, CreatePaymentRequest::class.java)
        assertEquals(req.amount, deserialized.amount)
        assertEquals(req.orderId, deserialized.orderId)
    }
} 