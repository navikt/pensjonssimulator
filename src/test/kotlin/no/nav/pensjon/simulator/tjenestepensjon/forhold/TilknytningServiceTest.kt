package no.nav.pensjon.simulator.tjenestepensjon.forhold

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.testutil.TestObjects.organisasjonsnummer
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.tjenestepensjon.forhold.client.TjenestepensjonsforholdClient
import org.springframework.http.HttpStatus

class TilknytningServiceTest : FunSpec({

    test("erPersonTilknyttetTjenestepensjonsordning should be true when client returns true") {
        val client = client(erTilknyttet = true)
        TilknytningService(client).erPersonTilknyttetTjenestepensjonsordning(pid, organisasjonsnummer) shouldBe true
    }

    test("erPersonTilknyttetTjenestepensjonsordning should be false when client returns false") {
        val client = client(erTilknyttet = false)
        TilknytningService(client).erPersonTilknyttetTjenestepensjonsordning(pid, organisasjonsnummer) shouldBe false
    }

    test("erPersonTilknyttetTjenestepensjonsordning should be false when person ikke funnet") {
        val client: TjenestepensjonsforholdClient = mockk {
            every { hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer) } throws
                    EgressException(
                        message = "Person ikke funnet.",
                        cause = null,
                        statusCode = HttpStatus.NOT_FOUND
                    )
        }

        TilknytningService(client).erPersonTilknyttetTjenestepensjonsordning(pid, organisasjonsnummer) shouldBe false
    }
})

private fun client(erTilknyttet: Boolean): TjenestepensjonsforholdClient =
    mockk {
        every { hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer) } returns erTilknyttet
    }