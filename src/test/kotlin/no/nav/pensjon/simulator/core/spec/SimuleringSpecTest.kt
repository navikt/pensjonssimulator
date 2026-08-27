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

    context("withHeltUttakDato") {
        context("ny uttaksdato er før inntektens sluttdato") {
            should("oppdatere inntektens varighet, ikke endre sluttdato og beløp") {
                val spec = simuleringSpec(
                    heltUttakDato = LocalDate.of(2031, 1, 1),
                    inntektEtterHeltUttakTom = LocalDate.of(2039, 1, 31),
                    inntektEtterHeltUttakAntallAar = 8
                ).withHeltUttakDato(LocalDate.of(2035, 1, 1))

                with(spec) {
                    heltUttakDato shouldBe LocalDate.of(2035, 1, 1)
                    inntektEtterHeltUttakAntallAar shouldBe 4 // varighet endret
                    inntektEtterHeltUttakTom shouldBe LocalDate.of(2039, 1, 31) // uforandret
                    inntektEtterHeltUttakBeloep shouldBe 67500 // uforandret
                }
            }
        }

        context("ny uttaksdato er etter inntektens sluttdato") {
            should("inntekten settes til 0 med sluttdato 1 måned etter uttaksdato") {
                val spec = simuleringSpec(
                    heltUttakDato = LocalDate.of(2031, 1, 1),
                    inntektEtterHeltUttakTom = LocalDate.of(2035, 1, 31),
                    inntektEtterHeltUttakAntallAar = 4
                ).withHeltUttakDato(LocalDate.of(2035, 2, 1))

                with(spec) {
                    heltUttakDato shouldBe LocalDate.of(2035, 2, 1)
                    inntektEtterHeltUttakTom shouldBe LocalDate.of(2035, 2, 28) // 1 måned etter uttaksdato
                    inntektEtterHeltUttakAntallAar shouldBe 0
                    inntektEtterHeltUttakBeloep shouldBe 0
                }
            }
        }

        context("ny uttaksdato er etter inntektens sluttdato, deretter før") {
            should("oppdatere inntektens varighet, bruke opprinnelig angitt sluttdato og beløp") {
                val spec = simuleringSpec(
                    heltUttakDato = LocalDate.of(2031, 1, 1),
                    inntektEtterHeltUttakTom = LocalDate.of(2035, 1, 31),
                    inntektEtterHeltUttakAntallAar = 4
                )
                    .withHeltUttakDato(LocalDate.of(2035, 2, 1)) // inntekt -> 0
                    .withHeltUttakDato(LocalDate.of(2033, 1, 1)) // inntekt -> opprinnelig

                with(spec) {
                    heltUttakDato shouldBe LocalDate.of(2033, 1, 1)
                    inntektEtterHeltUttakAntallAar shouldBe 2
                    inntektEtterHeltUttakTom shouldBe LocalDate.of(2035, 1, 31) // opprinnelig verdi
                    inntektEtterHeltUttakBeloep shouldBe 67500 // opprinnelig verdi
                }
            }
        }

        context("ny uttaksdato er udefinert") {
            should("sette varighet til 0, beholde sluttdato og beløp") {
                val spec = simuleringSpec(
                    heltUttakDato = LocalDate.of(2031, 1, 1),
                    inntektEtterHeltUttakTom = LocalDate.of(2035, 1, 31),
                    inntektEtterHeltUttakAntallAar = 4
                )                    .withHeltUttakDato(null)

                with(spec) {
                    heltUttakDato shouldBe null
                    inntektEtterHeltUttakAntallAar shouldBe 0 // varighet 0
                    inntektEtterHeltUttakTom shouldBe LocalDate.of(2035, 1, 31)
                    inntektEtterHeltUttakBeloep shouldBe 67500
                }
            }
        }
    }
})

private fun utlandPeriode(fom: LocalDate, tom: LocalDate?) =
    UtlandPeriode(fom, tom, land = LandkodeEnum.ALB, arbeidet = false)
