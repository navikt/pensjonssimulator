package no.nav.pensjon.simulator.common.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException

/**
 * NB: These tests work with Mockito, but not with Mockk.
 * Reason: Pid and Organisasjonsnummer are @JvmInline value class (which Mockk does not support)
 */
class ControllerBaseTest : FunSpec({

    val pid = Pid("12906498357")

    test("verifiser at testVerifiserAtBrukerTilknyttetTpLeverandoer ikke kaster exception naar bruker tilknyttet TpLeverandoer") {
        val tilknytning = mock<TilknytningService>()
        val orgNrProvider = mock<OrganisasjonsnummerProvider>()
        val controller = TestController(mock(), orgNrProvider, tilknytning)

        whenever(orgNrProvider.provideOrganisasjonsnummer()).thenReturn(Organisasjonsnummer("123456789"))
        whenever(tilknytning.erPersonTilknyttetTjenestepensjonsordning(any(), any())).thenReturn(true)

        controller.testVerifiserAtBrukerTilknyttetTpLeverandoer(pid)
    }

    test("verifiser at testVerifiserAtBrukerTilknyttetTpLeverandoer ikke kaster exception naar Nav simulerer (i dev)") {
        val tilknytning = mock<TilknytningService>()
        val orgNrProvider = mock<OrganisasjonsnummerProvider>()
        val controller = TestController(mock(), orgNrProvider, tilknytning)

        whenever(orgNrProvider.provideOrganisasjonsnummer()).thenReturn(Organisasjonsnummer("889640782"))

        controller.testVerifiserAtBrukerTilknyttetTpLeverandoer(pid)
    }

    test("should throw ResponseStatusException with correct message") {
        val traceAid = mock<TraceAid>()
        val controller = TestController(traceAid, mock(), mock())

        whenever(traceAid.callId()).thenReturn("123-124")

        val exception =
            shouldThrow<ResponseStatusException> { controller.testVerifiserAtBrukerTilknyttetTpLeverandoer(pid) }

        exception.reason shouldBe "Call ID: 123-124 | Error: Brukeren er ikke tilknyttet angitt TP-leverandør"
        exception.statusCode shouldBe org.springframework.http.HttpStatus.FORBIDDEN

    }
})

class TestController(
    traceAid: TraceAid,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) :
    ControllerBase(traceAid, organisasjonsnummerProvider, tilknytningService) {

    fun testVerifiserAtBrukerTilknyttetTpLeverandoer(pid: Pid) = verifiserAtBrukerTilknyttetTpLeverandoer(pid)

    override fun errorMessage(): String = "error from TestController"
}
