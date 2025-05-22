package br.edu.uaifood.payment.controller

import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/webhooks/payment")
class PaymentWebhookController(
    private val paymentService: PaymentService
) {
    @PostMapping("/{paymentId}/status")
    fun handlePaymentStatus(
        @PathVariable paymentId: String,
        @RequestParam status: String
    ): ResponseEntity<Map<String, String>> {
        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()

        val paymentStatus = when (status.lowercase()) {
            "approved", "succeeded" -> PaymentStatus.APPROVED
            "rejected", "failed" -> PaymentStatus.REJECTED
            "pending" -> PaymentStatus.PENDING
            else -> return ResponseEntity.badRequest().body(mapOf(
                "status" to "error",
                "message" to "Invalid status: $status"
            ))
        }

        val updatedPayment = paymentService.updatePaymentStatus(payment.id, paymentStatus)
        
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Payment status updated successfully",
            "paymentId" to updatedPayment.id.toString(),
            "orderId" to updatedPayment.orderId.toString(),
            "newStatus" to updatedPayment.status.toString()
        ))
    }

    @GetMapping("/success")
    fun handleSuccess(
        @RequestParam("payment_id") paymentId: String,
        @RequestParam("order_id") orderId: String
    ): ResponseEntity<Map<String, String>> {
        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()



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
        @RequestParam("order_id") orderId: String
    ): ResponseEntity<Map<String, String>> {
        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()

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
        @RequestParam("order_id") orderId: String
    ): ResponseEntity<Map<String, String>> {
        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(mapOf(
            "status" to "pending",
            "message" to "Payment is pending",
            "orderId" to orderId,
            "paymentId" to paymentId
        ))
    }
} 