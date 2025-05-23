package br.edu.uaifood.payment.service

import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class QrCodeService {
    fun generateQrCode(paymentId: UUID, amount: BigDecimal): String {
        // TODO: Implement real QR code generation
        // For now, we'll just return a mock PIX QR code
        return "00020126" + // PIX Static QR Code header
               "0014BR.GOV.BCB.PIX" + // PIX identifier
               "01" + paymentId.toString().take(32) + // Merchant account information
               "520400005303986" + // Transaction amount
               "5802BR" + // Country code
               "5913UAIFOOD STORE" + // Merchant name
               "6008BRASILIA" + // Merchant city
               "62070503***" + // Additional data field
               "6304" + // CRC16
               "ABCD" // CRC16 value (mock)
    }
} 