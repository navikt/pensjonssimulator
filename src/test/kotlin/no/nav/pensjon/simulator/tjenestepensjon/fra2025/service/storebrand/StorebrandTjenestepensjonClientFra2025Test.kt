package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.arrangeOkJsonResponse
import no.nav.pensjon.simulator.testutil.arrangeResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.SammenlignAFPService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025ServiceUnitTest.Companion.dummySpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.*
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.http.HttpStatus
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate

class StorebrandTjenestepensjonClientFra2025Test : FunSpec({

    var server: MockWebServer? = null
    var baseUrl: String? = null
    val objectMapper = JsonMapper()

    val sporingslogg = mockk<SporingsloggService> {
        every { logUtgaaendeRequest(Organisasjoner.STOREBRAND, any<Pid>(), any<String>()) } just runs
    }
    val sammenligner = mockk<SammenlignAFPService> {
        every { sammenlignOgLoggAfp(any(), any()) } just runs
    }

    fun client(context: BeanFactory) =
        StorebrandTjenestepensjonClientFra2025(
            baseUrl = baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean<WebClientBase>(),
            traceAid = mockk(relaxed = true),
            sporingslogg = sporingslogg,
            sammenligner = sammenligner,
            objectMapper = context.getBean<ObjectMapper>()
        )

    beforeSpec {
        Arrange.security()
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    test("sender request og leser respons fra Storebrand") {
        val tpNummer = "4081"
        val serverResponse = response(tpNummer)
        server?.arrangeOkJsonResponse(objectMapper.writeValueAsString(serverResponse))

        Arrange.webClientContextRunner().run {
            val result: Result<SimulertTjenestepensjon> =
                client(it).simuler(dummySpec("1963-02-05", afpErForespurt = true), tpNummer)

            result.isSuccess shouldBe true
            with(result.getOrNull().shouldNotBeNull()) {
                tpLeverandoer shouldBe "Storebrand"
                ordningsListe.map { it.tpNummer } shouldBe listOf(tpNummer)
                utbetalingsperioder shouldHaveSize 2
                utbetalingsperioder.map { it.maanedligBelop } shouldBe listOf(1000, 2000)
                aarsakIngenUtbetaling shouldBe listOf("Ikke stoettet: SAERALDERSPAASLAG")
            }
            server?.takeRequest()?.path shouldBe "/$tpNummer/ofa/v1/tjenestepensjon/simuler"
        }
    }

    test("returnerer feil ved feilrespons fra Storebrand") {
        server?.arrangeResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, body = "feil")

        Arrange.webClientContextRunner().run {
            val result = client(it).simuler(dummySpec("1963-02-06"), "4081")

            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<TjenestepensjonSimuleringException>()
            server?.takeRequest()?.path shouldBe "/4081/ofa/v1/tjenestepensjon/simuler"
        }
    }
}) {
    private companion object {

        fun response(tpNummer: String) =
            StorebrandSimulerTjenestepensjonResponse(
                inkludertOrdningListe = listOf(InkludertOrdning(tpnr = tpNummer)),
                utbetalingListe = listOf(
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2026, 3, 1),
                        delytelseListe = listOf(
                            Delytelse(ytelseType = "OAFP", maanedligBelop = 1000),
                            Delytelse(ytelseType = "PAASLAG", maanedligBelop = 2000)
                        )
                    )
                ),
                aarsakIngenUtbetaling = listOf(
                    AarsakIngenUtbetaling(
                        statusKode = "IKKE_STOETTET",
                        statusBeskrivelse = "Ikke stoettet",
                        ytelseType = "SAERALDERSPAASLAG"
                    )
                )
            )
    }
}
