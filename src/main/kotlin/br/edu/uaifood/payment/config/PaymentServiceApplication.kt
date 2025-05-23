package br.edu.uaifood.payment.config

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan

@SpringBootApplication(scanBasePackages = ["br.edu.uaifood.payment"])
@EnableJpaRepositories(basePackages = ["br.edu.uaifood.payment.repository"])
@EntityScan(basePackages = ["br.edu.uaifood.payment.domain", "br.edu.uaifood.payment.repository.entity"])
@ConfigurationPropertiesScan
class PaymentServiceApplication

fun main(args: Array<String>) {
    runApplication<PaymentServiceApplication>(*args)
} 