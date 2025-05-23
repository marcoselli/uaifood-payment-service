package br.edu.uaifood.payment.controller

import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/webhooks/payment")
class PaymentWebhookController(
    private val paymentService: PaymentService
) {
    @PostMapping("/{paymentId}/status")
    fun handlePaymentStatus(
        @PathVariable paymentId: String,
        @RequestParam status: String
    ): ResponseEntity<Map<String, Any>> {
        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()

        val newStatus = when (status.lowercase()) {
            "approved", "succeeded" -> PaymentStatus.APPROVED
            "failed" -> PaymentStatus.REJECTED
            else -> return ResponseEntity.badRequest()
                .body(mapOf(
                    "status" to "error",
                    "message" to "Invalid status: $status"
                ))
        }

        val updatedPayment = paymentService.updatePaymentStatus(payment.id, newStatus)
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "paymentId" to payment.id.toString(),
            "newStatus" to updatedPayment.status.toString()
        ))
    }

    @GetMapping("/success")
    fun handleSuccess(
        @RequestParam("payment_id") paymentId: String,
        @RequestParam("order_id") orderId: String
    ): ResponseEntity<Map<String, Any>> {
        if (paymentId.isBlank() || !isValidUUID(orderId)) {
            return ResponseEntity.badRequest()
                .body(mapOf(
                    "status" to "error",
                    "message" to "Invalid payment_id or order_id"
                ))
        }

        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()

        val updatedPayment = paymentService.updatePaymentStatus(payment.id, PaymentStatus.APPROVED)
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "paymentId" to paymentId,
            "orderId" to orderId,
            "paymentStatus" to updatedPayment.status.toString()
        ))
    }

    @GetMapping("/failure")
    fun handleFailure(
        @RequestParam("payment_id") paymentId: String,
        @RequestParam("order_id") orderId: String,
        @RequestParam("error", required = false) error: String?
    ): ResponseEntity<Map<String, Any>> {
        if (paymentId.isBlank() || !isValidUUID(orderId)) {
            return ResponseEntity.badRequest()
                .body(mapOf(
                    "status" to "error",
                    "message" to "Invalid payment_id or order_id"
                ))
        }

        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()

        val updatedPayment = paymentService.updatePaymentStatus(payment.id, PaymentStatus.REJECTED)
        return ResponseEntity.ok(mapOf(
            "status" to "failure",
            "paymentId" to paymentId,
            "orderId" to orderId,
            "error" to (error ?: "Payment failed"),
            "paymentStatus" to updatedPayment.status.toString()
        ))
    }

    @GetMapping("/cancel")
    fun handleCancel(
        @RequestParam("payment_id") paymentId: String,
        @RequestParam("order_id") orderId: String
    ): ResponseEntity<Map<String, Any>> {
        if (paymentId.isBlank() || !isValidUUID(orderId)) {
            return ResponseEntity.badRequest()
                .body(mapOf(
                    "status" to "error",
                    "message" to "Invalid payment_id or order_id"
                ))
        }

        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()

        val updatedPayment = paymentService.updatePaymentStatus(payment.id, PaymentStatus.CANCELLED)
        return ResponseEntity.ok(mapOf(
            "status" to "cancelled",
            "paymentId" to paymentId,
            "orderId" to orderId,
            "paymentStatus" to updatedPayment.status.toString()
        ))
    }

    @GetMapping("/refund")
    fun handleRefund(
        @RequestParam("payment_id") paymentId: String,
        @RequestParam("order_id") orderId: String,
        @RequestParam("refund_amount", required = false) refundAmount: String?
    ): ResponseEntity<Map<String, Any>> {
        if (paymentId.isBlank() || !isValidUUID(orderId)) {
            return ResponseEntity.badRequest()
                .body(mapOf(
                    "status" to "error",
                    "message" to "Invalid payment_id or order_id"
                ))
        }

        val payment = paymentService.getPaymentByPaymentId(paymentId)
            ?: return ResponseEntity.notFound().build()

        val updatedPayment = paymentService.updatePaymentStatus(payment.id, PaymentStatus.REFUNDED)
        return ResponseEntity.ok(mapOf(
            "status" to "refunded",
            "paymentId" to paymentId,
            "orderId" to orderId,
            "refundAmount" to (refundAmount ?: payment.amount.toString()),
            "paymentStatus" to updatedPayment.status.toString()
        ))
    }

    private fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
} 