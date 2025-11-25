package no.nav.pensjon.simulator.testutil

import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.tech.web.WebClientBuilderConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

@TestConfiguration
open class WebClientTestConfig {

    @Bean
    open fun webClientBase(objectMapper: ObjectMapper): WebClientBase =
        WebClientBase(
            builder = WebClient.builder()
                .codecs {
                    it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(mapper()))
                    it.defaultCodecs().jacksonJsonEncoder(JacksonJsonEncoder(JsonMapper.builder()))
                }
                .also {
                    WebClientBuilderConfiguration().customize(it)
                }
        )

    @Bean
    @Primary
    open fun objectMapper(): ObjectMapper =
        JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build()

    private companion object {
        private fun mapper(): JsonMapper =
            JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build()
    }
}
