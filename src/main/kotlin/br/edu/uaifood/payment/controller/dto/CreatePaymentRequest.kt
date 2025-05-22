package br.edu.uaifood.payment.controller.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.util.UUID

data class CreatePaymentRequest(
    @field:NotNull(message = "Order ID is required")
    val orderId: UUID,
    
    @field:NotNull(message = "Amount is required")
    @field:Positive(message = "Amount must be greater than zero")
    val amount: Double
) 