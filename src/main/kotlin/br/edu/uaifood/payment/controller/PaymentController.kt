package br.edu.uaifood.payment.controller

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentEvent
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment API", description = "API for managing payments")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping
    @Operation(summary = "Create a new payment")
    fun createPayment(
        @RequestParam orderId: UUID,
        @RequestParam amount: BigDecimal,
        @RequestParam paymentMethod: PaymentMethod
    ): ResponseEntity<Payment> {
        val payment = paymentService.createPayment(orderId, amount, paymentMethod)
        return ResponseEntity.ok(payment)
    }

    @PostMapping("/{paymentId}/process")
    @Operation(summary = "Process a payment")
    fun processPayment(@PathVariable paymentId: UUID): ResponseEntity<Payment> {
        val payment = paymentService.processPayment(paymentId)
        return ResponseEntity.ok(payment)
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    fun getPayment(@PathVariable paymentId: UUID): ResponseEntity<Payment> {
        val payment = paymentService.getPayment(paymentId)
        return ResponseEntity.ok(payment)
    }

    @GetMapping("/{paymentId}/events")
    @Operation(summary = "Get payment events")
    fun getPaymentEvents(@PathVariable paymentId: UUID): ResponseEntity<List<PaymentEvent>> {
        val events = paymentService.getPaymentEvents(paymentId)
        return ResponseEntity.ok(events)
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    fun getPaymentByOrderId(@PathVariable orderId: UUID): ResponseEntity<Payment?> {
        val payment = paymentService.getPaymentByOrderId(orderId)
        return ResponseEntity.ok(payment)
    }

    @GetMapping
    @Operation(summary = "Get all payments")
    fun getAllPayments(): ResponseEntity<List<Payment>> {
        val payments = paymentService.getAllPayments()
        return ResponseEntity.ok(payments)
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status")
    fun getPaymentsByStatus(@PathVariable status: PaymentStatus): ResponseEntity<List<Payment>> {
        val payments = paymentService.getPaymentsByStatus(status)
        return ResponseEntity.ok(payments)
    }

    @PutMapping("/{paymentId}/status")
    @Operation(summary = "Update payment status")
    fun updatePaymentStatus(
        @PathVariable paymentId: UUID,
        @RequestParam status: PaymentStatus
    ): ResponseEntity<Payment> {
        val payment = paymentService.updatePaymentStatus(paymentId, status)
        return ResponseEntity.ok(payment)
    }
} 