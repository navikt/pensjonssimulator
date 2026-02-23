package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.SoapClientConfig.Companion.PATH
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.arrangeResponse
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.AuthAttachingHttpRequestInterceptor
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.XMLHentStillingsprosentListeRequestWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XmlFaultWrapper
import no.nav.pensjon.simulator.tpregisteret.TpOrdning
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
        val client = SpkStillingsprosentSoapClient(
            webServiceTemplate = webServiceTemplate(baseUrl),
            samlTokenClient = Arrange.samlToken(),
            sporingsloggService = arrangeSporing(),
            url = "$baseUrl/nav/SimulereTjenestepensjon"
        )

        shouldThrow<EgressException> {
            client.getStillingsprosenter(
                pid = Pid("12345678901"),
                tpOrdning = TpOrdning(
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
