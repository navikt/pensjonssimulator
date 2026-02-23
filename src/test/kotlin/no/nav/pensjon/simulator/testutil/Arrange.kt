package no.nav.pensjon.simulator.testutil

import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alderspensjon.spec.OffentligSimuleringstypeDeducer
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.testutil.TestObjects.jwt
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml.SamlToken
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml.SamlTokenClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate

object Arrange {

    /**
     * Base64-encoded SAML token with dummy content.
     */
    private const val TEST_SAML_TOKEN_BASE64 =
        """PHNhbWwyOkFzc2VydGlvbiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiIgSUQ9IlNBTUwtYThjNmE4OWEtNmZjMC00NDdiLWE5YmQtMDM2ZGUyNzA0YmNiIiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDYtMDVUMTU6MjM6MTEuNzUwODYwWiIgVmVyc2lvbj0iMi4wIj48c2FtbDI6SXNzdWVyPklTMDI8L3NhbWwyOklzc3Vlcj48U2lnbmF0dXJlIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48U2lnbmVkSW5mbz48Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjxTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz48UmVmZXJlbmNlIFVSST0iI1NBTUwtYThjNmE4OWEtNmZjMC00NDdiLWE5YmQtMDM2ZGUyNzA0YmNiIj48VHJhbnNmb3Jtcz48VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9UcmFuc2Zvcm1zPjxEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIvPjxEaWdlc3RWYWx1ZT42K250ZXN0akRjPTwvRGlnZXN0VmFsdWU+PC9SZWZlcmVuY2U+PC9TaWduZWRJbmZvPjxTaWduYXR1cmVWYWx1ZT51L0MydGVzdG5qdWM9PC9TaWduYXR1cmVWYWx1ZT48L1NpZ25hdHVyZT48c2FtbDI6U3ViamVjdD48c2FtbDI6TmFtZUlEIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6MS4xOm5hbWVpZC1mb3JtYXQ6dW5zcGVjaWZpZWQiPnNydnRlc3Q8L3NhbWwyOk5hbWVJRD48c2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbiBNZXRob2Q9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpjbTpiZWFyZXIiPjxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uRGF0YSBOb3RCZWZvcmU9IjIwMjAtMDYtMDVUMTU6MjM6MTEuNzUwODYwWiIgTm90T25PckFmdGVyPSIyMDIwLTA2LTA1VDE2OjIzOjExLjc1MDg2MFoiLz48L3NhbWwyOlN1YmplY3RDb25maXJtYXRpb24+PC9zYW1sMjpTdWJqZWN0PjxzYW1sMjpDb25kaXRpb25zIE5vdEJlZm9yZT0iMjAyMC0wNi0wNVQxNToyMzoxMS43NTA4NjBaIiBOb3RPbk9yQWZ0ZXI9IjIwMjAtMDYtMDVUMTY6MjM6MTEuNzUwODYwWiIvPjxzYW1sMjpBdHRyaWJ1dGVTdGF0ZW1lbnQ+PHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJpZGVudFR5cGUiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIj48c2FtbDI6QXR0cmlidXRlVmFsdWU+U3lzdGVtcmVzc3Vyczwvc2FtbDI6QXR0cmlidXRlVmFsdWU+PC9zYW1sMjpBdHRyaWJ1dGU+PHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJhdXRoZW50aWNhdGlvbkxldmVsIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSI+PHNhbWwyOkF0dHJpYnV0ZVZhbHVlPjA8L3NhbWwyOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDI6QXR0cmlidXRlPjxzYW1sMjpBdHRyaWJ1dGUgTmFtZT0iY29uc3VtZXJJZCIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiPjxzYW1sMjpBdHRyaWJ1dGVWYWx1ZT5zcnZ0ZXN0PC9zYW1sMjpBdHRyaWJ1dGVWYWx1ZT48L3NhbWwyOkF0dHJpYnV0ZT48L3NhbWwyOkF0dHJpYnV0ZVN0YXRlbWVudD48L3NhbWwyOkFzc2VydGlvbj4="""

    fun foedselsdato(year: Int, month: Int, dayOfMonth: Int): GeneralPersonService =
        mockk<GeneralPersonService>().apply {
            every { foedselsdato(pid) } returns LocalDate.of(year, month, dayOfMonth)
        }

    fun simuleringstype(
        type: SimuleringTypeEnum,
        uttakFom: LocalDate,
        livsvarigOffentligAfpRettFom: LocalDate?
    ): OffentligSimuleringstypeDeducer =
        mockk<OffentligSimuleringstypeDeducer>().apply {
            every { deduceSimuleringstype(pid, uttakFom, livsvarigOffentligAfpRettFom) } returns type
        }

    fun normalder(foedselsdato: LocalDate): NormertPensjonsalderService =
        mockk<NormertPensjonsalderService>().apply {
            every { normalder(foedselsdato) } returns Alder(67, 0)
        }

    fun normalder(): NormertPensjonsalderService =
        mockk<NormertPensjonsalderService>().apply {
            every { normalder(pid) } returns Alder(67, 0)
        }

    fun samlToken(): SamlTokenClient =
        mockk<SamlTokenClient>().apply {
            every { samlAccessToken } returns SamlToken(
                accessToken = TEST_SAML_TOKEN_BASE64,
                expiresIn = 1L,
                tokenType = "test"
            )
        }

    fun security() {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        SecurityContextHolder.getContext().authentication = EnrichedAuthentication(
            initialAuth = TestingAuthenticationToken("TEST_USER", jwt),
            egressTokenSuppliersByService = EgressTokenSuppliersByService(mapOf())
        )
    }

    fun webClientContextRunner(): ApplicationContextRunner =
        ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientTestConfig::class.java)
        )
}

fun MockWebServer.arrangeOkJsonResponse(body: String) {
    this.enqueue(
        MockResponse()
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.OK.value()).setBody(body)
    )
}

fun MockWebServer.arrangeResponse(status: HttpStatus, body: String) {
    this.enqueue(MockResponse().setResponseCode(status.value()).setBody(body))
}
