package no.nav.pensjon.simulator.core.endring

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
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

class EndringValidatorTest : FunSpec({

    test("'validate' should throw exception if simuleringstype ikke gjelder endring") {
        shouldThrow<InvalidArgumentException> {
            EndringValidator.validate(simuleringSpec(type = SimuleringTypeEnum.ALDER))
        }.message shouldBe "Invalid simuleringstype: ALDER"
    }

    test("'validate' aksepterer ENDR_ALDER simuleringstype") {
        shouldNotThrowAny {
            EndringValidator.validate(
                endringSpec(type = SimuleringTypeEnum.ENDR_ALDER)
            )
        }
    }

    test("'validate' aksepterer ENDR_AP_M_AFP_PRIVAT simuleringstype") {
        shouldNotThrowAny {
            EndringValidator.validate(
                endringSpec(type = SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT)
            )
        }
    }

    test("'validate' aksepterer ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG simuleringstype") {
        shouldNotThrowAny {
            EndringValidator.validate(
                endringSpec(type = SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG)
            )
        }
    }

    test("'validate' aksepterer ENDR_ALDER_M_GJEN simuleringstype") {
        shouldNotThrowAny {
            EndringValidator.validate(
                endringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN)
            )
        }
    }

    // Tests for validate() - foersteUttakDato

    test("'validate' kaster exception hvis foersteUttakDato er null") {
        shouldThrow<InvalidArgumentException> {
            EndringValidator.validate(
                endringSpec(foersteUttakDato = null)
            )
        }.message shouldBe "forsteUttakDato must be set, and it must be the first day of the month"
    }

    test("'validate' kaster exception hvis foersteUttakDato ikke er første dag i måneden") {
        shouldThrow<InvalidArgumentException> {
            EndringValidator.validate(
                endringSpec(foersteUttakDato = LocalDate.of(2025, 6, 15))
            )
        }.message shouldBe "forsteUttakDato must be set, and it must be the first day of the month"
    }

    test("'validate' aksepterer foersteUttakDato som er første dag i måneden") {
        shouldNotThrowAny {
            EndringValidator.validate(
                endringSpec(foersteUttakDato = LocalDate.of(2025, 6, 1))
            )
        }
    }

    // Tests for validate() - uttakGrad and heltUttakDato

    test("'validate' kaster exception hvis uttakGrad under 100% og heltUttakDato er null") {
        shouldThrow<InvalidArgumentException> {
            EndringValidator.validate(
                endringSpec(
                    uttakGrad = UttakGradKode.P_50,
                    heltUttakDato = null
                )
            )
        }.message shouldBe "When uttaksgrad < 100% then heltUttakDato must be set, and it must be the first day of the month"
    }

    test("'validate' kaster exception hvis uttakGrad under 100% og heltUttakDato ikke er første dag i måneden") {
        shouldThrow<InvalidArgumentException> {
            EndringValidator.validate(
                endringSpec(
                    uttakGrad = UttakGradKode.P_50,
                    heltUttakDato = LocalDate.of(2026, 7, 15)
                )
            )
        }.message shouldBe "When uttaksgrad < 100% then heltUttakDato must be set, and it must be the first day of the month"
    }

    test("'validate' aksepterer uttakGrad under 100% med gyldig heltUttakDato") {
        shouldNotThrowAny {
            EndringValidator.validate(
                endringSpec(
                    uttakGrad = UttakGradKode.P_50,
                    heltUttakDato = LocalDate.of(2026, 7, 1)
                )
            )
        }
    }

    test("'validate' krever ikke heltUttakDato når uttakGrad er 100%") {
        shouldNotThrowAny {
            EndringValidator.validate(
                endringSpec(
                    uttakGrad = UttakGradKode.P_100,
                    heltUttakDato = null
                )
            )
        }
    }

    // Tests for validate() - ENDR_ALDER_M_GJEN and avdoed.doedDato

    test("'validate' kaster exception for ENDR_ALDER_M_GJEN hvis avdoed.doedDato er null") {
        shouldThrow<InvalidArgumentException> {
            EndringValidator.validate(
                endringSpec(
                    type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                    avdoed = Avdoed(
                        pid = Pid("12345678901"),
                        antallAarUtenlands = 0,
                        inntektFoerDoed = 0,
                        doedDato = LocalDate.of(2020, 1, 1)
                    ).let { null } // avdoed is null
                )
            )
        }.message shouldBe "avdod.dodsdato must be set for simuleringstype ENDR_ALDER_M_GJEN"
    }

    test("'validate' aksepterer ENDR_ALDER_M_GJEN med gyldig avdoed.doedDato") {
        shouldNotThrowAny {
            EndringValidator.validate(
                endringSpec(
                    type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                    avdoed = Avdoed(
                        pid = Pid("12345678901"),
                        antallAarUtenlands = 0,
                        inntektFoerDoed = 0,
                        doedDato = LocalDate.of(2020, 1, 1)
                    )
                )
            )
        }
    }

    // Tests for validateRequestBasedOnLoependeYtelser()

    test("'validateRequestBasedOnLoependeYtelser' kaster ikke exception for ikke-ENDR_ALDER_M_GJEN") {
        shouldNotThrowAny {
            EndringValidator.validateRequestBasedOnLoependeYtelser(
                spec = endringSpec(type = SimuleringTypeEnum.ENDR_ALDER),
                forrigeAlderspensjon = null
            )
        }
    }

    test("'validateRequestBasedOnLoependeYtelser' kaster ikke exception hvis BeregningsResultatAlderspensjon2011 har gjenlevenderett") {
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

    test("'validateRequestBasedOnLoependeYtelser' kaster ikke exception hvis BeregningsResultatAlderspensjon2016 har gjenlevenderett") {
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

    test("'validateRequestBasedOnLoependeYtelser' kaster exception for ENDR_ALDER_M_GJEN uten gjenlevenderett og uten avdoed.pid") {
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

    test("'validateRequestBasedOnLoependeYtelser' kaster exception for ENDR_ALDER_M_GJEN med null forrigeAlderspensjon og uten avdoed.pid") {
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

    test("'validateRequestBasedOnLoependeYtelser' aksepterer ENDR_ALDER_M_GJEN uten gjenlevenderett men med avdoed.pid") {
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
                    avdoed = Avdoed(
                        pid = Pid("12345678901"),
                        antallAarUtenlands = 0,
                        inntektFoerDoed = 0,
                        doedDato = LocalDate.of(2020, 1, 1)
                    )
                ),
                forrigeAlderspensjon = resultat
            )
        }
    }

    test("'validateRequestBasedOnLoependeYtelser' returnerer false for annet resultattype enn 2011/2016") {
        // BeregningsResultatAfpPrivat er ikke 2011 eller 2016, så harAlderspensjonMedGjenlevenderett returnerer false
        shouldThrow<InvalidArgumentException> {
            EndringValidator.validateRequestBasedOnLoependeYtelser(
                spec = endringSpec(
                    type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                    avdoed = null
                ),
                forrigeAlderspensjon = no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat()
            )
        }.message shouldBe "avdoed.pid must be set for SimuleringType ENDR_ALDER_M_GJEN"
    }
})

private fun endringSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.ENDR_ALDER,
    foersteUttakDato: LocalDate? = LocalDate.of(2025, 1, 1),
    heltUttakDato: LocalDate? = LocalDate.of(2027, 1, 1),
    uttakGrad: UttakGradKode = UttakGradKode.P_100,
    avdoed: Avdoed? = Avdoed(
        pid = Pid("12345678901"),
        antallAarUtenlands = 0,
        inntektFoerDoed = 0,
        doedDato = LocalDate.of(2020, 1, 1)
    )
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
