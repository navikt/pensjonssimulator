package no.nav.pensjon.simulator.alderspensjon.convert

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.trygdetid.Trygdetid
import java.time.LocalDate
import java.util.*

class SimulatorOutputConverterTest : FunSpec({

    /**
     * Tests mapping of:
     * - alderspensjon to alderspensjonFraFolketrygden
     * - pensjonPeriodeListe to alderspensjon
     * Also tests that:
     * - harUttak = false if no uttaksgrad covers today's date
     * - harNokTrygdetidForGarantipensjon = false if mindre enn 5 år trygdetid
     * - trygdetid = max av kapittel 19-trygdetid og kapittel 20-trygdetid
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
                                    garantipensjonssats = 1.1
                                    garantitillegg = 21
                                    delingstall = 1.2
                                    pensjonBeholdningFoerUttak = 123000
                                    spt = 2.3
                                    tt_anv_kap19 = 19
                                    tt_anv_kap20 = 4 // => mindre enn 5 år 'kapittel 20'-trygdetid
                                    pa_f92 = 5
                                    pa_e91 = 6
                                    forholdstall = 3.4
                                    basispensjon = 350
                                    grunnpensjon = 400
                                    tilleggspensjon = 500
                                    restBasisPensjon = 550
                                    pensjonstillegg = 600
                                    skjermingstillegg = 700
                                    gjtAPKap19 = 800
                                    minstePensjonsnivaSats = 11.22
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
                    garantipensjon = SimulertGarantipensjon(aarligBeloep = 300, sats = 1.1),
                    garantitillegg = 21,
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
                    basispensjon = 350,
                    grunnpensjon = 400,
                    tilleggspensjon = 500,
                    restpensjon = 550,
                    pensjonstillegg = 600,
                    skjermingstillegg = 700,
                    kapittel19Gjenlevendetillegg = 800,
                    minstePensjonsnivaaSats = 11.22
                )
            ),
            maanedligAlderspensjonForKnekkpunkter = SimulertMaanedligAlderspensjonForKnekkpunkter(
                vedGradertUttak = null,
                vedHeltUttak = SimulertMaanedligAlderspensjon(beloep = 0, inntektspensjon = null, delingstall = null, pensjonBeholdningFoerUttak = null, pensjonBeholdningEtterUttak = null, sluttpoengtall = null, poengaarFoer92 = null, poengaarEtter91 = null, forholdstall = null, grunnpensjon = null, tilleggspensjon = null, pensjonstillegg = null, skjermingstillegg = null, andelsbroekKap19 = null, andelsbroekKap20 = null, basispensjon = null, restpensjon = null, gjenlevendetillegg = null, minstePensjonsnivaaSats = null, trygdetidKap19 = null, trygdetidKap20 = null, garantipensjon = null, garantitillegg = null),
                vedNormertPensjonsalder = SimulertMaanedligAlderspensjon(beloep = 0, inntektspensjon = null, delingstall = null, pensjonBeholdningFoerUttak = null, pensjonBeholdningEtterUttak = null, sluttpoengtall = null, poengaarFoer92 = null, poengaarEtter91 = null, forholdstall = null, grunnpensjon = null, tilleggspensjon = null, pensjonstillegg = null, skjermingstillegg = null, andelsbroekKap19 = null, andelsbroekKap20 = null, basispensjon = null, restpensjon = null, gjenlevendetillegg = null, minstePensjonsnivaaSats = null, trygdetidKap19 = null, trygdetidKap20 = null, garantipensjon = null, garantitillegg = null)
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
            primaerTrygdetid = Trygdetid(kapittel19 = 19, kapittel20 = 4),
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
        ).primaerTrygdetid.erTilstrekkelig shouldBe true
    }

    test("'pensjon' should populate maanedligAlderspensjonForKnekkpunkter when exact date matches exist") {
        val gradertDato = LocalDate.of(2030, 1, 1)
        val heltDato = LocalDate.of(2032, 1, 1)
        val normertDato = LocalDate.of(2034, 1, 1)

        SimulatorOutputConverter.pensjon(
            source = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    kapittel19Andel = 0.3
                    kapittel20Andel = 0.7
                    simulertBeregningInformasjonListe = listOf(
                        SimulertBeregningInformasjon().apply {
                            datoFom = gradertDato
                            maanedligBeloep = 5000
                            inntektspensjonPerMaaned = 1000
                            delingstall = 14.5
                            pensjonBeholdningEtterUttak = 500000
                            spt = 3.5
                            pa_f92 = 10
                            pa_e91 = 15
                            forholdstall = 1.1
                            grunnpensjonPerMaaned = 800
                            tilleggspensjonPerMaaned = 900
                            pensjonstilleggPerMaaned = 200
                            skjermingstillegg = 50
                            basispensjon = 1200
                            restBasisPensjon = 300
                            gjtAPKap19PerMaaned = 100
                            minstePensjonsnivaSats = 2.5
                            tt_anv_kap19 = 40
                            tt_anv_kap20 = 20
                            garantipensjonPerMaaned = 600
                            garantipensjonssats = 2.0
                            garantitilleggPerMaaned = 50
                        },
                        SimulertBeregningInformasjon().apply {
                            datoFom = heltDato
                            maanedligBeloep = 7000
                            inntektspensjonPerMaaned = 1500
                        },
                        SimulertBeregningInformasjon().apply {
                            datoFom = normertDato
                            maanedligBeloep = 8000
                            inntektspensjonPerMaaned = 2000
                        }
                    )
                }
            },
            today = LocalDate.of(2025, 2, 15),
            gradertUttakDato = gradertDato,
            heltUttakDato = heltDato,
            normertPensjoneringsdato = normertDato
        ).maanedligAlderspensjonForKnekkpunkter shouldBe SimulertMaanedligAlderspensjonForKnekkpunkter(
            vedGradertUttak = SimulertMaanedligAlderspensjon(
                beloep = 5000,
                inntektspensjon = 1000,
                delingstall = 14.5,
                pensjonBeholdningFoerUttak = null,
                pensjonBeholdningEtterUttak = 500000,
                sluttpoengtall = 3.5,
                poengaarFoer92 = 10,
                poengaarEtter91 = 15,
                forholdstall = 1.1,
                grunnpensjon = 800,
                tilleggspensjon = 900,
                pensjonstillegg = 200,
                skjermingstillegg = 50,
                andelsbroekKap19 = 0.3,
                andelsbroekKap20 = 0.7,
                basispensjon = 1200,
                restpensjon = 300,
                gjenlevendetillegg = 100,
                minstePensjonsnivaaSats = 2.5,
                trygdetidKap19 = 40,
                trygdetidKap20 = 20,
                garantipensjon = SimulertMaanedligGarantipensjon(maanedligBeloep = 600, sats = 2.0),
                garantitillegg = 50
            ),
            vedHeltUttak = SimulertMaanedligAlderspensjon(
                beloep = 7000,
                inntektspensjon = 1500,
                delingstall = null,
                pensjonBeholdningFoerUttak = null,
                pensjonBeholdningEtterUttak = null,
                sluttpoengtall = null,
                poengaarFoer92 = null,
                poengaarEtter91 = null,
                forholdstall = null,
                grunnpensjon = null,
                tilleggspensjon = null,
                pensjonstillegg = null,
                skjermingstillegg = null,
                andelsbroekKap19 = 0.3,
                andelsbroekKap20 = 0.7,
                basispensjon = null,
                restpensjon = null,
                gjenlevendetillegg = null,
                minstePensjonsnivaaSats = null,
                trygdetidKap19 = null,
                trygdetidKap20 = null,
                garantipensjon = null,
                garantitillegg = null
            ),
            vedNormertPensjonsalder = SimulertMaanedligAlderspensjon(
                beloep = 8000,
                inntektspensjon = 2000,
                delingstall = null,
                pensjonBeholdningFoerUttak = null,
                pensjonBeholdningEtterUttak = null,
                sluttpoengtall = null,
                poengaarFoer92 = null,
                poengaarEtter91 = null,
                forholdstall = null,
                grunnpensjon = null,
                tilleggspensjon = null,
                pensjonstillegg = null,
                skjermingstillegg = null,
                andelsbroekKap19 = 0.3,
                andelsbroekKap20 = 0.7,
                basispensjon = null,
                restpensjon = null,
                gjenlevendetillegg = null,
                minstePensjonsnivaaSats = null,
                trygdetidKap19 = null,
                trygdetidKap20 = null,
                garantipensjon = null,
                garantitillegg = null
            )
        )
    }

    test("'pensjon' should return null vedGradertUttak and empty vedHeltUttak/vedNormertPensjonsalder when no exact date match exists") {
        val gradertDato = LocalDate.of(2030, 1, 1)
        val heltDato = LocalDate.of(2032, 1, 1)
        val normertDato = LocalDate.of(2034, 1, 1)
        val noMatchDato = LocalDate.of(2099, 1, 1)

        val empty = SimulertMaanedligAlderspensjon(
            beloep = 0,
            inntektspensjon = null,
            delingstall = null,
            pensjonBeholdningFoerUttak = null,
            pensjonBeholdningEtterUttak = null,
            sluttpoengtall = null,
            poengaarFoer92 = null,
            poengaarEtter91 = null,
            forholdstall = null,
            grunnpensjon = null,
            tilleggspensjon = null,
            pensjonstillegg = null,
            skjermingstillegg = null,
            andelsbroekKap19 = null,
            andelsbroekKap20 = null,
            basispensjon = null,
            restpensjon = null,
            gjenlevendetillegg = null,
            minstePensjonsnivaaSats = null,
            trygdetidKap19 = null,
            trygdetidKap20 = null,
            garantipensjon = null,
            garantitillegg = null
        )

        SimulatorOutputConverter.pensjon(
            source = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    simulertBeregningInformasjonListe = listOf(
                        SimulertBeregningInformasjon().apply {
                            datoFom = noMatchDato
                            maanedligBeloep = 5000
                        }
                    )
                }
            },
            today = LocalDate.of(2025, 2, 15),
            gradertUttakDato = gradertDato,
            heltUttakDato = heltDato,
            normertPensjoneringsdato = normertDato
        ).maanedligAlderspensjonForKnekkpunkter shouldBe SimulertMaanedligAlderspensjonForKnekkpunkter(
            vedGradertUttak = null,
            vedHeltUttak = empty,
            vedNormertPensjonsalder = empty
        )
    }

    test("'pensjon' should return null vedGradertUttak when gradertUttakDato is null") {
        val heltDato = LocalDate.of(2032, 1, 1)
        val normertDato = LocalDate.of(2034, 1, 1)

        SimulatorOutputConverter.pensjon(
            source = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    simulertBeregningInformasjonListe = listOf(
                        SimulertBeregningInformasjon().apply {
                            datoFom = heltDato
                            maanedligBeloep = 7000
                        },
                        SimulertBeregningInformasjon().apply {
                            datoFom = normertDato
                            maanedligBeloep = 8000
                        }
                    )
                }
            },
            today = LocalDate.of(2025, 2, 15),
            gradertUttakDato = null,
            heltUttakDato = heltDato,
            normertPensjoneringsdato = normertDato
        ).maanedligAlderspensjonForKnekkpunkter.vedGradertUttak shouldBe null
    }
})
