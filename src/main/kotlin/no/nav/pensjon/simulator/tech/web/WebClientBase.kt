package no.nav.pensjon.simulator.tech.web

import org.springframework.web.reactive.function.client.WebClient

class WebClientBase(
    private val builder: WebClient.Builder
) {
    fun withBaseUrl(url: String): WebClient =
        builder.clone().baseUrl(url).build()
}