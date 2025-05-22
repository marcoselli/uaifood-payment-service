package br.edu.uaifood.payment.controller

import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.service.PaymentService
import br.edu.uaifood.payment.service.StripePaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/webhooks/stripe")
class StripeWebhookController(
    private val paymentService: PaymentService,
    private val stripePaymentService: StripePaymentService
) {
    @PostMapping
    fun handleWebhook(
        @RequestParam("type") type: String,
        @RequestParam("id") id: String
    ): ResponseEntity<Map<String, String>> {
        return when (type.lowercase()) {
            "payment_intent.succeeded" -> {
                val payment = stripePaymentService.handlePaymentNotification(id, "succeeded")
                ResponseEntity.ok(mapOf(
                    "status" to "success",
                    "message" to "Payment processed successfully",
                    "paymentId" to payment.id.toString()
                ))
            }
            "payment_intent.payment_failed" -> {
                val payment = stripePaymentService.handlePaymentNotification(id, "canceled")
                ResponseEntity.ok(mapOf(
                    "status" to "success",
                    "message" to "Payment failed",
                    "paymentId" to payment.id.toString()
                ))
            }
            else -> ResponseEntity.ok(mapOf(
                "status" to "success",
                "message" to "Event ignored: $type"
            ))
        }
    }

    @GetMapping("/success")
    fun handleSuccess(
        @RequestParam("payment_intent") paymentIntentId: String,
        @RequestParam("order_id") orderId: String
    ): ResponseEntity<Map<String, String>> {

        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Payment processed successfully",
            "orderId" to orderId,
            "paymentId" to paymentIntentId
        ))
    }

    @GetMapping("/cancel")
    fun handleCancel(
        @RequestParam("payment_intent") paymentIntentId: String,
        @RequestParam("order_id") orderId: String
    ): ResponseEntity<Map<String, String>> {

        stripePaymentService.cancelPayment(paymentIntentId)

        return ResponseEntity.ok(mapOf(
            "status" to "cancelled",
            "message" to "Payment was cancelled",
            "orderId" to orderId,
            "paymentId" to paymentIntentId
        ))
    }
} 