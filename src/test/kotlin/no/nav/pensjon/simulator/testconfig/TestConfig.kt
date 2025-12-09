package no.nav.pensjon.simulator.testconfig

import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations

@TestConfiguration
open class TestConfig {

    @Bean
    open fun namedParameterJdbcOperations(): NamedParameterJdbcOperations =
        mockk()
}


