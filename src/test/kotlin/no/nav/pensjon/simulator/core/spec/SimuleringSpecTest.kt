package no.nav.pensjon.simulator.core.spec

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate

class SimuleringSpecTest : ShouldSpec({

    context("kreverAvsluttetUfoeretrygd") {
        should("gi 'true' for privat AFP") {
            simuleringSpec(type = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT).kreverAvsluttetUfoeretrygd shouldBe true
            simuleringSpec(type = SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT).kreverAvsluttetUfoeretrygd shouldBe true
        }

        should("gi 'true' for livsvarig offentlig AFP med helt uttak") {
            simuleringSpec(
                type = SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG,
                uttaksgrad = UttakGradKode.P_100
            ).kreverAvsluttetUfoeretrygd shouldBe true
        }

        should("gi 'false' for livsvarig offentlig AFP med gradert uttak") {
            simuleringSpec(
                type = SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
                uttaksgrad = UttakGradKode.P_50
            ).kreverAvsluttetUfoeretrygd shouldBe false
        }

        should("gi 'false' for tidsbestemt offentlig AFP") {
            simuleringSpec(type = SimuleringTypeEnum.AFP_ETTERF_ALDER).kreverAvsluttetUfoeretrygd shouldBe false
        }
    }

    context("hasSameUttakAs") {
        should("be true if same dates are given for foersteUttakDato and heltUttakDato respectively") {
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

        should("be false if dates are respectively different") {
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

        should("be true if all dates are null") {
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

        should("be false if null vs non-null") {
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
    }

    context("limitedUtenlandsoppholdAntallAar") {
        should("bruke 'utland antall år' hvis ingen utenlandsperioder") {
            simuleringSpec(
                utlandAntallAar = 2,
                utlandPeriodeListe = emptyList()
            ).limitedUtenlandsoppholdAntallAar shouldBe 2
        }

        should("bruke utenlandsperiodene hvis 'utland antall år' er 0") {
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
    }
})

private fun utlandPeriode(fom: LocalDate, tom: LocalDate?) =
    UtlandPeriode(fom, tom, land = LandkodeEnum.ALB, arbeidet = false)
