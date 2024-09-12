package no.nav.pensjon.simulator.tech.trace

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
open class TraceConfiguration {

    @Bean
    open fun callIdGenerator() = CallIdGenerator { UUID.randomUUID().toString() }
}
