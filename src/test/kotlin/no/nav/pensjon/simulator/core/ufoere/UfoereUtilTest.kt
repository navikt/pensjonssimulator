package no.nav.pensjon.simulator.core.ufoere

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.UforetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate

class UfoereUtilTest : FunSpec({

    context("validateUfoeregrad") {

        test("kaster ikke exception når person ikke har uforehistorikk") {
            val person = PenPerson().apply {
                uforehistorikk = null
            }
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 1, 1),
                uttakGrad = UttakGradKode.P_100
            )

            shouldNotThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("kaster ikke exception når uforehistorikk har tom uforeperiodeListe") {
            val person = PenPerson().apply {
                uforehistorikk = Uforehistorikk().apply {
                    uforeperiodeListe = mutableListOf()
                }
            }
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 1, 1),
                uttakGrad = UttakGradKode.P_100
            )

            shouldNotThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("kaster ikke exception når sum av ufoeregrad og uttaksgrad er nøyaktig 100") {
            val person = createPersonWithUfoeregrad(50, 2025, UforetypeEnum.UFORE)
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_50
            )

            shouldNotThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("kaster ikke exception når sum av ufoeregrad og uttaksgrad er under 100") {
            val person = createPersonWithUfoeregrad(30, 2025, UforetypeEnum.UFORE)
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_50
            )

            shouldNotThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("kaster BadSpecException når sum av ufoeregrad og uttaksgrad overstiger 100") {
            val person = createPersonWithUfoeregrad(60, 2025, UforetypeEnum.UFORE)
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_50
            )

            val exception = shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }

            exception.message shouldContain "60"
            exception.message shouldContain "50"
            exception.message shouldContain "100"
        }

        test("kaster BadSpecException når 100% uttaksgrad med eksisterende uforegrad") {
            val person = createPersonWithUfoeregrad(20, 2025, UforetypeEnum.UFORE)
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_100
            )

            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("bruker maks ufoeregrad når person har flere uforeperioder for samme år") {
            val person = PenPerson().apply {
                uforehistorikk = Uforehistorikk().apply {
                    uforeperiodeListe = mutableListOf(
                        createUforeperiode(30, 2025, UforetypeEnum.UFORE),
                        createUforeperiode(50, 2025, UforetypeEnum.UFORE),
                        createUforeperiode(40, 2025, UforetypeEnum.UFORE)
                    )
                }
            }
            // Max er 50, uttaksgrad er 60 => sum = 110 > 100
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_60
            )

            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("ignorerer uforeperioder som ikke er for gjeldende år") {
            val person = PenPerson().apply {
                uforehistorikk = Uforehistorikk().apply {
                    uforeperiodeListe = mutableListOf(
                        createUforeperiode(80, 2020, UforetypeEnum.UFORE, ufgTom = LocalDate.of(2023, 12, 31))
                    )
                }
            }
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_100
            )

            // 2020-perioden med tom i 2023 gjelder ikke for 2025
            shouldNotThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("inkluderer uforeperiode som startet tidligere og fortsatt løper (ingen ufgTom)") {
            val person = PenPerson().apply {
                uforehistorikk = Uforehistorikk().apply {
                    uforeperiodeListe = mutableListOf(
                        createUforeperiode(60, 2020, UforetypeEnum.UFORE, ufgTom = null) // Fortsatt løpende
                    )
                }
            }
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_50
            )

            // 60 + 50 = 110 > 100
            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("inkluderer uforeperiode når ufgFom er samme år som uttaksåret") {
            val person = PenPerson().apply {
                uforehistorikk = Uforehistorikk().apply {
                    uforeperiodeListe = mutableListOf(
                        createUforeperiode(60, 2025, UforetypeEnum.UFORE)
                    )
                }
            }
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_50
            )

            // 60 + 50 = 110 > 100
            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("inkluderer uforeperiode når uttaksåret er innenfor periode") {
            val person = PenPerson().apply {
                uforehistorikk = Uforehistorikk().apply {
                    uforeperiodeListe = mutableListOf(
                        createUforeperiode(60, 2020, UforetypeEnum.UFORE, ufgTom = LocalDate.of(2030, 12, 31))
                    )
                }
            }
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_50
            )

            // Periode løper 2020-2030, så 2025 er inkludert. 60 + 50 = 110 > 100
            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("ignorerer ikke-reelle uforeperioder (VIRK_IKKE_UFOR)") {
            val person = PenPerson().apply {
                uforehistorikk = Uforehistorikk().apply {
                    uforeperiodeListe = mutableListOf(
                        createUforeperiode(80, 2025, UforetypeEnum.VIRK_IKKE_UFOR)
                    )
                }
            }
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_100
            )

            // VIRK_IKKE_UFOR er ikke en "real" uforeperiode, så den ignoreres
            shouldNotThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("inkluderer uforeperiode med type UFORE") {
            val person = createPersonWithUfoeregrad(60, 2025, UforetypeEnum.UFORE)
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_50
            )

            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("inkluderer uforeperiode med type YRKE") {
            val person = createPersonWithUfoeregrad(60, 2025, UforetypeEnum.YRKE)
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_50
            )

            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("inkluderer uforeperiode med type UF_M_YRKE") {
            val person = createPersonWithUfoeregrad(60, 2025, UforetypeEnum.UF_M_YRKE)
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_50
            )

            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("håndterer uforeperiode uten uforeTypeEnum som ikke-reell") {
            val person = PenPerson().apply {
                uforehistorikk = Uforehistorikk().apply {
                    uforeperiodeListe = mutableListOf(
                        Uforeperiode().apply {
                            ufg = 80
                            ufgFom = LocalDate.of(2025, 1, 1).toNorwegianDateAtNoon()
                            ufgTom = null
                            uforeTypeEnum = null // Ingen type satt
                        }
                    )
                }
            }
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_100
            )

            // uforeTypeEnum = null => isRealUforeperiode() = false, så ignoreres
            shouldNotThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("bruker uttaksåret fra foersteUttakDato for å bestemme gjeldende uforegrad") {
            val person = PenPerson().apply {
                uforehistorikk = Uforehistorikk().apply {
                    uforeperiodeListe = mutableListOf(
                        createUforeperiode(80, 2024, UforetypeEnum.UFORE, ufgTom = LocalDate.of(2024, 12, 31)),
                        createUforeperiode(30, 2025, UforetypeEnum.UFORE)
                    )
                }
            }
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1), // År 2025
                uttakGrad = UttakGradKode.P_80
            )

            // For 2025: ufoeregrad 30, uttaksgrad 80 => sum = 110 > 100
            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("kaster ikke exception med 0% uttaksgrad selv med høy ufoeregrad") {
            val person = createPersonWithUfoeregrad(100, 2025, UforetypeEnum.UFORE)
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_0
            )

            // 100 + 0 = 100, som er OK
            shouldNotThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("kaster exception med 20% uttaksgrad og 90% ufoeregrad") {
            val person = createPersonWithUfoeregrad(90, 2025, UforetypeEnum.UFORE)
            val spec = createSimuleringSpec(
                foersteUttakDato = LocalDate.of(2025, 6, 1),
                uttakGrad = UttakGradKode.P_20
            )

            // 90 + 20 = 110 > 100
            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(person, spec)
            }
        }

        test("validerer ulike uttaksgrader korrekt") {
            val person = createPersonWithUfoeregrad(50, 2025, UforetypeEnum.UFORE)

            // 50 + 40 = 90, OK
            shouldNotThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(
                    person,
                    createSimuleringSpec(LocalDate.of(2025, 1, 1), UttakGradKode.P_40)
                )
            }

            // 50 + 60 = 110, FEIL
            shouldThrow<BadSpecException> {
                UfoereUtil.validateUfoeregrad(
                    person,
                    createSimuleringSpec(LocalDate.of(2025, 1, 1), UttakGradKode.P_60)
                )
            }
        }
    }
})

