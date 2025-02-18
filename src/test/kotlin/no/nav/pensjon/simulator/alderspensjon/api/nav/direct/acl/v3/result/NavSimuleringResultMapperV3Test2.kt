package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result

import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.core.beholdning.OpptjeningGrunnlag
import org.junit.jupiter.api.Test
import java.time.LocalDate

// Copied from PEN
// TODO use FunSpec
class NavSimuleringResultMapperV3Test2 {

    @Test
    fun `map resultat med gradert uttak`() {
        val source = simulertPensjonEllerAlternativ(
            listOf(
                alderspensjonFraFolketrygden(100, 7),
                alderspensjonFraFolketrygden(50, 8),
            )
        )

        val result: NavSimuleringResultV3 = NavSimuleringResultMapperV3.toDto(source)

        with(result.alderspensjonMaanedsbeloep!!) {
            gradertUttakBeloep shouldBe 8
            heltUttakBeloep shouldBe 7
        }
    }

    @Test
    fun `map resultat med helt uttak`() {
        val source = simulertPensjonEllerAlternativ(listOf(alderspensjonFraFolketrygden(100, 7)))

        val result: NavSimuleringResultV3 = NavSimuleringResultMapperV3.toDto(source)

        with(result.alderspensjonMaanedsbeloep!!) {
            gradertUttakBeloep shouldBe null
            heltUttakBeloep shouldBe 7
        }
        assertResult(result)
    }

    @Test
    fun `map resultat uten helt uttak skal sette haandtere ingen maanedsbeloep`() {
        val source = simulertPensjonEllerAlternativ(emptyList())

        val result: NavSimuleringResultV3 = NavSimuleringResultMapperV3.toDto(source)

        with(result.alderspensjonMaanedsbeloep!!) {
            gradertUttakBeloep shouldBe null
            heltUttakBeloep shouldBe 0
        }
        assertResult(result)
    }

    @Test
    fun `map resultat skal velge foerste i listen med flere perioder med samme uttaksgrad`() {
        val source = simulertPensjonEllerAlternativ(
            listOf(
                alderspensjonFraFolketrygden(grad = 50, beloep = 1),
                alderspensjonFraFolketrygden(grad = 50, beloep = 2),
                alderspensjonFraFolketrygden(grad = 100, beloep = 3),
                alderspensjonFraFolketrygden(grad = 100, beloep = 4),
            )
        )

        val result: NavSimuleringResultV3 = NavSimuleringResultMapperV3.toDto(source)

        with(result.alderspensjonMaanedsbeloep!!) {
            gradertUttakBeloep shouldBe 1
            heltUttakBeloep shouldBe 3
        }
        assertResult(result)
    }

    private companion object {

        private fun assertResult(result: NavSimuleringResultV3) {
            result.alderspensjonListe.size shouldBe 1
            with(result.alderspensjonListe[0]) {
                alderAar shouldBe 1
                beloep shouldBe 2
                inntektspensjon shouldBe 3
                garantipensjon shouldBe 4
                delingstall shouldBe 5.0
                pensjonBeholdningFoerUttak shouldBe 6
                andelsbroekKap19 shouldBe 0.3
                andelsbroekKap20 shouldBe 0.7
                sluttpoengtall shouldBe 9.0
                trygdetidKap19 shouldBe 10
                trygdetidKap20 shouldBe 11
                poengaarFoer92 shouldBe 12
                poengaarEtter91 shouldBe 13
                forholdstall shouldBe 14.0
                grunnpensjon shouldBe 15
                tilleggspensjon shouldBe 16
                pensjonstillegg shouldBe 17
                skjermingstillegg shouldBe 18
            }

            result.privatAfpListe.size shouldBe 1
            with(result.privatAfpListe[0]) {
                alderAar shouldBe 9
                beloep shouldBe 10
            }

            result.livsvarigOffentligAfpListe.size shouldBe 1
            with(result.livsvarigOffentligAfpListe[0]) {
                alderAar shouldBe 13
                beloep shouldBe 14
            }

            result.vilkaarsproeving.alternativ shouldBe null
            result.vilkaarsproeving.vilkaarErOppfylt shouldBe true
            result.tilstrekkeligTrygdetidForGarantipensjon shouldBe true
            result.trygdetid shouldBe 21

            result.opptjeningGrunnlagListe.size shouldBe 1
            with(result.opptjeningGrunnlagListe[0]) {
                aar shouldBe 22
                pensjonsgivendeInntektBeloep shouldBe 23
            }
        }

        private fun simulertPensjonEllerAlternativ(alderspensjonFraFolketrygden: List<SimulertAlderspensjonFraFolketrygden>) =
            SimulertPensjonEllerAlternativ(
                pensjon = SimulertPensjon(
                    alderspensjon = listOf(
                        SimulertAarligAlderspensjon(
                            alderAar = 1,
                            beloep = 2,
                            inntektspensjon = 3,
                            garantipensjon = 4,
                            delingstall = 5.0,
                            pensjonBeholdningFoerUttak = 6,
                            andelsbroekKap19 = 0.3,
                            andelsbroekKap20 = 0.7,
                            sluttpoengtall = 9.0,
                            trygdetidKap19 = 10,
                            trygdetidKap20 = 11,
                            poengaarFoer92 = 12,
                            poengaarEtter91 = 13,
                            forholdstall = 14.0,
                            grunnpensjon = 15,
                            tilleggspensjon = 16,
                            pensjonstillegg = 17,
                            skjermingstillegg = 18
                        )
                    ),
                    alderspensjonFraFolketrygden,
                    privatAfp = listOf(
                        SimulertPrivatAfp(
                            alderAar = 9,
                            beloep = 10
                        )
                    ),
                    pre2025OffentligAfp = SimulertPre2025OffentligAfp(
                        alderAar = 11,
                        beloep = 12
                    ),
                    livsvarigOffentligAfp = listOf(
                        SimulertLivsvarigOffentligAfp(
                            alderAar = 13,
                            beloep = 14
                        )
                    ),
                    pensjonBeholdningPeriodeListe = listOf(
                        SimulertPensjonBeholdningPeriode(
                            datoFom = LocalDate.now(),
                            pensjonBeholdning = 15.0,
                            garantipensjonBeholdning = 16.0,
                            garantitilleggBeholdning = 17.0,
                            garantipensjonNivaa = SimulertGarantipensjonNivaa(
                                beloep = 18.0,
                                satsType = "REGULERINGSFAKTOR",
                                sats = 19.0,
                                anvendtTrygdetid = 20,
                            )
                        )
                    ),
                    harUttak = true,
                    harNokTrygdetidForGarantipensjon = true,
                    trygdetid = 21,
                    opptjeningGrunnlagListe = listOf(
                        OpptjeningGrunnlag(
                            aar = 22,
                            pensjonsgivendeInntekt = 23
                        )
                    )
                ),
                alternativ = null
            )

        private fun alderspensjonFraFolketrygden(grad: Int, beloep: Int) =
            SimulertAlderspensjonFraFolketrygden(
                datoFom = LocalDate.now(),
                delytelseListe = emptyList(),
                uttakGrad = grad,
                maanedligBeloep = beloep
            )
    }
}
