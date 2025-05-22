package br.edu.uaifood.payment.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class QrCodeService {
    fun generateQrCode(orderId: UUID, amount: Double): String {
        // TODO: Implement real QR code generation
        // For now, we'll just return a mock URL
        return "https://uaifood-payment.com/pay/$orderId?amount=$amount"
    }
} 