package no.nav.pensjon.simulator.core.spec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate

class SimuleringSpecTest : FunSpec({

    test("hasSameUttakAs should be true if same dates are given for foersteUttakDato and heltUttakDato respectively") {
        simuleringSpec(
            foersteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2029, 1, 1),
                heltUttakDato = LocalDate.of(2032, 6, 1)
            )
        ) shouldBe true
    }

    test("hasSameUttakAs should be false if dates are respectively different") {
        simuleringSpec(
            foersteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2029, 1, 2),
                heltUttakDato = LocalDate.of(2032, 6, 1)
            )
        ) shouldBe false

        simuleringSpec(
            foersteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2029, 1, 1),
                heltUttakDato = LocalDate.of(2033, 6, 1)
            )
        ) shouldBe false
    }

    test("hasSameUttakAs should be true if all dates are null") {
        simuleringSpec(
            foersteUttakDato = null,
            heltUttakDato = null
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = null,
                heltUttakDato = null
            )
        ) shouldBe true
    }

    test("hasSameUttakAs should be false if null vs non-null") {
        simuleringSpec(
            foersteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = null
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2029, 1, 1),
                heltUttakDato = LocalDate.of(2032, 6, 1)
            )
        ) shouldBe false

        simuleringSpec(
            foersteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = null,
                heltUttakDato = LocalDate.of(2032, 6, 1)
            )
        ) shouldBe false
    }

    test("limitedUtenlandsoppholdAntallAar skal bruke 'utland antall år' hvis ingen utenlandsperioder") {
        simuleringSpec(
            utlandAntallAar = 2,
            utlandPeriodeListe = emptyList()
        ).limitedUtenlandsoppholdAntallAar shouldBe 2
    }

    test("limitedUtenlandsoppholdAntallAar skal bruke utenlandsperiodene hvis 'utland antall år' er 0") {
        simuleringSpec(
            utlandAntallAar = 0,
            utlandPeriodeListe = listOf(
                utlandPeriode(
                    fom = LocalDate.of(2010, 1, 1),
                    tom = LocalDate.of(2010, 12, 31)
                )
            ),
            foedselsdato = LocalDate.of(1963, 1, 15)
        ).limitedUtenlandsoppholdAntallAar shouldBe 1
    }
})

private fun utlandPeriode(fom: LocalDate, tom: LocalDate?) =
    UtlandPeriode(fom, tom, land = LandkodeEnum.ALB, arbeidet = false)
