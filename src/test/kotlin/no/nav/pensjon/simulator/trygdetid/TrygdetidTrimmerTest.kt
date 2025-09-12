package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.trygd.TrygdetidOpphold
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class TrygdetidTrimmerTest : FunSpec({

    /**
     * Funksjonen 'aldersbegrens' skal begrense trygdetid til:
     * - alder f.o.m. 16 år (minstealder for trygdetid)
     * - alder t.o.m. utgangen av året personen fyller 66 år (maksimumsalder for trygdetid)
     */
    test("'aldersbegrens' skal begrense trygdetid)") {
        val result = TrygdetidTrimmer.aldersbegrens(
            trygdetidsperiodeListe = listOf(
                TTPeriode().apply {
                    fom = dateAtNoon(2015, Calendar.JANUARY, 1) // dvs. før 16 år
                    tom = dateAtNoon(2067, Calendar.DECEMBER, 31) // dvs. etter 66 år
                }),
            foersteUttakDato = LocalDate.of(2070, 1, 1),
            foedselsdato = LocalDate.of(2000, 1, 15)
            // 16 år 2016-01-15, 66 år 2066-01-15
        )

        result.size shouldBe 1
        with(result[0]) {
            fom shouldBe dateAtNoon(2016, Calendar.JANUARY, 15)
            tom shouldBe dateAtNoon(2066, Calendar.DECEMBER, 31)
        }
    }

    test("removeIkkeAvtaleland skal fjerne land Norge ikke har trygdeavtale med") {
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

        result.size shouldBe 3
        with(result[0]) {
            fom shouldBe dateAtNoon(2000, Calendar.JANUARY, 1)
            tom shouldBe dateAtNoon(2000, Calendar.DECEMBER, 31)
            landEnum shouldBe LandkodeEnum.NLD
        }
        result[1].landEnum shouldBe LandkodeEnum.EST
        result[2].landEnum shouldBe LandkodeEnum.NOR
    }
})

private fun trygdetidsperiode(aar: Int, land: LandkodeEnum) =
    TTPeriode().apply {
        fom = dateAtNoon(aar, Calendar.JANUARY, 1)
        tom = dateAtNoon(aar, Calendar.DECEMBER, 31)
        landEnum = land
    }
