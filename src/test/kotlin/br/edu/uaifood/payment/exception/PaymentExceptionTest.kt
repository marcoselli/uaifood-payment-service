package br.edu.uaifood.payment.exception

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.http.HttpStatus

class PaymentExceptionTest {

    @Test
    fun `should create PaymentException with message`() {
        // Given
        val message = "Payment error occurred"

        // When
        val exception = PaymentException(message)

        // Then
        assertTrue(exception.message?.contains(message) == true)
    }

    @Test
    fun `should create PaymentNotFoundException with message`() {
        // Given
        val message = "Payment not found"

        // When
        val exception = PaymentNotFoundException(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun `should create PaymentProcessingException with message`() {
        // Given
        val message = "Payment processing failed"

        // When
        val exception = PaymentProcessingException(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun `should create PaymentProcessingException with message and cause`() {
        // Given
        val message = "Payment processing failed"
        val cause = RuntimeException("Original error")

        // When
        val exception = PaymentProcessingException(message, cause)

        // Then
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `should instantiate PaymentException`() {
        val message = "Payment error occurred"
        val ex = PaymentException(message)
        assertEquals(message, ex.reason)
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `should instantiate PaymentNotFoundException`() {
        val ex = PaymentNotFoundException("not found")
        assertEquals("not found", ex.message)
    }

    @Test
    fun `should instantiate PaymentProcessingException`() {
        val ex = PaymentProcessingException("fail")
        assertEquals("fail", ex.message)
    }
} 