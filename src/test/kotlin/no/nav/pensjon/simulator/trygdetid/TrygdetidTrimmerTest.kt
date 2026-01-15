package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class TrygdetidTrimmerTest : ShouldSpec({

    context("aldersbegrens") {
        /**
         * Funksjonen 'aldersbegrens' skal begrense trygdetid til:
         * - alder f.o.m. 16 år (minstealder for trygdetid)
         * - alder t.o.m. utgangen av året personen fyller 66 år (maksimumsalder for trygdetid)
         */
        should("skal begrense trygdetid") {
            val result = TrygdetidTrimmer.aldersbegrens(
                trygdetidsperiodeListe = listOf(
                    TTPeriode().apply {
                        fom = LocalDate.of(2015, 1, 1).toNorwegianDateAtNoon() // dvs. før 16 år
                        tom = LocalDate.of(2067, 12, 31).toNorwegianDateAtNoon() // dvs. etter 66 år
                    }),
                foersteUttakDato = LocalDate.of(2070, 1, 1),
                foedselsdato = LocalDate.of(2000, 1, 15)
                // 16 år 2016-01-15, 66 år 2066-01-15
            )

            result shouldHaveSize 1
            with(result[0]) {
                fom shouldBe LocalDate.of(2016, 1, 15).toNorwegianDateAtNoon()
                tom shouldBe LocalDate.of(2066, 12, 31).toNorwegianDateAtNoon()
            }
        }
    }

    context("removeIkkeAvtaleland") {
        should("fjerne land Norge ikke har trygdeavtale med") {
            val result = TrygdetidTrimmer.removeIkkeAvtaleland(
                trygdetidGrunnlagListe = listOf(
                    TrygdetidOpphold(
                        periode = trygdetidsperiode(aar = 2000, land = LandkodeEnum.NLD),
                        arbeidet = false // Nederland krever ikke arbeid => inkluderes
                    ),
                    TrygdetidOpphold(
                        periode = trygdetidsperiode(aar = 2001, land = LandkodeEnum.BEL),
                        arbeidet = false // Belgia krever arbeid => inkluderes ikke
                    ),
                    TrygdetidOpphold(
                        periode = trygdetidsperiode(aar = 2002, land = LandkodeEnum.EST),
                        arbeidet = true // Estland krever arbeid => inkluderes
                    ),
                    TrygdetidOpphold(
                        periode = trygdetidsperiode(aar = 2003, land = LandkodeEnum.NOR), // Norge inkluderes
                        arbeidet = false
                    ),
                    TrygdetidOpphold(
                        periode = trygdetidsperiode(aar = 2004, land = LandkodeEnum.ANT),
                        arbeidet = true // Antarktis ikke avtaleland => inkluderes ikke
                    )
                )
            )

            result shouldHaveSize 3
            with(result[0]) {
                fom shouldBe LocalDate.of(2000, 1, 1).toNorwegianDateAtNoon()
                tom shouldBe LocalDate.of(2000, 12, 31).toNorwegianDateAtNoon()
                landEnum shouldBe LandkodeEnum.NLD
            }
            result[1].landEnum shouldBe LandkodeEnum.EST
            result[2].landEnum shouldBe LandkodeEnum.NOR
        }
    }
})

private fun trygdetidsperiode(aar: Int, land: LandkodeEnum) =
    TTPeriode().apply {
        fom = LocalDate.of(aar, 1, 1).toNorwegianDateAtNoon()
        tom = LocalDate.of(aar, 12, 31).toNorwegianDateAtNoon()
        landEnum = land
    }
