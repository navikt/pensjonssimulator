package no.nav.pensjon.simulator.core.spec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate

class UtlandPeriodeConverterTest : FunSpec({

    val foedselsdato = LocalDate.of(1963, 1, 15)

    test("'limitedAntallAar' skal gi 0 hvis ingen perioder") {
        UtlandPeriodeConverter.limitedAntallAar(
            periodeListe = emptyList(),
            foedselsdato
        ) shouldBe 0
    }

    test("'limitedAntallAar' skal gi 0 for periode med ikke-komplett år") {
        UtlandPeriodeConverter.limitedAntallAar(
            periodeListe = listOf(
                utlandPeriode(
                    fom = LocalDate.of(2010, 1, 1),
                    tom = LocalDate.of(2010, 12, 30) // mangler én dag
                )
            ),
            foedselsdato
        ) shouldBe 0
    }

    test("'limitedAntallAar' skal gi 60 hvis periodens sluttdato er udefinert") {
        UtlandPeriodeConverter.limitedAntallAar(
            periodeListe = listOf(
                utlandPeriode(
                    fom = LocalDate.of(2010, 1, 1),
                    tom = null
                )
            ),
            foedselsdato
        ) shouldBe 60
    }

    test("'limitedAntallAar' skal begrenses til 60") {
        UtlandPeriodeConverter.limitedAntallAar(
            periodeListe = listOf(
                utlandPeriode(
                    fom = LocalDate.of(2000, 1, 1),
                    tom = LocalDate.of(2070, 1, 1)
                ),
                utlandPeriode(
                    fom = LocalDate.of(2080, 1, 1),
                    tom = null
                )
            ),
            foedselsdato
        ) shouldBe 60
    }

    test("'limitedAntallAar' skal avrundes nedover til nærmeste heltall") {
        UtlandPeriodeConverter.limitedAntallAar(
            periodeListe = listOf(
                utlandPeriode(
                    fom = LocalDate.of(2010, 2, 20),
                    tom = LocalDate.of(2012, 10, 10)
                )
            ),
            foedselsdato
        ) shouldBe 2
    }

    test("'limitedAntallAar' skal ikke ta med tid før minimumsalder for trygdetid") {
        UtlandPeriodeConverter.limitedAntallAar(
            periodeListe = listOf(
                utlandPeriode(
                    fom = LocalDate.of(2013, 1, 1),
                    tom = LocalDate.of(2015, 1, 14)
                )
            ),
            foedselsdato = LocalDate.of(1998, 1, 15) // dato da minimumsalder (16 år) oppnås er 2014-01-15
        ) shouldBe 1 // tiden 2013-01-01 t.o.m. 2014-01-14 tas ikke med i beregningen
    }

    test("'limitedAntallAar' skal håndtere tilfellet der alle periodene slutter før minimumsalder for trygdetid") {
        UtlandPeriodeConverter.limitedAntallAar(
            periodeListe = listOf(
                utlandPeriode(
                    fom = LocalDate.of(2010, 1, 1),
                    tom = LocalDate.of(2010, 12, 31)
                )
            ),
            foedselsdato = LocalDate.of(2000, 1, 15) // dato da minimumsalder (16 år) oppnås er 2016-01-15
        ) shouldBe 0
    }

    test("'limitedAntallAar' ignorerer perioder der startdato er etter sluttdato") {
        UtlandPeriodeConverter.limitedAntallAar(
            periodeListe = listOf(
                utlandPeriode(
                    fom = LocalDate.of(2025, 1, 1), // etter sluttdato ('tom')
                    tom = LocalDate.of(2020, 1, 1)
                )
            ),
            foedselsdato = LocalDate.of(1998, 1, 15)
        ) shouldBe 0
    }

    /**
     * NB: Test som illustrerer en mangel ved utregningen.
     */
    test("'limitedAntallAar' mangler håndtering av skuddår") {
        UtlandPeriodeConverter.limitedAntallAar(
            periodeListe = listOf(
                utlandPeriode(
                    fom = LocalDate.of(2004, 1, 1), // 2004 har 366 dager
                    tom = LocalDate.of(2004, 12, 30) // dvs. 365 dager, men ikke et helt år i dette tilfellet
                )
            ),
            foedselsdato
        ) shouldBe 1 // hvis skuddår hadde blitt tatt med i beregningen, skulle denne vært 0
    }

    /**
     * NB: Test som illustrerer en mangel ved utregningen.
     */
    test("'limitedAntallAar' mangler håndtering av overlapp") {
        UtlandPeriodeConverter.limitedAntallAar(
            periodeListe = listOf(
                utlandPeriode(
                    fom = LocalDate.of(2020, 1, 1),
                    tom = LocalDate.of(2022, 12, 31)
                ),
                utlandPeriode(
                    fom = LocalDate.of(2021, 1, 1), // overlapper med...
                    tom = LocalDate.of(2021, 12, 31) // ...perioden over
                )
            ),
            foedselsdato
        ) shouldBe 4 // hvis overlapp hadde blitt håndtert, skulle denne vært 3
    }
})

private fun utlandPeriode(fom: LocalDate, tom: LocalDate?) =
    UtlandPeriode(fom, tom, land = LandkodeEnum.ALB, arbeidet = false)
