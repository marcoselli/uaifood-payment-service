package br.edu.uaifood.payment.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class PaymentException(reason: String) : 
    ResponseStatusException(HttpStatus.BAD_REQUEST, reason) 