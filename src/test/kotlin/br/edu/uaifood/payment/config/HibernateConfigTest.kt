package br.edu.uaifood.payment.config

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class HibernateConfigTest {
    @Test
    fun `should instantiate HibernateConfig`() {
        val config = HibernateConfig()
        assertNotNull(config)
    }
} 