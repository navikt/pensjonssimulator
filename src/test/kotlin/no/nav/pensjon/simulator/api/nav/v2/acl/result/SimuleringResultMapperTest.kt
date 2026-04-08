package no.nav.pensjon.simulator.api.nav.v2.acl.result

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.trygdetid.Trygdetid

class SimuleringResultMapperTest : ShouldSpec({

    should("map empty result to DTO with empty lists") {
        SimuleringResultMapper.toDto(
            source = SimulertPensjonEllerAlternativ(
                pensjon = null,
                alternativ = null,
                problem = null
            )
        ) shouldBe SimuleringResultDto(
            alderspensjonListe = emptyList(),
            alderspensjonMaanedsbeloep = UttaksbeloepDto(gradertUttakBeloep = null, heltUttakBeloep = 0),
            maanedligAlderspensjonForKnekkpunkter = null,
            livsvarigOffentligAfpListe = emptyList(),
            tidsbegrensetOffentligAfp = null,
            privatAfpListe = emptyList(),
            primaerTrygdetid = null,
            vilkaarsproevingsresultat = VilkaarsproevingsresultatDto(
                erInnvilget = true, // siden alternativ = null
                alternativ = null
            ),
            pensjonsgivendeInntektListe = emptyList(),
            problem = null
        )
    }

    should("map alderspensjon to DTO") {
        SimuleringResultMapper.toDto(
            source = SimulertPensjonEllerAlternativ(
                pensjon = SimulertPensjon(
                    alderspensjon = listOf(
                        SimulertAarligAlderspensjon(
                            alderAar = 65,
                            beloep = 123,
                            inntektspensjon = 124,
                            garantipensjon = SimulertGarantipensjon(aarligBeloep = 125, sats = 1.2),
                            garantitillegg = 126,
                            delingstall = 1.3,
                             pensjonBeholdningFoerUttak = 127,
                             andelsbroekKap19 = 1.4,
                            andelsbroekKap20 = 1.5,
                            sluttpoengtall = 1.6,
                            trygdetidKap19 = 19,
                            trygdetidKap20 = 20,
                            poengaarFoer92 = 130,
                            poengaarEtter91 = 131,
                            forholdstall = 1.7,
                            basispensjon = 132,
                            grunnpensjon = 133,
                            tilleggspensjon = 134,
                            restpensjon = 135,
                            pensjonstillegg = 136,
                            skjermingstillegg = 137,
                            kapittel19Gjenlevendetillegg = 138,
                            minstePensjonsnivaaSats = 1.8
                        )
                    ),
                    alderspensjonFraFolketrygden = emptyList(),
                    privatAfp = emptyList(),
                    pre2025OffentligAfp = null,
                    livsvarigOffentligAfp = emptyList(),
                    pensjonBeholdningPeriodeListe = emptyList(),
                    harUttak = true,
                    primaerTrygdetid = Trygdetid(kapittel19 = 19, kapittel20 = 20),
                    maanedligAlderspensjonForKnekkpunkter = SimulertMaanedligAlderspensjonForKnekkpunkter(
                        vedGradertUttak = null,
                        vedHeltUttak = SimulertMaanedligAlderspensjon(beloep = 0, inntektspensjon = null, delingstall = null, pensjonBeholdningFoerUttak = null, pensjonBeholdningEtterUttak = null, sluttpoengtall = null, poengaarFoer92 = null, poengaarEtter91 = null, forholdstall = null, grunnpensjon = null, tilleggspensjon = null, pensjonstillegg = null, skjermingstillegg = null, andelsbroekKap19 = null, andelsbroekKap20 = null, basispensjon = null, restpensjon = null, gjenlevendetillegg = null, minstePensjonsnivaaSats = null, trygdetidKap19 = null, trygdetidKap20 = null, garantipensjon = null, garantitillegg = null),
                        vedNormertPensjonsalder = SimulertMaanedligAlderspensjon(beloep = 0, inntektspensjon = null, delingstall = null, pensjonBeholdningFoerUttak = null, pensjonBeholdningEtterUttak = null, sluttpoengtall = null, poengaarFoer92 = null, poengaarEtter91 = null, forholdstall = null, grunnpensjon = null, tilleggspensjon = null, pensjonstillegg = null, skjermingstillegg = null, andelsbroekKap19 = null, andelsbroekKap20 = null, basispensjon = null, restpensjon = null, gjenlevendetillegg = null, minstePensjonsnivaaSats = null, trygdetidKap19 = null, trygdetidKap20 = null, garantipensjon = null, garantitillegg = null)
                    ),
                    opptjeningGrunnlagListe = emptyList()
                ),
                alternativ = null,
                problem = null
            )
        ) shouldBe SimuleringResultDto(
            alderspensjonListe = listOf(
                AlderspensjonDto(
                    alderAar = 65,
                    beloep = 123,
                    inntektspensjon = 124,
                    delingstall = 1.3,
                    pensjonsbeholdningFoerUttak = 127,
                    sluttpoengtall = 1.6,
                    poengaarFoer92 = 130,
                    poengaarEtter91 = 131,
                    forholdstall = 1.7,
                    grunnpensjon = 133,
                    tilleggspensjon = 134,
                    pensjonstillegg = 136,
                    skjermingstillegg = 137,
                    kapittel19Pensjon = Kapittel19PensjonDto(
                        andelsbroek = 1.4,
                        trygdetidAntallAar = 19,
                        basispensjon = 132,
                        restpensjon = 135,
                        gjenlevendetillegg = 138,
                        minstePensjonsnivaaSats = 1.8
                    ),
                    kapittel20Pensjon = Kapittel20PensjonDto(
                        andelsbroek = 1.5,
                        trygdetidAntallAar = 20,
                        garantipensjon = GarantipensjonDto(aarligBeloep = 125, maanedligBeloep = null, sats = 1.2),
                        garantitillegg = 126
                    )
                )
            ),
            alderspensjonMaanedsbeloep = UttaksbeloepDto(
                gradertUttakBeloep = null,
                heltUttakBeloep = 0
            ),
            maanedligAlderspensjonForKnekkpunkter = MaanedligAlderspensjonForKnekkpunkter(
                vedGradertUttak = null,
                vedHeltUttak = MaanedligAlderspensjon(beloep = 0, inntektspensjon = null, delingstall = null, pensjonsbeholdningFoerUttak = null, pensjonsbeholdningEtterUttak = null, sluttpoengtall = null, poengaarFoer92 = null, poengaarEtter91 = null, forholdstall = null, grunnpensjon = null, tilleggspensjon = null, pensjonstillegg = null, skjermingstillegg = null, kapittel19Pensjon = Kapittel19PensjonDto(andelsbroek = null, trygdetidAntallAar = 0, basispensjon = null, restpensjon = null, gjenlevendetillegg = null, minstePensjonsnivaaSats = null), kapittel20Pensjon = Kapittel20PensjonDto(andelsbroek = null, trygdetidAntallAar = 0, garantipensjon = null, garantitillegg = null)),
                vedNormertPensjonsalder = MaanedligAlderspensjon(beloep = 0, inntektspensjon = null, delingstall = null, pensjonsbeholdningFoerUttak = null, pensjonsbeholdningEtterUttak = null, sluttpoengtall = null, poengaarFoer92 = null, poengaarEtter91 = null, forholdstall = null, grunnpensjon = null, tilleggspensjon = null, pensjonstillegg = null, skjermingstillegg = null, kapittel19Pensjon = Kapittel19PensjonDto(andelsbroek = null, trygdetidAntallAar = 0, basispensjon = null, restpensjon = null, gjenlevendetillegg = null, minstePensjonsnivaaSats = null), kapittel20Pensjon = Kapittel20PensjonDto(andelsbroek = null, trygdetidAntallAar = 0, garantipensjon = null, garantitillegg = null))
            ),
            livsvarigOffentligAfpListe = emptyList(),
            tidsbegrensetOffentligAfp = null,
            privatAfpListe = emptyList(),
            primaerTrygdetid = TrygdetidDto(
                antallAar = 20, // høyeste av trygdetid for kapittel 19 og kapittel 20
                erUtilstrekkelig = false
            ),
            vilkaarsproevingsresultat = VilkaarsproevingsresultatDto(erInnvilget = true, alternativ = null),
            pensjonsgivendeInntektListe = emptyList(),
            problem = null
        )
    }
})
