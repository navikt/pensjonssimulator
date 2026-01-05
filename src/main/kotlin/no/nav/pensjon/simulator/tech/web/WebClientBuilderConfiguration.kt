package no.nav.pensjon.simulator.tech.web

import org.springframework.boot.webclient.WebClientCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Configuration
open class WebClientBuilderConfiguration : WebClientCustomizer {

    override fun customize(webClientBuilder: WebClient.Builder) {
        webClientBuilder
            .exchangeStrategies(largeBufferStrategies())
            .filter(filterResponse())
    }

    companion object {
        private const val MAX_IN_MEMORY_SIZE = 10485760 // 10 MB (10 * 1024 * 1024)

        private fun largeBufferStrategies(): ExchangeStrategies =
            ExchangeStrategies.builder()
                .codecs { it.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE) }
                .build()

        private fun filterResponse() = ExchangeFilterFunction.ofResponseProcessor(::filter)

        private fun filter(response: ClientResponse): Mono<ClientResponse> {
            val status = response.statusCode()

            return when {
                status.is4xxClientError -> clientError(status, response)
                status.is5xxServerError -> serverError(response)
                else -> Mono.just(response)
            }
        }

        private fun clientError(status: HttpStatusCode, response: ClientResponse): Mono<ClientResponse> =
            response
                .bodyToMono(String::class.java)
                .defaultIfEmpty(emptyResponseInfo(response))
                .flatMap { Mono.error(EgressException(message = it, statusCode = status)) }

        private fun serverError(response: ClientResponse): Mono<ClientResponse> =
            response
                .bodyToMono(String::class.java)
                .flatMap { Mono.error(EgressException(message = it)) }

        private fun emptyResponseInfo(response: ClientResponse): String {
            val status = response.statusCode()

            return when {
                isAccessDenied(status) -> "$status: ${reasonForAccessDenial(response)}"
                else -> status.toString()
            }
        }

        private fun isAccessDenied(status: HttpStatusCode) =
            status == HttpStatus.FORBIDDEN || status == HttpStatus.UNAUTHORIZED

        private fun reasonForAccessDenial(response: ClientResponse): String =
            response.headers().asHttpHeaders()[HttpHeaders.WWW_AUTHENTICATE]?.firstOrNull() ?: "(access denied)"
    }
}
