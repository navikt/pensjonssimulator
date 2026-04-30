package no.nav.pensjon.simulator.inntekt

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class LignetInntektServiceTest : ShouldSpec({

    val inntektService: InntektService = mockk()
    val generelleData: GenerelleDataHolder = mockk()
    val lignetInntektService = LignetInntektService(inntektService, generelleData)

    context("siste lignede inntekt er fra året etter siste gyldige opptjeningsår") {
        val sisteLignedeInntektAar = 2025
        val sisteGyldigeOpptjeningsaar = 2024

        should("legge til siste lignede inntekt og returnere årstallet etter året for denne inntekten") {
            val inntekter = mutableListOf<AarligInntekt>()
            every { generelleData.getSisteGyldigeOpptjeningsaar() } returns sisteGyldigeOpptjeningsaar
            every { inntektService.hentSisteLignetInntekt(pid) } returns inntekt(sisteLignedeInntektAar)

            val nesteInntektsaar = lignetInntektService.behandleSistLignedeInntekt(pid, inntekter)

            nesteInntektsaar shouldBe 2026
            inntekter shouldHaveSize 1
            with(inntekter.first()) {
                inntektAar shouldBe 2025
                beloep shouldBe 100000L
            }
        }
    }

    context("siste lignede inntekt er fra samme år som siste gyldige opptjeningsår") {
        val sisteLignedeInntektAar = 2024
        val sisteGyldigeOpptjeningsaar = 2024

        should("returnere årstallet etter siste gyldige opptjeningsår, og ikke legge til siste lignede inntekt") {
            val inntekter = mutableListOf<AarligInntekt>()
            every { generelleData.getSisteGyldigeOpptjeningsaar() } returns sisteGyldigeOpptjeningsaar
            every { inntektService.hentSisteLignetInntekt(pid) } returns inntekt(sisteLignedeInntektAar)

            val nesteInntektsaar = lignetInntektService.behandleSistLignedeInntekt(pid, inntekter)

            nesteInntektsaar shouldBe 2025
            inntekter.shouldBeEmpty()
        }
    }
})

private fun inntekt(aar: Int) =
    LoependeInntekt(
        aarligBeloep = 100000,
        fom = LocalDate.of(aar, 6, 1)
    )