private fun createSimuleringSpec(
    foersteUttakDato: LocalDate,
    uttakGrad: UttakGradKode
) = SimuleringSpec(
    type = SimuleringTypeEnum.ALDER,
    sivilstatus = SivilstatusType.UGIF,
    epsHarPensjon = false,
    foersteUttakDato = foersteUttakDato,
    heltUttakDato = null,
    pid = Pid("12345678901"),
    foedselDato = LocalDate.of(1963, 1, 15),
    avdoed = null,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = uttakGrad,
    forventetInntektBeloep = 0,
    inntektUnderGradertUttakBeloep = 0,
    inntektEtterHeltUttakBeloep = 0,
    inntektEtterHeltUttakAntallAar = 0,
    foedselAar = 1963,
    utlandAntallAar = 0,
    utlandPeriodeListe = mutableListOf(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = null,
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

private fun createPersonWithUfoeregrad(
    ufoeregrad: Int,
    aar: Int,
    uforeType: UforetypeEnum
) = PenPerson().apply {
    uforehistorikk = Uforehistorikk().apply {
        uforeperiodeListe = mutableListOf(
            createUforeperiode(ufoeregrad, aar, uforeType)
        )
    }
}

private fun createUforeperiode(
    ufoeregrad: Int,
    aar: Int,
    uforeType: UforetypeEnum,
    ufgTom: LocalDate? = null
) = Uforeperiode().apply {
    ufg = ufoeregrad
    ufgFom = LocalDate.of(aar, 1, 1).toNorwegianDateAtNoon()
    this.ufgTom = ufgTom?.toNorwegianDateAtNoon()
    uforeTypeEnum = uforeType
}
