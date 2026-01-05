package no.nav.pensjon.simulator.common.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.statistikk.SimuleringHendelse
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ControllerBaseTest : ShouldSpec({

    val pid = Pid("12906498357")

    context("registrerHendelse") {
        should("registrere simuleringstype 'ALDER' når organisasjonen ikke er Nav") {
            val statistikk = mockk<StatistikkService>(relaxed = true)

            val controller = TestController(
                traceAid = mockk(),
                statistikk,
                organisasjonsnummerProvider = arrangeOrganisasjonsnummer(nummer = "123456789"),
                tilknytningService = mockk()
            )

            controller.testRegistrerHendelse(SimuleringTypeEnum.ENDR_ALDER_M_GJEN)

            verify(exactly = 1) {
                statistikk.registrer(
                    SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer(value = "123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    )
                )
            }
        }
    }

    context("verifiserAtBrukerTilknyttetTpLeverandoer") {
        should("ikke kaste exception når personen er tilknyttet tjenestepensjonsleverandør") {
            val controller = TestController(
                traceAid = mock(),
                organisasjonsnummerProvider = arrangeOrganisasjonsnummer(nummer = "123456789"),
                tilknytningService = arrangeErTilknyttet()
            )

            controller.testVerifiserAtBrukerTilknyttetTpLeverandoer(pid)
        }

        should("ikke kaste exception når Nav simulerer (i dev)") {
            val controller = TestController(
                traceAid = mockk(),
                organisasjonsnummerProvider = arrangeOrganisasjonsnummer(nummer = "889640782"),
                tilknytningService = mockk()
            )

            controller.testVerifiserAtBrukerTilknyttetTpLeverandoer(pid)
        }

        should("kaste exception med årsak når personen ikke er tilknyttet tjenestepensjonsleverandør") {
            val controller = TestController(
                traceAid = mockk<TraceAid>().apply { every { callId() } returns "123-124" },
                organisasjonsnummerProvider = mockk(relaxed = true),
                tilknytningService = mockk(relaxed = true)
            )

            val exception =
                shouldThrow<ResponseStatusException> { controller.testVerifiserAtBrukerTilknyttetTpLeverandoer(pid) }

            with(exception) {
                message shouldBe """403 FORBIDDEN "Call ID: 123-124 | Error: Brukeren er ikke tilknyttet angitt TP-leverandør""""
                reason shouldBe "Call ID: 123-124 | Error: Brukeren er ikke tilknyttet angitt TP-leverandør"
                statusCode shouldBe HttpStatus.FORBIDDEN
            }
        }
    }
})

/**
 * NB: Since Organisasjonsnummer is a @JvmInline value class, Mockk does not support it. Using Mockito instead.
 */
private fun arrangeOrganisasjonsnummer(nummer: String): OrganisasjonsnummerProvider =
    mock<OrganisasjonsnummerProvider>().apply {
        whenever(provideOrganisasjonsnummer()).thenReturn(Organisasjonsnummer(nummer))
    }

/**
 * NB: Since arrangeOrganisasjonsnummer uses Mockito for OrganisasjonsnummerProvider,
 * have to use Mockito for TilknytningService if they are mocked in the same test.
 */
private fun arrangeErTilknyttet(): TilknytningService =
    mock<TilknytningService>().apply {
        whenever(erPersonTilknyttetTjenestepensjonsordning(any(), any())).thenReturn(true)
    }

class TestController(
    traceAid: TraceAid,
    statistikk: StatistikkService? = null,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) :
    ControllerBase(traceAid, statistikk, organisasjonsnummerProvider, tilknytningService) {

    fun testVerifiserAtBrukerTilknyttetTpLeverandoer(pid: Pid) =
        verifiserAtBrukerTilknyttetTpLeverandoer(pid)

    fun testRegistrerHendelse(simuleringstype: SimuleringTypeEnum) {
        registrerHendelse(simuleringstype)
    }

    override fun errorMessage(): String = "error from TestController"
}
