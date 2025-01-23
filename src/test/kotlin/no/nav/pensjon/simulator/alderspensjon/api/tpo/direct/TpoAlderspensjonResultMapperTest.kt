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
                            pensjonBeholdningFoerUttak = 1000000
                        )
                    ),
                    alderspensjonFraFolketrygden = listOf(
                        SimulertAlderspensjonFraFolketrygden(
                            datoFom = LocalDate.of(2030, 1, 2),
                            delytelseListe = listOf(
                                SimulertDelytelse(type = YtelseskomponentTypeEnum.GAP, beloep = 1234)
                            ),
                            uttakGrad = 50
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
})
