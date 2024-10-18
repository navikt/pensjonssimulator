package no.nav.pensjon.simulator.tjenestepensjon

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testutil.TestObjects.organisasjonsnummer
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class TilknytningServiceTest : FunSpec({

    test("erPersonTilknyttetTjenestepensjonsordning is true when client returns true") {
        val client = client(erTilknyttet = true)
        TilknytningService(client).erPersonTilknyttetTjenestepensjonsordning(pid, organisasjonsnummer) shouldBe true
    }

    test("erPersonTilknyttetTjenestepensjonsordning is false when client returns false") {
        val client = client(erTilknyttet = false)
        TilknytningService(client).erPersonTilknyttetTjenestepensjonsordning(pid, organisasjonsnummer) shouldBe false
    }
})

private fun client(erTilknyttet: Boolean): TpregisteretClient =
    mock(TpregisteretClient::class.java).also {
        `when`(it.hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer)).thenReturn(erTilknyttet)
    }
