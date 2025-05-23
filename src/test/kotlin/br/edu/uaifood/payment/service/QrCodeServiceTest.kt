package br.edu.uaifood.payment.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.util.UUID

class QrCodeServiceTest {

    private val qrCodeService = QrCodeService()

    @Test
    fun `should generate QR code for PIX payment`() {
        // Given
        val paymentId = UUID.randomUUID()
        val amount = BigDecimal("100.00")

        // When
        val qrCode = qrCodeService.generateQrCode(paymentId, amount)

        // Then
        assertNotNull(qrCode)
        assertTrue(qrCode.isNotEmpty())
        assertTrue(qrCode.startsWith("00020126"))
    }

    @Test
    fun `should generate QR code with correct format`() {
        // Given
        val paymentId = UUID.randomUUID()
        val amount = BigDecimal("100.00")

        // When
        val qrCode = qrCodeService.generateQrCode(paymentId, amount)

        // Then
        assertTrue(qrCode.contains("BR.GOV.BCB.PIX"))
        assertTrue(qrCode.contains("UAIFOOD STORE"))
        assertTrue(qrCode.contains("BRASILIA"))
    }

    @Test
    fun `should instantiate QrCodeService`() {
        val service = QrCodeService()
        assertNotNull(service)
    }
} 