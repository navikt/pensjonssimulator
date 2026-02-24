package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.web.SoapClientConfig.Companion.PATH
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.arrangeResponse
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.XMLHentStillingsprosentListeRequestWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XMLHentStillingsprosentListeResponseWrapper
import no.nav.pensjon.simulator.tpregisteret.TpOrdning
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpStatus
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.ws.client.core.WebServiceTemplate
import java.time.LocalDate

/**
 * Tests for SpkStillingsprosentSoapClient when SPK returns a successful response.
 */
class SpkStillingsprosentSoapClientSuccessTest : ShouldSpec({

    var server: MockWebServer? = null
    var baseUrl: String? = null

    beforeEach {
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    should("gi stillingsprosentlisten som XML-responsen inneholder") {
        Arrange.security()
        server?.arrangeResponse(HttpStatus.OK, RESPONSE_BODY)
        val client = SpkStillingsprosentSoapClient(
            webServiceTemplate = webServiceTemplate(baseUrl),
            samlTokenClient = Arrange.samlToken(),
            sporingsloggService = arrangeSporing(),
            url = "$baseUrl/nav/SimulereTjenestepensjon"
        )

        client.getStillingsprosenter(
            pid = Pid("12345678901"),
            tpOrdning = TpOrdning(
                navn = "pensjonskasse",
                tpNr = "1234",
                datoSistOpptjening = LocalDate.of(2025, 1, 1),
                tssId = "867530"
            )
        ) shouldBe listOf(
            Stillingsprosent(
                datoFom = LocalDate.of(2020, 1, 1),
                datoTom = LocalDate.of(2025, 1, 31),
                stillingsprosent = 78.9,
                aldersgrense = 70,
                faktiskHovedlonn = "H1",
                stillingsuavhengigTilleggslonn = "T1"
            ),
            Stillingsprosent(
                datoFom = LocalDate.of(2021, 6, 7),
                datoTom = LocalDate.of(2022, 7, 8),
                stillingsprosent = 10.0,
                aldersgrense = 72,
                faktiskHovedlonn = "H2",
                stillingsuavhengigTilleggslonn = "T2"
            )
        )
    }
})

private fun webServiceTemplate(baseUrl: String?) =
    WebServiceTemplate().apply {
        marshaller = Jaxb2Marshaller().apply {
            setClassesToBeBound(XMLHentStillingsprosentListeRequestWrapper::class.java)
        }
        unmarshaller = Jaxb2Marshaller().apply {
            setClassesToBeBound(XMLHentStillingsprosentListeResponseWrapper::class.java)
        }
        setDefaultUri("$baseUrl$PATH")
    }

private fun arrangeSporing(): SporingsloggService =
    mockk<SporingsloggService>().apply {
        every {
            logUtgaaendeRequest(organisasjonsnummer = Organisasjoner.SPK, pid = any(), leverteData = any())
        } just runs
    }

private const val RESPONSE_BODY =
    """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Header/>
    <soap:Body>
       <ns2:hentStillingsprosentListeResponse xmlns:ns2="http://nav.no/ekstern/pensjon/tjenester/tjenestepensjonSimulering/v1">
          <response>
             <stillingsprosentListe>
                 <stillingsprosent>78.9</stillingsprosent>
                 <datoFom>2020-01-01</datoFom>
                 <datoTom>2025-01-31</datoTom>
                 <faktiskHovedlonn>H1</faktiskHovedlonn>
                 <stillingsuavhengigTilleggslonn>T1</stillingsuavhengigTilleggslonn>
                 <aldersgrense>70</aldersgrense>
             </stillingsprosentListe>
             <stillingsprosentListe>
                 <stillingsprosent>10</stillingsprosent>
                 <datoFom>2021-06-07</datoFom>
                 <datoTom>2022-07-08</datoTom>
                 <faktiskHovedlonn>H2</faktiskHovedlonn>
                 <stillingsuavhengigTilleggslonn>T2</stillingsuavhengigTilleggslonn>
                 <aldersgrense>72</aldersgrense>
             </stillingsprosentListe>
          </response>
       </ns2:hentStillingsprosentListeResponse>
    </soap:Body>
</soap:Envelope>"""
