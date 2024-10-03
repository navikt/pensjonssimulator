package no.nav.pensjon.simulator.tpregisteret

import no.nav.pensjon.simulator.tech.trace.TraceAid
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.net.URI

@ExtendWith(MockitoExtension::class)
class TpregisteretClientTest {

    @Mock
    private lateinit var webClientBuilder: WebClient.Builder

    @Mock
    private lateinit var webClient: WebClient

    @Mock
    private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec

    @Mock
    private lateinit var requestBodySpec: WebClient.RequestBodySpec

    @Mock
    private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>

    @Mock
    private lateinit var responseSpec: WebClient.ResponseSpec

    @Mock
    private lateinit var traceAid: TraceAid

    private lateinit var tpregisteretClient: TpregisteretClient

    @BeforeEach
    fun setUp() {
        `when`(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder)
        `when`(webClientBuilder.build()).thenReturn(webClient)
        `when`(webClient.post()).thenReturn(requestBodyUriSpec)
        `when`(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec)
        `when`(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.retrieve()).thenReturn(responseSpec)

        tpregisteretClient = TpregisteretClient(
            baseUrl = "http://localhost:8080",
            retryAttempts = "3",
            webClientBuilder = webClientBuilder,
            traceAid = traceAid
        )
    }

    @Test
    fun `should return true when response has forhold as true`() {
        val pid = "12345678901"
        val orgNummer = "123456789"
        val response = BrukerTilknyttetTpLeverandoerResponse(true)
        `when`(responseSpec.bodyToMono(BrukerTilknyttetTpLeverandoerResponse::class.java))
            .thenReturn(Mono.just(response))

        val result = tpregisteretClient.hentErBrukerTilknyttetTpLeverandoer(pid, orgNummer)

        assertTrue(result)
    }

    @Test
    fun `should return false when response is null`() {
        val pid = "12345678901"
        val orgNummer = "123456789"
        `when`(responseSpec.bodyToMono(BrukerTilknyttetTpLeverandoerResponse::class.java))
            .thenReturn(Mono.empty())

        val result = tpregisteretClient.hentErBrukerTilknyttetTpLeverandoer(pid, orgNummer)

        assertFalse(result)
    }

    @Test
    fun `should return false when WebClientRequestException is thrown`() {
        val pid = "12345678901"
        val orgNummer = "123456789"
        `when`(responseSpec.bodyToMono(BrukerTilknyttetTpLeverandoerResponse::class.java))
            .thenThrow(WebClientRequestException(Throwable("Error"), HttpMethod.GET, URI.create("http://localhost:8080"), HttpHeaders()))

        val result = tpregisteretClient.hentErBrukerTilknyttetTpLeverandoer(pid, orgNummer)

        assertFalse(result)
    }

    @Test
    fun `should return false when WebClientResponseException is thrown`() {
        val pid = "12345678901"
        val orgNummer = "123456789"
        `when`(responseSpec.bodyToMono(BrukerTilknyttetTpLeverandoerResponse::class.java))
            .thenThrow(WebClientResponseException.create(500, "Internal Server Error", HttpHeaders(), byteArrayOf(), null))

        val result = tpregisteretClient.hentErBrukerTilknyttetTpLeverandoer(pid, orgNummer)

        assertFalse(result)
    }

}