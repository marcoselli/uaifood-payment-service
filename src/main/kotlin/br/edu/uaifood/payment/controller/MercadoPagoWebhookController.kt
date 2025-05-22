package br.edu.uaifood.payment.controller

import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.service.PaymentService
import br.edu.uaifood.payment.service.MercadoPagoService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/webhooks/mercadopago")
class MercadoPagoWebhookController(
    private val paymentService: PaymentService,
    private val mercadoPagoService: MercadoPagoService
) {
    @PostMapping
    fun handleWebhook(
        @RequestParam("topic") topic: String,
        @RequestParam("id") id: String
    ): ResponseEntity<Map<String, String>> {
        return when (topic.lowercase()) {
            "payment" -> {
                val payment = mercadoPagoService.handlePaymentNotification(id, "approved")
                ResponseEntity.ok(mapOf(
                    "status" to "success",
                    "message" to "Payment processed successfully",
                    "paymentId" to payment.id.toString()
                ))
            }
            "payment.rejected" -> {
                val payment = mercadoPagoService.handlePaymentNotification(id, "rejected")
                ResponseEntity.ok(mapOf(
                    "status" to "success",
                    "message" to "Payment rejected",
                    "paymentId" to payment.id.toString()
                ))
            }
            "payment.pending" -> {
                val payment = mercadoPagoService.handlePaymentNotification(id, "pending")
                ResponseEntity.ok(mapOf(
                    "status" to "success",
                    "message" to "Payment pending",
                    "paymentId" to payment.id.toString()
                ))
            }
            else -> ResponseEntity.badRequest().body(mapOf(
                "status" to "error",
                "message" to "Invalid topic: $topic"
            ))
        }
    }

    @GetMapping("/success")
    fun handleSuccess(
        @RequestParam("payment_id") paymentId: String,
        @RequestParam("external_reference") orderId: String
    ): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Payment processed successfully",
            "orderId" to orderId,
            "paymentId" to paymentId
        ))
    }

    @GetMapping("/failure")
    fun handleFailure(
        @RequestParam("payment_id") paymentId: String,
        @RequestParam("external_reference") orderId: String
    ): ResponseEntity<Map<String, String>> {
        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()

        paymentService.updatePaymentStatus(payment.id, PaymentStatus.REJECTED)

        return ResponseEntity.ok(mapOf(
            "status" to "failure",
            "message" to "Payment was rejected",
            "orderId" to orderId,
            "paymentId" to paymentId
        ))
    }

    @GetMapping("/pending")
    fun handlePending(
        @RequestParam("payment_id") paymentId: String,
        @RequestParam("external_reference") orderId: String
    ): ResponseEntity<Map<String, String>> {
        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()

        paymentService.updatePaymentStatus(payment.id, PaymentStatus.PENDING)

        return ResponseEntity.ok(mapOf(
            "status" to "pending",
            "message" to "Payment is pending",
            "orderId" to orderId,
            "paymentId" to paymentId
        ))
    }
} 