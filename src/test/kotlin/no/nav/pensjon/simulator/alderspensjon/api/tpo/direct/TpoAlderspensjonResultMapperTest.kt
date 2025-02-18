package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.*
import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.core.beholdning.OpptjeningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import java.time.LocalDate

class TpoAlderspensjonResultMapperTest : FunSpec({

    test("mapPensjonEllerAlternativ for null resulterer i tomt resultat") {
        TpoAlderspensjonResultMapper.mapPensjonEllerAlternativ(
            source = null,
            angittFoersteUttakFom = LocalDate.of(2030, 1, 1),
            angittAndreUttakFom = null
        ) shouldBe AlderspensjonResult(
            simuleringSuksess = false,
            aarsakListeIkkeSuksess = emptyList(),
            alderspensjon = emptyList(),
            forslagVedForLavOpptjening = null,
            harUttak = false
        )
    }

    test("mapPensjonEllerAlternativ for innvilget should return alderspensjon result") {
        TpoAlderspensjonResultMapper.mapPensjonEllerAlternativ(
            source = SimulertPensjonEllerAlternativ(
                SimulertPensjon(
                    alderspensjon = listOf(
                        SimulertAarligAlderspensjon(
                            alderAar = 65,
                            beloep = 1000,
                            inntektspensjon = 500,
                            garantipensjon = 400,
                            delingstall = 1.2,
                            pensjonBeholdningFoerUttak = 1000000,
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
                            skjermingstillegg = 890
                        )
                    ),
                    alderspensjonFraFolketrygden = listOf(
                        SimulertAlderspensjonFraFolketrygden(
                            datoFom = LocalDate.of(2030, 1, 2),
                            delytelseListe = listOf(
                                SimulertDelytelse(type = YtelseskomponentTypeEnum.GAP, beloep = 1234)
                            ),
                            uttakGrad = 50,
                            maanedligBeloep = 500
                        )
                    ),
                    privatAfp = listOf(SimulertPrivatAfp(alderAar = 63, beloep = 5000)),
                    pre2025OffentligAfp = null,
                    livsvarigOffentligAfp = listOf(SimulertLivsvarigOffentligAfp(alderAar = 66, beloep = 6000)),
                    pensjonBeholdningPeriodeListe = listOf(
                        SimulertPensjonBeholdningPeriode(
                            pensjonBeholdning = 100.2,
                            garantipensjonBeholdning = 200.3,
                            garantitilleggBeholdning = 300.4,
                            datoFom = LocalDate.of(2021, 1, 1),
                            garantipensjonNivaa = SimulertGarantipensjonNivaa(
                                beloep = 1.2,
                                satsType = "type1",
                                sats = 1.2,
                                anvendtTrygdetid = 35,
                            )
                        )
                    ),
                    harUttak = true,
                    harNokTrygdetidForGarantipensjon = true,
                    trygdetid = 40,
                    opptjeningGrunnlagListe = listOf(
                        OpptjeningGrunnlag(aar = 2024, pensjonsgivendeInntekt = 50000)
                    )
                ),
                alternativ = null
            ),
            angittFoersteUttakFom = LocalDate.of(2030, 1, 2),
            angittAndreUttakFom = null
        ) shouldBe AlderspensjonResult(
            simuleringSuksess = true,
            aarsakListeIkkeSuksess = emptyList(),
            alderspensjon = listOf(
                AlderspensjonFraFolketrygden(
                    fom = LocalDate.of(2030, 1, 2),
                    delytelseListe = listOf(PensjonDelytelse(pensjonType = PensjonType.GARANTIPENSJON, beloep = 1234)),
                    uttaksgrad = Uttaksgrad.FEMTI_PROSENT
                )
            ),
            forslagVedForLavOpptjening = null,
            harUttak = true
        )
    }

    test("mapPensjonEllerAlternativ plukker ut pensjon for første uttaksdato") {
        TpoAlderspensjonResultMapper.mapPensjonEllerAlternativ(
            source = SimulertPensjonEllerAlternativ(
                SimulertPensjon(
                    alderspensjon = emptyList(),
                    alderspensjonFraFolketrygden = listOf(
                        SimulertAlderspensjonFraFolketrygden(
                            datoFom = LocalDate.of(2031, 1, 1), // matches angittFoersteUttakFom
                            delytelseListe = emptyList(),
                            uttakGrad = 100,
                            maanedligBeloep = 500
                        ),
                        SimulertAlderspensjonFraFolketrygden(
                            datoFom = LocalDate.of(2032, 1, 1), // no match
                            delytelseListe = emptyList(),
                            uttakGrad = 100,
                            maanedligBeloep = 600
                        )
                    ),
                    privatAfp = emptyList(),
                    pre2025OffentligAfp = null,
                    livsvarigOffentligAfp = emptyList(),
                    pensjonBeholdningPeriodeListe = emptyList(),
                    harUttak = true,
                    harNokTrygdetidForGarantipensjon = true,
                    trygdetid = 40,
                    opptjeningGrunnlagListe = emptyList()
                ),
                alternativ = null
            ),
            angittFoersteUttakFom = LocalDate.of(2031, 1, 1),
            angittAndreUttakFom = null
        ) shouldBe AlderspensjonResult(
            simuleringSuksess = true,
            aarsakListeIkkeSuksess = emptyList(),
            alderspensjon = listOf(
                AlderspensjonFraFolketrygden(
                    fom = LocalDate.of(2031, 1, 1),
                    delytelseListe = emptyList(),
                    uttaksgrad = Uttaksgrad.HUNDRE_PROSENT
                )
            ),
            forslagVedForLavOpptjening = null,
            harUttak = true
        )
    }

    test("mapPensjonEllerAlternativ plukker ut pensjon for begge uttaksdatoer når gradert") {
        TpoAlderspensjonResultMapper.mapPensjonEllerAlternativ(
            source = SimulertPensjonEllerAlternativ(
                SimulertPensjon(
                    alderspensjon = emptyList(),
                    alderspensjonFraFolketrygden = listOf(
                        SimulertAlderspensjonFraFolketrygden(
                            datoFom = LocalDate.of(2035, 6, 1), // matches angittAndreUttakFom
                            delytelseListe = emptyList(),
                            uttakGrad = 100,
                            maanedligBeloep = 500
                        ),
                        SimulertAlderspensjonFraFolketrygden(
                            datoFom = LocalDate.of(2032, 1, 1), // no match
                            delytelseListe = emptyList(),
                            uttakGrad = 60,
                            maanedligBeloep = 600
                        ),
                        SimulertAlderspensjonFraFolketrygden(
                            datoFom = LocalDate.of(2031, 1, 1), // matches angittFoersteUttakFom
                            delytelseListe = emptyList(),
                            uttakGrad = 80,
                            maanedligBeloep = 700
                        )
                    ),
                    privatAfp = emptyList(),
                    pre2025OffentligAfp = null,
                    livsvarigOffentligAfp = emptyList(),
                    pensjonBeholdningPeriodeListe = emptyList(),
                    harUttak = true,
                    harNokTrygdetidForGarantipensjon = true,
                    trygdetid = 40,
                    opptjeningGrunnlagListe = emptyList()
                ),
                alternativ = null
            ),
            angittFoersteUttakFom = LocalDate.of(2031, 1, 1),
            angittAndreUttakFom = LocalDate.of(2035, 6, 1)
        ) shouldBe AlderspensjonResult(
            simuleringSuksess = true,
            aarsakListeIkkeSuksess = emptyList(),
            alderspensjon = listOf(
                AlderspensjonFraFolketrygden(
                    fom = LocalDate.of(2031, 1, 1),
                    delytelseListe = emptyList(),
                    uttaksgrad = Uttaksgrad.AATTI_PROSENT
                ), AlderspensjonFraFolketrygden(
                    fom = LocalDate.of(2035, 6, 1),
                    delytelseListe = emptyList(),
                    uttaksgrad = Uttaksgrad.HUNDRE_PROSENT
                )
            ),
            forslagVedForLavOpptjening = null,
            harUttak = true
        )
    }
})
