package no.nav.pensjon.simulator.testutil

import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.tech.web.WebClientBuilderConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@TestConfiguration
open class WebClientTestConfig {
    @Bean
    open fun webClientBase(): WebClientBase =
        WebClientBase(
            builder = WebClient.builder().also {
                WebClientBuilderConfiguration().customize(it)
            }
        )
}
