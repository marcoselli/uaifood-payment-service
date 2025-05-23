package br.edu.uaifood.payment.repository.entity

import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class PaymentEntityTest {
    @Test
    fun `should instantiate PaymentEntity`() {
        val entity = PaymentEntity(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            amount = BigDecimal.TEN,
            status = PaymentStatus.APPROVED,
            paymentMethod = PaymentMethod.PIX,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            paymentId = "pid",
            errorMessage = null
        )
        assertNotNull(entity)
        assertEquals(PaymentStatus.APPROVED, entity.status)
    }

    @Test
    fun `should convert PaymentStatus enum`() {
        assertEquals(PaymentStatus.APPROVED, PaymentStatus.valueOf("APPROVED"))
        assertTrue(PaymentStatus.values().isNotEmpty())
    }

    @Test
    fun `should convert PaymentMethod enum`() {
        assertEquals(PaymentMethod.PIX, PaymentMethod.valueOf("PIX"))
        assertTrue(PaymentMethod.values().isNotEmpty())
    }
} 