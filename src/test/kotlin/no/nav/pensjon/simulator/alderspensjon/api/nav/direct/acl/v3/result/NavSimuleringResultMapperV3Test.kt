package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.opptjening.OpptjeningGrunnlag
import java.time.LocalDate

class NavSimuleringResultMapperV3Test : FunSpec({

    /**
     * Test av mapping ved gradert uttak.
     * Forventet resultat er bl.a.:
     * - alderspensjonMaanedsbeloep heltUttakBeloep skal være 0
     */
    test("toDto for gradert uttak med alternativ") {
        NavSimuleringResultMapperV3.toDto(
            SimulertPensjonEllerAlternativ(
                pensjon = SimulertPensjon(
                    alderspensjon = listOf(
                        SimulertAarligAlderspensjon(
                            alderAar = 65,
                            beloep = 123,
                            inntektspensjon = 234,
                            garantipensjon = 345,
                            delingstall = 1.2,
                            pensjonBeholdningFoerUttak = 456,
                            andelsbroekKap19 = 0.3,
                            andelsbroekKap20 = 0.7,
                            sluttpoengtall = 5.6,
                            trygdetidKap19 = 30,
                            trygdetidKap20 = 40,
                            poengaarFoer92 = 10,
                            poengaarEtter91 = 20,
                            forholdstall = 0.8,
                            grunnpensjon = 567,
                            tilleggspensjon = 678,
                            pensjonstillegg = 789,
                            skjermingstillegg = 890,
                            kapittel19Gjenlevendetillegg = 321
                        )
                    ),
                    alderspensjonFraFolketrygden = listOf(
                        SimulertAlderspensjonFraFolketrygden(
                            datoFom = LocalDate.of(2021, 1, 1),
                            delytelseListe = listOf(
                                SimulertDelytelse(type = YtelseskomponentTypeEnum.GAP, beloep = 567)
                            ),
                            uttakGrad = 50, // => gradert
                            maanedligBeloep = 678 // skal mappes til gradertUttakBeloep
                        )
                    ),
                    privatAfp = listOf(
                        SimulertPrivatAfp(
                            alderAar = 66,
                            beloep = 789,
                            kompensasjonstillegg = 123,
                            kronetillegg = 5,
                            livsvarig = 93,
                            maanedligBeloep = 100
                        )
                    ),
                    pre2025OffentligAfp = SimulertPre2025OffentligAfp(
                        alderAar = 62,
                        totaltAfpBeloep = 890,
                        tidligereArbeidsinntekt = 123,
                        grunnbeloep = 456,
                        sluttpoengtall = 7.8,
                        trygdetid = 30,
                        poengaarTom1991 = 10,
                        poengaarFom1992 = 20,
                        grunnpensjon = 567,
                        tilleggspensjon = 678,
                        afpTillegg = 789,
                        saertillegg = 890,
                        afpGrad = 80,
                        afpAvkortetTil70Prosent = false
                    ),
                    livsvarigOffentligAfp = listOf(
                        SimulertLivsvarigOffentligAfp(
                            alderAar = 63,
                            beloep = 901,
                            maanedligBeloep = 100
                        )
                    ),
                    pensjonBeholdningPeriodeListe = listOf(
                        SimulertPensjonBeholdningPeriode(
                            pensjonBeholdning = 2.3,
                            garantipensjonBeholdning = 3.4,
                            garantitilleggBeholdning = 4.5,
                            datoFom = LocalDate.of(2022, 2, 1),
                            garantipensjonNivaa = SimulertGarantipensjonNivaa(
                                beloep = 5.6,
                                satsType = "sats1",
                                sats = 6.7,
                                anvendtTrygdetid = 40
                            )
                        )
                    ),
                    harUttak = true,
                    harTilstrekkeligTrygdetid = true,
                    trygdetid = 39,
                    opptjeningGrunnlagListe = listOf(OpptjeningGrunnlag(aar = 1999, pensjonsgivendeInntekt = 1002))
                ),
                alternativ = SimulertAlternativ(
                    gradertUttakAlder = SimulertUttakAlder(
                        alder = Alder(aar = 66, maaneder = 2),
                        uttakDato = LocalDate.of(2023, 3, 1),
                    ),
                    uttakGrad = UttakGradKode.P_20,
                    heltUttakAlder = SimulertUttakAlder(
                        alder = Alder(aar = 67, maaneder = 0),
                        uttakDato = LocalDate.of(2024, 1, 1),
                    ),
                    resultStatus = SimulatorResultStatus.GOOD
                )
            )
        ) shouldBe NavSimuleringResultV3(
            alderspensjonListe = listOf(
                NavAlderspensjonV3(
                    alderAar = 65,
                    beloep = 123,
                    inntektspensjon = 234,
                    garantipensjon = 345,
                    delingstall = 1.2,
                    pensjonBeholdningFoerUttak = 456,
                    andelsbroekKap19 = 0.3,
                    andelsbroekKap20 = 0.7,
                    sluttpoengtall = 5.6,
                    trygdetidKap19 = 30,
                    trygdetidKap20 = 40,
                    poengaarFoer92 = 10,
                    poengaarEtter91 = 20,
                    forholdstall = 0.8,
                    grunnpensjon = 567,
                    tilleggspensjon = 678,
                    pensjonstillegg = 789,
                    skjermingstillegg = 890,
                    kapittel19Gjenlevendetillegg = 321
                )
            ),
            alderspensjonMaanedsbeloep = NavMaanedsbeloepV3(
                gradertUttakBeloep = 678,
                heltUttakBeloep = 0 // since gradert uttak
            ),
            pre2025OffentligAfp = NavPre2025OffentligAfp(
                alderAar = 62,
                totaltAfpBeloep = 890,
                tidligereArbeidsinntekt = 123,
                grunnbeloep = 456,
                sluttpoengtall = 7.8,
                trygdetid = 30,
                poengaarTom1991 = 10,
                poengaarFom1992 = 20,
                grunnpensjon = 567,
                tilleggspensjon = 678,
                afpTillegg = 789,
                saertillegg = 890,
                afpGrad = 80,
                afpAvkortetTil70Prosent = false
            ),
            privatAfpListe = listOf(
                NavPrivatAfpV3(
                    alderAar = 66,
                    beloep = 789,
                    kompensasjonstillegg = 123,
                    kronetillegg = 5,
                    livsvarig = 93,
                    maanedligBeloep = 100
                )
            ),
            livsvarigOffentligAfpListe = listOf(
                NavLivsvarigOffentligAfpV3(
                    alderAar = 63,
                    beloep = 901,
                    maanedligBeloep = 100
                )
            ),
            vilkaarsproeving = NavVilkaarsproevingResultatV3(
                vilkaarErOppfylt = false, // since alternativ exists
                alternativ = NavAlternativtResultatV3(
                    gradertUttakAlder = NavAlderV3(aar = 66, maaneder = 2),
                    uttaksgrad = 20,
                    heltUttakAlder = NavAlderV3(aar = 67, maaneder = 0),
                )
            ),
            tilstrekkeligTrygdetidForGarantipensjon = true,
            trygdetid = 39,
            opptjeningGrunnlagListe = listOf(NavOpptjeningGrunnlagV3(aar = 1999, pensjonsgivendeInntektBeloep = 1002))
        )
    }

    /**
     * Test av mapping av maanedsbeloep ved ugradert uttak.
     * Forventet resultat:
     * - alderspensjonMaanedsbeloep heltUttakBeloep skal være større enn 0
     * - alderspensjonMaanedsbeloep gradertUttakBeloep skal være null
     */
    test("toDto for ugradert maanedsbeloep") {
        NavSimuleringResultMapperV3.toDto(
            SimulertPensjonEllerAlternativ(
                pensjon = SimulertPensjon(
                    alderspensjon = emptyList(),
                    alderspensjonFraFolketrygden = listOf(
                        SimulertAlderspensjonFraFolketrygden(
                            datoFom = LocalDate.of(2021, 1, 1),
                            delytelseListe = emptyList(),
                            uttakGrad = 100, // => ugradert
                            maanedligBeloep = 678 // skal mappes til heltUttakBeloep
                        )
                    ),
                    privatAfp = emptyList(),
                    pre2025OffentligAfp = null,
                    livsvarigOffentligAfp = emptyList(),
                    pensjonBeholdningPeriodeListe = emptyList(),
                    harUttak = true,
                    harTilstrekkeligTrygdetid = true,
                    trygdetid = 0,
                    opptjeningGrunnlagListe = emptyList()
                ),
                alternativ = null
            )
        ).alderspensjonMaanedsbeloep shouldBe NavMaanedsbeloepV3(
            gradertUttakBeloep = null, // ugradert
            heltUttakBeloep = 678
        )
    }
})
