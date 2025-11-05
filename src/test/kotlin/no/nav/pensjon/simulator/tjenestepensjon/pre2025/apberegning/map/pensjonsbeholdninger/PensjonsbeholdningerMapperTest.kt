package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.map.pensjonsbeholdninger

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import java.time.LocalDate

class PensjonsbeholdningerMapperTest : StringSpec({

    ("mapper fungerer som forventet") {
        val beholdningListe = listOf(
            BeholdningPeriode(
                datoFom = LocalDate.of(2025, 1, 1),
                pensjonsbeholdning = 1.0,
                garantipensjonsbeholdning = 2.0,
                garantitilleggsbeholdning = 3.0,
                garantipensjonsniva = null
            ),
            BeholdningPeriode(
                datoFom = LocalDate.of(2026, 1, 1),
                pensjonsbeholdning = 7.0,
                garantipensjonsbeholdning = 8.0,
                garantitilleggsbeholdning = 9.0,
                garantipensjonsniva = null
            )
        )

        val result = PensjonsbeholdningerMapper.map(beholdningListe)

        result.size shouldBe 2

        with (result[0]) {
            fom shouldBe LocalDate.of(2025, 1, 1)
            pensjonsbeholdning shouldBe 1.0
            garantipensjonsbeholdning shouldBe 2.0
            garantitilleggsbeholdning shouldBe 3.0
        }
        with (result[1]) {
            fom shouldBe LocalDate.of(2026, 1, 1)
            pensjonsbeholdning shouldBe 7.0
            garantipensjonsbeholdning shouldBe 8.0
            garantitilleggsbeholdning shouldBe 9.0
        }
    }

    ("mapper håndterer null i felter til Pensjonsbeholdningsperiode") {
        val result = PensjonsbeholdningerMapper.map(listOf(BeholdningPeriode(
            datoFom = LocalDate.of(2026, 1, 1),
            pensjonsbeholdning = null,
            garantipensjonsbeholdning = null,
            garantitilleggsbeholdning = null,
            garantipensjonsniva = null
        )))

        result.size shouldBe 1
        with (result[0]) {
            fom shouldBe LocalDate.of(2026, 1, 1)
            pensjonsbeholdning shouldBe null
            garantipensjonsbeholdning shouldBe null
            garantitilleggsbeholdning shouldBe null
        }
    }

    ("mapper null liste til tom liste") {
        val result = PensjonsbeholdningerMapper.map(null)
        result.size shouldBe 0
    }

})
