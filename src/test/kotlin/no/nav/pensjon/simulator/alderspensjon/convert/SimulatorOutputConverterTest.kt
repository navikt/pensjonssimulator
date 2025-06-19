package no.nav.pensjon.simulator.alderspensjon.convert

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertAarligAlderspensjon
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertAlderspensjonFraFolketrygden
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjon
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class SimulatorOutputConverterTest : FunSpec({

    /**
     * Tests mapping of:
     * - alderspensjon to alderspensjonFraFolketrygden
     * - pensjonPeriodeListe to alderspensjon
     * Also tests that:
     * - harUttak = false if no uttaksgrad covers today's date
     * - harNokTrygdetidForGarantipensjon = false if mindre enn 5 år 'kapittel 20'-trygdetid
     */
    test("'pensjon' should map SimulatorOutput to SimulertPensjon") {
        SimulatorOutputConverter.pensjon(
            source = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    kapittel19Andel = 0.2
                    kapittel20Andel = 0.8
                    simulertBeregningInformasjonListe = listOf(
                        SimulertBeregningInformasjon().apply { datoFom = LocalDate.of(2030, 1, 1) }
                    )
                    pensjonPeriodeListe.add(
                        PensjonPeriode().apply {
                            beloep = 1000
                            alderAar = 64
                            simulertBeregningInformasjonListe = mutableListOf(
                                SimulertBeregningInformasjon().apply {
                                    datoFom = LocalDate.of(2031, 2, 1) // not mapped
                                    inntektspensjon = 200
                                    garantipensjon = 300
                                    delingstall = 1.2
                                    pensjonBeholdningFoerUttak = 123000
                                    spt = 2.3
                                    tt_anv_kap19 = 19
                                    tt_anv_kap20 = 4 // => mindre enn 5 år 'kapittel 20'-trygdetid
                                    pa_f92 = 5
                                    pa_e91 = 6
                                    forholdstall = 3.4
                                    grunnpensjon = 400
                                    tilleggspensjon = 500
                                    pensjonstillegg = 600
                                    skjermingstillegg = 700
                                    gjtAPKap19 = 800
                                })
                            uttakGradListe = listOf(
                                Uttaksgrad().apply {
                                    fomDato = dateAtNoon(2030, Calendar.JANUARY, 1)
                                    tomDato = dateAtNoon(2040, Calendar.DECEMBER, 1)
                                    uttaksgrad = 50
                                }
                            )
                        })
                }
            },
            today = LocalDate.of(2025, 2, 15), // no uttaksgrad covers this date
            inntektVedFase1Uttak = 123
        ) shouldBe SimulertPensjon(
            alderspensjon = listOf(
                SimulertAarligAlderspensjon(
                    alderAar = 64,
                    beloep = 1000,
                    inntektspensjon = 200,
                    garantipensjon = 300,
                    delingstall = 1.2,
                    pensjonBeholdningFoerUttak = 123000,
                    andelsbroekKap19 = 0.2,
                    andelsbroekKap20 = 0.8,
                    sluttpoengtall = 2.3,
                    trygdetidKap19 = 19,
                    trygdetidKap20 = 4,
                    poengaarFoer92 = 5,
                    poengaarEtter91 = 6,
                    forholdstall = 3.4,
                    grunnpensjon = 400,
                    tilleggspensjon = 500,
                    pensjonstillegg = 600,
                    skjermingstillegg = 700,
                    kapittel19Gjenlevendetillegg = 800
                )
            ),
            alderspensjonFraFolketrygden = listOf(
                SimulertAlderspensjonFraFolketrygden(
                    datoFom = LocalDate.of(2030, 1, 1),
                    delytelseListe = emptyList(),
                    uttakGrad = 0,
                    maanedligBeloep = 0
                )
            ),
            privatAfp = emptyList(),
            pre2025OffentligAfp = null,
            livsvarigOffentligAfp = emptyList(),
            pensjonBeholdningPeriodeListe = emptyList(),
            harUttak = false,
            harNokTrygdetidForGarantipensjon = false,
            trygdetid = 4, // NB: Mapped twice (also to trygdetidKap20)
            opptjeningGrunnlagListe = emptyList()
        )
    }

    test("'pensjon' should set harUttak to 'true' if uttak covers today's date") {
        SimulatorOutputConverter.pensjon(
            source = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    pensjonPeriodeListe.add(
                        PensjonPeriode().apply {
                            uttakGradListe = listOf(
                                Uttaksgrad().apply {
                                    fomDato = dateAtNoon(2020, Calendar.JANUARY, 1)
                                    tomDato = dateAtNoon(2030, Calendar.DECEMBER, 1)
                                    uttaksgrad = 50
                                }
                            )
                        })
                }
            },
            today = LocalDate.of(2025, 2, 15) // uttaksgrad covers this date
        ).harUttak shouldBe true
    }

    test("'pensjon' should set harUttak to 'false' if uttaksgrad zero") {
        SimulatorOutputConverter.pensjon(
            source = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    pensjonPeriodeListe.add(
                        PensjonPeriode().apply {
                            uttakGradListe = listOf(
                                Uttaksgrad().apply {
                                    fomDato = dateAtNoon(2020, Calendar.JANUARY, 1)
                                    tomDato = dateAtNoon(2030, Calendar.DECEMBER, 1)
                                    uttaksgrad = 0
                                }
                            )
                        })
                }
            },
            today = LocalDate.of(2025, 2, 15) // uttaksgrad covers this date
        ).harUttak shouldBe false
    }

    test("'pensjon' should set harNokTrygdetidForGarantipensjon to 'true' if minst 5 år 'kapittel 20'-trygdetid") {
        SimulatorOutputConverter.pensjon(
            source = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    pensjonPeriodeListe.add(
                        PensjonPeriode().apply {
                            simulertBeregningInformasjonListe = mutableListOf(
                                SimulertBeregningInformasjon().apply { tt_anv_kap20 = 6 }) // => minst 5 år trygdetid
                        })
                }
            },
            today = LocalDate.of(2025, 2, 15)
        ).harNokTrygdetidForGarantipensjon shouldBe true
    }
})
