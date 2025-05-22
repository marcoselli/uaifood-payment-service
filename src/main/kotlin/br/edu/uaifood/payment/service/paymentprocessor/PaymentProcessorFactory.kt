package br.edu.uaifood.payment.service.paymentprocessor

import br.edu.uaifood.payment.domain.Payment
import br.edu.uaifood.payment.domain.PaymentMethod
import br.edu.uaifood.payment.domain.PaymentStatus
import br.edu.uaifood.payment.service.MercadoPagoService
import br.edu.uaifood.payment.service.StripePaymentService
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class PaymentProcessorFactory(
    private val mercadoPagoService: MercadoPagoService,
    private val stripePaymentService: StripePaymentService
) {
    fun getProcessor(paymentMethod: PaymentMethod): PaymentProcessor {
        return when (paymentMethod) {
            PaymentMethod.MERCADO_PAGO -> MercadoPagoProcessor(mercadoPagoService)
            PaymentMethod.STRIPE -> StripeProcessor(stripePaymentService)
            PaymentMethod.PIX -> throw UnsupportedOperationException("PIX payment method not implemented yet")
            PaymentMethod.CREDIT_CARD -> throw UnsupportedOperationException("Credit card payment method not implemented yet")
            PaymentMethod.DEBIT_CARD -> throw UnsupportedOperationException("Debit card payment method not implemented yet")
        }
    }
}

@Component
class MercadoPagoProcessor(
    private val mercadoPagoService: MercadoPagoService
) : PaymentProcessor {
    override val paymentMethod: PaymentMethod = PaymentMethod.MERCADO_PAGO

    override fun createPaymentIntent(payment: Payment): PaymentIntent {
        val result = mercadoPagoService.createPaymentIntent(payment)
        return PaymentIntent(
            id = result["id"]!!,
            status = PaymentStatus.PENDING,
            amount = payment.amount,
            currency = "BRL",
            clientSecret = result["checkoutUrl"]!!,
            paymentMethod = PaymentMethod.MERCADO_PAGO,
            metadata = mapOf(
                "orderId" to payment.orderId.toString(),
                "paymentId" to payment.id.toString()
            )
        )
    }

    override fun processPayment(paymentId: UUID, paymentIntentId: String): PaymentResult {
        val payment = mercadoPagoService.simulatePaymentProcessing(paymentIntentId)
        return PaymentResult(
            status = payment.status,
            errorMessage = payment.errorMessage,
            metadata = mapOf(
                "paymentIntentId" to paymentIntentId,
                "isMock" to "true"
            )
        )
    }

    override fun getPaymentStatus(paymentIntentId: String): PaymentStatus {
        val payment = mercadoPagoService.simulatePaymentProcessing(paymentIntentId)
        return payment.status
    }
}

@Component
class StripeProcessor(
    private val stripePaymentService: StripePaymentService
) : PaymentProcessor {
    override val paymentMethod: PaymentMethod = PaymentMethod.STRIPE

    override fun createPaymentIntent(payment: Payment): PaymentIntent {
        val result = stripePaymentService.createPaymentIntent(payment)
        return PaymentIntent(
            id = result["id"]!!,
            status = PaymentStatus.PENDING,
            amount = payment.amount,
            currency = "BRL",
            clientSecret = result["clientSecret"]!!,
            paymentMethod = PaymentMethod.STRIPE,
            metadata = mapOf(
                "orderId" to payment.orderId.toString(),
                "paymentId" to payment.id.toString()
            )
        )
    }

    override fun processPayment(paymentId: UUID, paymentIntentId: String): PaymentResult {
        val payment = stripePaymentService.simulatePaymentProcessing(paymentIntentId)
        return PaymentResult(
            status = payment.status,
            errorMessage = payment.errorMessage,
            metadata = mapOf(
                "paymentIntentId" to paymentIntentId,
                "isMock" to "true"
            )
        )
    }

    override fun getPaymentStatus(paymentIntentId: String): PaymentStatus {
        val payment = stripePaymentService.simulatePaymentProcessing(paymentIntentId)
        return payment.status
    }
} 