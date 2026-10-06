package no.nav.pensjon.simulator.opptjening

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class OpptjeningMedBeholdningServiceTest : ShouldSpec({

    context("tom opptjeningstypeliste, opptjeningstype er 'PPI', skal hente opptjeningsgrunnlag, har uføretrygd-kravlinje") {
        should("legge til opptjeningstyper for inntektsår") {
            val result = OpptjeningMedBeholdningService(
                opptjeningService = mockk(relaxed = true) {
                    every {
                        hentOpptjeningsgrunnlagSamling(any(), any())
                    } returns OpptjeningsgrunnlagSamling(
                        pid = pid,
                        inntektListe = listOf(
                            Inntektsgrunnlag().apply {
                                fomLd = LocalDate.of(2021, 1, 1)
                                inntektTypeEnum = InntekttypeEnum.ARBLIGN
                                poppOpptjeningType = OpptjeningPOPPTypeEnum.PI66
                            },
                            Inntektsgrunnlag().apply {
                                fomLd = LocalDate.of(2021, 2, 1)
                                inntektTypeEnum = InntekttypeEnum.AI
                                poppOpptjeningType = OpptjeningPOPPTypeEnum.AI
                            },
                            Inntektsgrunnlag().apply {
                                fomLd = LocalDate.of(2021, 3, 1)
                                inntektTypeEnum = InntekttypeEnum.FPI
                                poppOpptjeningType = OpptjeningPOPPTypeEnum.FL_PGI_LOENN
                            }
                        ),
                        omsorgListe = emptyList(),
                        dagpengeListe = emptyList(),
                        foerstegangstjeneste = null
                    )

                    every {
                        hentPensjonspoeng(any(), any(), any(), any())
                    } returns listOf(
                        Opptjeningsgrunnlag().apply {
                            ar = 2021
                            opptjeningTypeEnum = OpptjeningtypeEnum.PPI
                            opptjeningTypeListe = mutableListOf()
                        },
                        Opptjeningsgrunnlag().apply {
                            ar = 2022 // ingen inntekt dette året, dermed skal ingen opptjeningstyper legges til
                            opptjeningTypeEnum = OpptjeningtypeEnum.PPI
                            opptjeningTypeListe = mutableListOf()
                        }
                    )
                },
                personService = mockk(relaxed = true),
                time = mockk()
            ).pensjonsopptjening(
                OpptjeningMedBeholdningSpec(
                    pid = pid,
                    hentPensjonspoeng = true,
                    hentOpptjeningsgrunnlag = true,
                    hentBeholdninger = false,
                    harUfoeretrygdKravlinje = true,
                    regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
                    sakType = SakTypeEnum.ALDER,
                    personSpecListe = listOf(
                        OpptjeningMedBeholdningPersonSpec(
                            pid = pid,
                            sisteGyldigeOpptjeningsaar = 2024,
                            gjelderSoeker = true
                        )
                    ),
                    soekerSpec = OpptjeningMedBeholdningPersonSpec(
                        pid = pid,
                        sisteGyldigeOpptjeningsaar = 2024,
                        gjelderSoeker = true
                    )
                )
            )

            with(result) {
                opptjeningsgrunnlagListe shouldHaveSize 2
                with(opptjeningsgrunnlagListe[0]) {
                    opptjeningTypeListe shouldHaveSize 3
                    opptjeningTypeListe[0].opptjeningPOPPTypeEnum shouldBe OpptjeningPOPPTypeEnum.PI66
                    opptjeningTypeListe[1].opptjeningPOPPTypeEnum shouldBe OpptjeningPOPPTypeEnum.AI
                    opptjeningTypeListe[2].opptjeningPOPPTypeEnum shouldBe OpptjeningPOPPTypeEnum.FL_PGI_LOENN
                }
                with(opptjeningsgrunnlagListe[1]) {
                    opptjeningTypeListe shouldHaveSize 0
                }
            }
        }
    }
})