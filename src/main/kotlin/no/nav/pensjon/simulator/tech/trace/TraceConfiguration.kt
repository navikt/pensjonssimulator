package no.nav.pensjon.simulator.tech.trace

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class TraceConfiguration {

    @Bean
    fun callIdGenerator() = CallIdGenerator { UUID.randomUUID().toString() }
}
