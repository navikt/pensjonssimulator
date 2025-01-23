package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonFraFolketrygden
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonResult
import no.nav.pensjon.simulator.alderspensjon.PensjonDelytelse
import no.nav.pensjon.simulator.alderspensjon.PensjonType
import no.nav.pensjon.simulator.alderspensjon.Uttaksgrad
import no.nav.pensjon.simulator.core.beholdning.OpptjeningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import java.time.LocalDate

class TpoAlderspensjonResultMapperTest : FunSpec({

    test("mapPensjonEllerAlternativ for null should throw exception") {
        val exception = shouldThrow<RuntimeException> {
            TpoAlderspensjonResultMapper.mapPensjonEllerAlternativ(
                source = null,
                angittFoersteUttakFom = LocalDate.of(2030, 1, 2),
                angittAndreUttakFom = null
            )
        }

        exception.message shouldBe "Ingen alderspensjon fra folketrygden funnet for f.o.m.-dato 2030-01-02 blant []"
    }

    test("mapPensjonEllerAlternativ for null should throw exception that describes simulert alderspensjon entries") {
        val exception = shouldThrow<RuntimeException> {
            TpoAlderspensjonResultMapper.mapPensjonEllerAlternativ(
                source = SimulertPensjonEllerAlternativ(
                    SimulertPensjon(
                        alderspensjon = emptyList(),
                        alderspensjonFraFolketrygden = listOf(
                            SimulertAlderspensjonFraFolketrygden(
                                datoFom = LocalDate.of(2030, 1, 2),
                                delytelseListe = listOf(
                                    SimulertDelytelse(type = YtelseskomponentTypeEnum.GAP, beloep = 1234)
                                ),
                                uttakGrad = 50
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
                angittFoersteUttakFom = LocalDate.of(2030, 2, 1),
                angittAndreUttakFom = null
            )
        }

        exception.message shouldBe "Ingen alderspensjon fra folketrygden funnet for f.o.m.-dato 2030-02-01 blant" +
                " [SimulertAlderspensjonFraFolketrygden(datoFom=2030-01-02," +
                " delytelseListe=[SimulertDelytelse(type=GAP, beloep=1234)], uttakGrad=50)]"
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
})
