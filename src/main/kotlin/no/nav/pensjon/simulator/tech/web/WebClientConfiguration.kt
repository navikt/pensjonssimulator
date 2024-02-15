package no.nav.pensjon.simulator.tech.web

import io.netty.channel.ChannelOption
import io.netty.handler.logging.LogLevel
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig : WebClientCustomizer {

    override fun customize(webClientBuilder: WebClient.Builder) {
        webClientBuilder
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT.toInt())
                        .responseTimeout(Duration.ofMillis(TIMEOUT))
                        .doOnConnected(::addTimeoutHandlers)
                        .wiretap(
                            "reactor.netty.http.client.HttpClient",
                            LogLevel.DEBUG,
                            AdvancedByteBufFormat.TEXTUAL
                        )
                )
            )
            .exchangeStrategies(largeBufferStrategies())
            .filter(filterResponse())
    }

    @Bean
    @Primary
    fun regularWebClient(): WebClient =
        WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT.toInt())
                    .responseTimeout(Duration.ofMillis(TIMEOUT))
                    .doOnConnected { conn ->
                        conn.addHandlerLast(ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
                        conn.addHandlerLast(WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
                    })
            )
            .filter(filterResponse())
            .build()

    companion object {
        private const val MAX_IN_MEMORY_SIZE = 10485760 // 10 MB (10 * 1024 * 1024)
        private const val TIMEOUT: Long = 20_000

        private fun addTimeoutHandlers(connection: Connection) {
            connection.addHandlerLast(ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
            connection.addHandlerLast(WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
        }

        private fun largeBufferStrategies(): ExchangeStrategies =
            ExchangeStrategies.builder()
                .codecs { it.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE) }
                .build()

        private fun filterResponse() = ExchangeFilterFunction.ofResponseProcessor(::filter)

        private fun filter(response: ClientResponse): Mono<ClientResponse> {
            val statusCode = response.statusCode()

            return if (statusCode.is4xxClientError)
                clientError(statusCode, response)
            else if (statusCode.is5xxServerError)
                serverError(response)
            else
                Mono.just(response)
        }

        private fun clientError(statusCode: HttpStatusCode, response: ClientResponse): Mono<ClientResponse> =
            response
                .bodyToMono(String::class.java)
                .defaultIfEmpty(emptyResponseInfo(response))
                .flatMap { Mono.error(EgressException(message = it, statusCode = statusCode)) }

        private fun serverError(response: ClientResponse): Mono<ClientResponse> =
            response
                .bodyToMono(String::class.java)
                .flatMap { Mono.error(EgressException(message = it)) }

        private fun emptyResponseInfo(response: ClientResponse): String {
            val statusCode = response.statusCode()

            return if (isAccessDenied(statusCode))
                "$statusCode: ${reasonForAccessDenial(response)}"
            else
                statusCode.toString()
        }

        private fun isAccessDenied(statusCode: HttpStatusCode) =
            statusCode == HttpStatus.FORBIDDEN || statusCode == HttpStatus.UNAUTHORIZED

        private fun reasonForAccessDenial(response: ClientResponse): String =
            response.headers().asHttpHeaders()[HttpHeaders.WWW_AUTHENTICATE]?.firstOrNull() ?: "(access denied)"
    }
}
