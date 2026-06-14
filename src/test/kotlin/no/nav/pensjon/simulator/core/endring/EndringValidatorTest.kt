package no.nav.pensjon.simulator.core.endring

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SpecialBeregningInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class EndringValidatorTest : ShouldSpec({

    context("validate") {
        should("kaste exception hvis simuleringstype ikke gjelder endring") {
            shouldThrow<InvalidArgumentException> {
                EndringValidator.validate(simuleringSpec(type = SimuleringTypeEnum.ALDER))
            }.message shouldBe "Invalid simuleringstype: ALDER"
        }

        should("akseptere ENDR_ALDER simuleringstype") {
            shouldNotThrowAny {
                EndringValidator.validate(endringSpec(type = SimuleringTypeEnum.ENDR_ALDER))
            }
        }

        should("akseptere ENDR_AP_M_AFP_PRIVAT simuleringstype") {
            shouldNotThrowAny {
                EndringValidator.validate(endringSpec(type = SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT))
            }
        }

        should("akseptere ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG simuleringstype") {
            shouldNotThrowAny {
                EndringValidator.validate(endringSpec(type = SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG))
            }
        }

        should("akseptere ENDR_ALDER_M_GJEN simuleringstype") {
            shouldNotThrowAny {
                EndringValidator.validate(endringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN))
            }
        }
    }

    context("validate - dato for første uttak") {
        should("kaste exception hvis dato for første uttak er udefinert") {
            shouldThrow<InvalidArgumentException> {
                EndringValidator.validate(endringSpec(foersteUttakDato = null))
            }.message shouldBe "forsteUttakDato must be set, and it must be the first day of the month"
        }

        should("kaste exception hvis dato for første uttak ikke er første dag i måneden") {
            shouldThrow<InvalidArgumentException> {
                EndringValidator.validate(
                    endringSpec(foersteUttakDato = LocalDate.of(2025, 6, 15))
                )
            }.message shouldBe "forsteUttakDato must be set, and it must be the first day of the month"
        }

        should("akseptere dato for første uttak som er første dag i måneden") {
            shouldNotThrowAny {
                EndringValidator.validate(
                    endringSpec(foersteUttakDato = LocalDate.of(2025, 6, 1))
                )
            }
        }
    }

    context("validate - uttaksgrad og dato for helt uttak") {
        should("kaste exception hvis uttaksgrad under 100 % og dato for helt uttak er udefinert") {
            shouldThrow<InvalidArgumentException> {
                EndringValidator.validate(
                    endringSpec(
                        uttakGrad = UttakGradKode.P_50,
                        heltUttakDato = null
                    )
                )
            }.message shouldBe "When uttaksgrad < 100% then heltUttakDato must be set, and it must be the first day of the month"
        }

        should("kaste exception hvis uttaksgrad under 100 % og dato for helt uttak ikke er første dag i måneden") {
            shouldThrow<InvalidArgumentException> {
                EndringValidator.validate(
                    endringSpec(
                        uttakGrad = UttakGradKode.P_50,
                        heltUttakDato = LocalDate.of(2026, 7, 15)
                    )
                )
            }.message shouldBe "When uttaksgrad < 100% then heltUttakDato must be set, and it must be the first day of the month"
        }

        should("akseptere uttaksgrad under 100 % med gyldig dato for helt uttak") {
            shouldNotThrowAny {
                EndringValidator.validate(
                    endringSpec(
                        uttakGrad = UttakGradKode.P_50,
                        heltUttakDato = LocalDate.of(2026, 7, 1)
                    )
                )
            }
        }

        should("ikke kreve dato for helt uttak når uttaksgrad er 100 %") {
            shouldNotThrowAny {
                EndringValidator.validate(
                    endringSpec(
                        uttakGrad = UttakGradKode.P_100,
                        heltUttakDato = null
                    )
                )
            }
        }
    }

    context("validateRequestBasedOnLoependeYtelser") {
        should("ikke kaste exception for ikke-ENDR_ALDER_M_GJEN") {
            shouldNotThrowAny {
                EndringValidator.validateRequestBasedOnLoependeYtelser(
                    spec = endringSpec(type = SimuleringTypeEnum.ENDR_ALDER),
                    forrigeAlderspensjon = null
                )
            }
        }

        should("ikke kaste exception hvis BeregningsResultatAlderspensjon2011 har gjenlevenderett") {
            val resultat = BeregningsResultatAlderspensjon2011().apply {
                beregningsinformasjon = SpecialBeregningInformasjon(
                    epsMottarPensjon = false,
                    epsHarInntektOver2G = false,
                    harGjenlevenderett = true
                )
            }

            shouldNotThrowAny {
                EndringValidator.validateRequestBasedOnLoependeYtelser(
                    spec = endringSpec(
                        type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                        avdoed = null // avdoed.pid er null
                    ),
                    forrigeAlderspensjon = resultat
                )
            }
        }

        should("ikke kaste exception hvis BeregningsResultatAlderspensjon2016 har gjenlevenderett") {
            val resultat = BeregningsResultatAlderspensjon2016().apply {
                beregningsResultat2011 = BeregningsResultatAlderspensjon2011().apply {
                    beregningsInformasjonKapittel19 = BeregningsInformasjon().apply {
                        rettPaGjenlevenderett = true
                    }
                }
            }

            shouldNotThrowAny {
                EndringValidator.validateRequestBasedOnLoependeYtelser(
                    spec = endringSpec(
                        type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                        avdoed = null
                    ),
                    forrigeAlderspensjon = resultat
                )
            }
        }

        should("kaste exception for ENDR_ALDER_M_GJEN uten gjenlevenderett og uten avdoed.pid") {
            val resultat = BeregningsResultatAlderspensjon2011().apply {
                beregningsinformasjon = SpecialBeregningInformasjon(
                    epsMottarPensjon = false,
                    epsHarInntektOver2G = false,
                    harGjenlevenderett = false
                )
            }

            shouldThrow<InvalidArgumentException> {
                EndringValidator.validateRequestBasedOnLoependeYtelser(
                    spec = endringSpec(
                        type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                        avdoed = null
                    ),
                    forrigeAlderspensjon = resultat
                )
            }.message shouldBe "avdoed.pid must be set for SimuleringType ENDR_ALDER_M_GJEN"
        }

        should("kaste exception for ENDR_ALDER_M_GJEN med null forrigeAlderspensjon og uten avdoed.pid") {
            shouldThrow<InvalidArgumentException> {
                EndringValidator.validateRequestBasedOnLoependeYtelser(
                    spec = endringSpec(
                        type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                        avdoed = null
                    ),
                    forrigeAlderspensjon = null
                )
            }.message shouldBe "avdoed.pid must be set for SimuleringType ENDR_ALDER_M_GJEN"
        }

        should("akseptere ENDR_ALDER_M_GJEN uten gjenlevenderett men med avdøds PID") {
            val resultat = BeregningsResultatAlderspensjon2011().apply {
                beregningsinformasjon = SpecialBeregningInformasjon(
                    epsMottarPensjon = false,
                    epsHarInntektOver2G = false,
                    harGjenlevenderett = false
                )
            }

            shouldNotThrowAny {
                EndringValidator.validateRequestBasedOnLoependeYtelser(
                    spec = endringSpec(
                        type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                        avdoed = avdoedSpec()
                    ),
                    forrigeAlderspensjon = resultat
                )
            }
        }

        context("har løpende ytelse uten gjenlevenderett") {
            should("kaste exception for endring av pensjon med gjenlevenderett hvis avdødes ID ikke er angitt") {
                shouldThrow<InvalidArgumentException> {
                    EndringValidator.validateRequestBasedOnLoependeYtelser(
                        spec = endringSpec(
                            type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                            avdoed = null
                        ),
                        forrigeAlderspensjon =
                            no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat()
                    )
                }.message shouldBe "avdoed.pid must be set for SimuleringType ENDR_ALDER_M_GJEN"
            }
        }
    }
})

