package no.nav.pensjon.simulator.testutil

import no.nav.pensjon.simulator.tech.web.WebClientConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@TestConfiguration
open class WebClientTestConfig {

    @Bean
    open fun webClientBuilder(): WebClient.Builder =
        WebClient.builder().also { WebClientConfig().customize(it) }
}
