package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.SoapClientConfig.Companion.PATH
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.arrangeResponse
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.AuthAttachingHttpRequestInterceptor
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.XMLHentStillingsprosentListeRequestWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XmlFaultWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml.SamlToken
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml.SamlTokenClient
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpStatus
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.ws.client.core.WebServiceTemplate
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender
import java.time.LocalDate

/**
 * Tests for SpkStillingsprosentSoapClient when SPK returns a fault response.
 */
class SpkStillingsprosentSoapClientFaultTest : ShouldSpec({

    var server: MockWebServer? = null
    var baseUrl: String? = null

    beforeEach {
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    should("throw exception if 'stillingsprosentliste kan ikke leveres' fault is returned from SPK") {
        Arrange.security()
        server?.arrangeResponse(HttpStatus.INTERNAL_SERVER_ERROR, FAULT_RESPONSE_BODY)
        val client = SPKStillingsprosentSoapClient(
            webServiceTemplate = webServiceTemplate(baseUrl),
            samlTokenClient = arrangeSamlToken(),
            sporingsloggService = arrangeSporing(),
            url = "$baseUrl/nav/SimulereTjenestepensjon"
        )

        shouldThrow<EgressException> {
            client.getStillingsprosenter(
                fnr = "12345678901",
                tpOrdning = TpOrdningFullDto(
                    navn = "pensjonskasse",
                    tpNr = "1234",
                    datoSistOpptjening = LocalDate.of(2025, 1, 1),
                    tssId = "867530"
                )
            )
        }.message shouldBe "Fault(code=soap:Server," +
                " message=StillingsprosentListeKanIkkeLeveres," +
                " detail=Det finnes ingen historikk for dette fnr)"
    }
})

private fun webServiceTemplate(baseUrl: String?) =
    WebServiceTemplate().apply {
        marshaller = Jaxb2Marshaller().apply {
            setClassesToBeBound(XMLHentStillingsprosentListeRequestWrapper::class.java)
        }
        unmarshaller = Jaxb2Marshaller().apply {
            setClassesToBeBound(XmlFaultWrapper::class.java)
        }
        setDefaultUri("$baseUrl$PATH")
        setInterceptors(arrayOf(AuthAttachingHttpRequestInterceptor()))
        setMessageSender(HttpUrlConnectionMessageSender())
        setCheckConnectionForFault(true)
        setCheckConnectionForError(false)
    }

private fun arrangeSamlToken(): SamlTokenClient =
    mockk<SamlTokenClient>().apply {
        every { samlAccessToken } returns SamlToken(
            accessToken = TEST_SAML_TOKEN_BASE64,
            expiresIn = 1L,
            tokenType = "test"
        )
    }

private fun arrangeSporing(): SporingsloggService =
    mockk<SporingsloggService>().apply {
        every {
            logUtgaaendeRequest(organisasjonsnummer = Organisasjoner.SPK, pid = any(), leverteData = any())
        } just runs
    }

private const val FAULT_RESPONSE_BODY =
    """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Header/>
    <soap:Body wsu:Id="id-ebb42929-60bf-4d6f-b1c7-823144290276" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
        <soap:Fault>
            <faultcode>soap:Server</faultcode>
            <faultstring>StillingsprosentListeKanIkkeLeveres</faultstring>
            <detail>
                <ns2:hentStillingsprosentListestillingsprosentListeKanIkkeLeveres xmlns:ns2="http://nav.no/ekstern/pensjon/tjenester/tjenestepensjonSimulering/v1">
                    <errorMessage>Det finnes ingen historikk for dette fnr</errorMessage>
                </ns2:hentStillingsprosentListestillingsprosentListeKanIkkeLeveres>
            </detail>
        </soap:Fault>
    </soap:Body>
</soap:Envelope>"""

/**
 * Base64-encoded SAML token with dummy content.
 */
private const val TEST_SAML_TOKEN_BASE64 =
    """PHNhbWwyOkFzc2VydGlvbiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiIgSUQ9IlNBTUwtYThjNmE4OWEtNmZjMC00NDdiLWE5YmQtMDM2ZGUyNzA0YmNiIiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDYtMDVUMTU6MjM6MTEuNzUwODYwWiIgVmVyc2lvbj0iMi4wIj48c2FtbDI6SXNzdWVyPklTMDI8L3NhbWwyOklzc3Vlcj48U2lnbmF0dXJlIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48U2lnbmVkSW5mbz48Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjxTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz48UmVmZXJlbmNlIFVSST0iI1NBTUwtYThjNmE4OWEtNmZjMC00NDdiLWE5YmQtMDM2ZGUyNzA0YmNiIj48VHJhbnNmb3Jtcz48VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9UcmFuc2Zvcm1zPjxEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIvPjxEaWdlc3RWYWx1ZT42K250ZXN0akRjPTwvRGlnZXN0VmFsdWU+PC9SZWZlcmVuY2U+PC9TaWduZWRJbmZvPjxTaWduYXR1cmVWYWx1ZT51L0MydGVzdG5qdWM9PC9TaWduYXR1cmVWYWx1ZT48L1NpZ25hdHVyZT48c2FtbDI6U3ViamVjdD48c2FtbDI6TmFtZUlEIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6MS4xOm5hbWVpZC1mb3JtYXQ6dW5zcGVjaWZpZWQiPnNydnRlc3Q8L3NhbWwyOk5hbWVJRD48c2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbiBNZXRob2Q9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpjbTpiZWFyZXIiPjxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uRGF0YSBOb3RCZWZvcmU9IjIwMjAtMDYtMDVUMTU6MjM6MTEuNzUwODYwWiIgTm90T25PckFmdGVyPSIyMDIwLTA2LTA1VDE2OjIzOjExLjc1MDg2MFoiLz48L3NhbWwyOlN1YmplY3RDb25maXJtYXRpb24+PC9zYW1sMjpTdWJqZWN0PjxzYW1sMjpDb25kaXRpb25zIE5vdEJlZm9yZT0iMjAyMC0wNi0wNVQxNToyMzoxMS43NTA4NjBaIiBOb3RPbk9yQWZ0ZXI9IjIwMjAtMDYtMDVUMTY6MjM6MTEuNzUwODYwWiIvPjxzYW1sMjpBdHRyaWJ1dGVTdGF0ZW1lbnQ+PHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJpZGVudFR5cGUiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIj48c2FtbDI6QXR0cmlidXRlVmFsdWU+U3lzdGVtcmVzc3Vyczwvc2FtbDI6QXR0cmlidXRlVmFsdWU+PC9zYW1sMjpBdHRyaWJ1dGU+PHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJhdXRoZW50aWNhdGlvbkxldmVsIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSI+PHNhbWwyOkF0dHJpYnV0ZVZhbHVlPjA8L3NhbWwyOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDI6QXR0cmlidXRlPjxzYW1sMjpBdHRyaWJ1dGUgTmFtZT0iY29uc3VtZXJJZCIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiPjxzYW1sMjpBdHRyaWJ1dGVWYWx1ZT5zcnZ0ZXN0PC9zYW1sMjpBdHRyaWJ1dGVWYWx1ZT48L3NhbWwyOkF0dHJpYnV0ZT48L3NhbWwyOkF0dHJpYnV0ZVN0YXRlbWVudD48L3NhbWwyOkFzc2VydGlvbj4="""
