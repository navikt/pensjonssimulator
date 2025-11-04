package no.nav.pensjon.simulator.tech.web

import io.netty.channel.ChannelOption
import io.netty.handler.logging.LogLevel
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import java.time.Duration

@Configuration
open class WebClientBaseConfiguration {

    @Bean
    @Primary
    open fun normalWebClientBase(webClientBuilder: WebClient.Builder) =
        WebClientBase(
            builder = webClientBuilder
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient.create()
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, NORMAL_TIMEOUT_SECONDS * 1000)
                            .responseTimeout(Duration.ofMillis(NORMAL_TIMEOUT_SECONDS * 1000L))
                            .doOnConnected(::addNormalTimeoutHandlers)
                            .wiretap(
                                "reactor.netty.http.client.HttpClient",
                                LogLevel.DEBUG,
                                AdvancedByteBufFormat.TEXTUAL
                            )
                    )
                )
        )

    @Bean("long-timeout")
    open fun patientWebClientBase(webClientBuilder: WebClient.Builder) =
        WebClientBase(
            builder = webClientBuilder
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient
                            .create()
                            .doOnConnected(::addLongReadTimeoutHandler)
                    )
                )
        )

    companion object {
        private const val NORMAL_TIMEOUT_SECONDS = 20
        private const val LONG_READ_TIMEOUT_SECONDS = 45

        private fun addNormalTimeoutHandlers(connection: Connection) {
            connection.addHandlerLast(ReadTimeoutHandler(NORMAL_TIMEOUT_SECONDS))
            connection.addHandlerLast(WriteTimeoutHandler(NORMAL_TIMEOUT_SECONDS))
        }

        private fun addLongReadTimeoutHandler(connection: Connection) {
            connection.addHandlerLast(ReadTimeoutHandler(LONG_READ_TIMEOUT_SECONDS))
        }
    }
}