private fun endringSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.ENDR_ALDER,
    foersteUttakDato: LocalDate? = LocalDate.of(2025, 1, 1),
    heltUttakDato: LocalDate? = LocalDate.of(2027, 1, 1),
    uttakGrad: UttakGradKode = UttakGradKode.P_100,
    avdoed: Avdoed? = avdoedSpec()
) = SimuleringSpec(
    type = type,
    sivilstatus = SivilstatusType.UGIF,
    epsHarPensjon = false,
    foersteUttakDato = foersteUttakDato,
    heltUttakDato = heltUttakDato,
    pid = Pid("12345678901"),
    foedselDato = LocalDate.of(1963, 1, 1),
    avdoed = avdoed,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = uttakGrad,
    forventetInntektBeloep = 250000,
    inntektUnderGradertUttakBeloep = 125000,
    inntektEtterHeltUttakBeloep = 67500,
    inntektEtterHeltUttakAntallAar = 5,
    foedselAar = 1963,
    utlandAntallAar = 0,
    utlandPeriodeListe = mutableListOf(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = false,
    epsHarInntektOver2G = false,
    livsvarigOffentligAfp = null,
    pre2025OffentligAfp = null,
    erAnonym = false,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = true,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false
)

private fun avdoedSpec() =
    Avdoed(
        pid = Pid("12345678901"),
        antallAarUtenlands = 0,
        inntektFoerDoed = 0,
        doedDato = LocalDate.of(2020, 1, 1)
    )
