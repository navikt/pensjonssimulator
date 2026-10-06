package no.nav.pensjon.simulator.opptjening

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.testutil.TestObjects
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class OpptjeningMedBeholdningServiceTest : ShouldSpec({

    context("gjelder ikke søker, nytt regelverk, relevant sakstype, skal hente pensjonspoeng") {
        should("beholde år etter siste gyldige opptjeningsår") {
            val ikkeSoekerPid = Pid("12906498357")

            val personSpecListe = personSpecListe(
                pid = ikkeSoekerPid,
                sisteGyldigeOpptjeningsaar = 2025,
                gjelderSoeker = false // gjelder ikke søker
            )

            val result = OpptjeningMedBeholdningService(
                opptjeningService = arrangeOpptjening(
                    listOf(
                        pensjonspoeng(aar = 2024),
                        pensjonspoeng(aar = 2025),
                        pensjonspoeng(aar = 2026) // år etter siste gyldige opptjeningsår - skal beholdes
                    )
                ),
                personService = mockk(relaxed = true),
                time = mockk()
            ).pensjonsopptjening(
                OpptjeningMedBeholdningSpec(
                    pid = ikkeSoekerPid, // gjelder ikke søker
                    hentPensjonspoeng = true, // skal hente pensjonspoeng
                    hentOpptjeningsgrunnlag = true,
                    hentBeholdninger = false,
                    harUfoeretrygdKravlinje = false,
                    regelverkType = RegelverkTypeEnum.N_REG_G_OPPTJ, // nytt regelverk
                    sakType = SakTypeEnum.ALDER, // relevant sakstype
                    personSpecListe = personSpecListe,
                    soekerSpec = personSpecListe(sisteGyldigeOpptjeningsaar = 2025, gjelderSoeker = true)[0]
                )
            )

            with(result) {
                opptjeningsgrunnlagListe shouldHaveSize 3
                with(opptjeningsgrunnlagListe[0]) {
                    ar shouldBe 2024
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
                with(opptjeningsgrunnlagListe[1]) {
                    ar shouldBe 2025
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
                with(opptjeningsgrunnlagListe[2]) {
                    ar shouldBe 2026 // år beholdt
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
            }
        }
    }

    context("gammelt regelverk, gjelder søker, relevant sakstype, skal hente pensjonspoeng") {
        should("beholde år etter siste gyldige opptjeningsår") {
            val personSpecListe = personSpecListe(sisteGyldigeOpptjeningsaar = 2025, gjelderSoeker = true)

            val result = OpptjeningMedBeholdningService(
                opptjeningService = arrangeOpptjening(
                    listOf(
                        pensjonspoeng(aar = 2024),
                        pensjonspoeng(aar = 2025),
                        pensjonspoeng(aar = 2026) // år etter siste gyldige opptjeningsår - skal beholdes
                    )
                ),
                personService = mockk(relaxed = true),
                time = mockk()
            ).pensjonsopptjening(
                OpptjeningMedBeholdningSpec(
                    pid = pid, // gjelder søker
                    hentPensjonspoeng = true, // skal hente pensjonspoeng
                    hentOpptjeningsgrunnlag = true,
                    hentBeholdninger = false,
                    harUfoeretrygdKravlinje = false,
                    regelverkType = RegelverkTypeEnum.G_REG, // gammelt regelverk
                    sakType = SakTypeEnum.ALDER, // relevant sakstype
                    personSpecListe = personSpecListe,
                    soekerSpec = personSpecListe[0]
                )
            )

            with(result) {
                opptjeningsgrunnlagListe shouldHaveSize 3
                with(opptjeningsgrunnlagListe[0]) {
                    ar shouldBe 2024
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
                with(opptjeningsgrunnlagListe[1]) {
                    ar shouldBe 2025
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
                with(opptjeningsgrunnlagListe[2]) {
                    ar shouldBe 2026 // år beholdt
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
            }
        }
    }

    context("nytt regelverk, gjelder søker, relevant sakstype, skal hente pensjonspoeng") {
        should("fjerne år etter siste gyldige opptjeningsår") {
            val personSpecListe = personSpecListe(sisteGyldigeOpptjeningsaar = 2025)

            val result = OpptjeningMedBeholdningService(
                opptjeningService = arrangeOpptjening(
                    listOf(
                        pensjonspoeng(aar = 2024),
                        pensjonspoeng(aar = 2025),
                        pensjonspoeng(aar = 2026) // år etter siste gyldige opptjeningsår - skal fjernes
                    )
                ),
                personService = mockk(relaxed = true),
                time = mockk()
            ).pensjonsopptjening(
                OpptjeningMedBeholdningSpec(
                    pid = pid, // gjelder søker
                    hentPensjonspoeng = true, // skal hente pensjonspoeng
                    hentOpptjeningsgrunnlag = true,
                    hentBeholdninger = false,
                    harUfoeretrygdKravlinje = false,
                    regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ, // nytt regelverk
                    sakType = SakTypeEnum.AFP_PRIVAT, // relevant sakstype
                    personSpecListe = personSpecListe,
                    soekerSpec = personSpecListe[0]
                )
            )

            with(result) {
                opptjeningsgrunnlagListe shouldHaveSize 2
                with(opptjeningsgrunnlagListe[0]) {
                    ar shouldBe 2024
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
                with(opptjeningsgrunnlagListe[1]) {
                    ar shouldBe 2025
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
                // år 2026 fjernet
            }
        }
    }


    context("tom opptjeningstypeliste, opptjeningstype er 'Pensjonspoeng for pensjonsgivende inntekt'") {
        context("skal hente pensjonspoeng og opptjeningsgrunnlag, har uføretrygd-kravlinje") {
            should("legge til opptjeningstyper for inntektsår") {
                val personSpecListe = personSpecListe()
                val result = OpptjeningMedBeholdningService(
                    opptjeningService = arrangeOpptjening(
                        listOf(
                            pensjonspoeng(aar = 2021), // opptjeningstype er 'Pensjonspoeng for pensjonsgivende inntekt'
                            pensjonspoeng(aar = 2022), // ingen inntekt dette året, dermed skal ingen opptjeningstyper legges til
                        )
                    ),
                    personService = mockk(relaxed = true),
                    time = mockk()
                ).pensjonsopptjening(
                    OpptjeningMedBeholdningSpec(
                        pid = pid,
                        hentPensjonspoeng = true, // skal hente pensjonspoeng
                        hentOpptjeningsgrunnlag = true, // skal hente opptjeningsgrunnlag
                        hentBeholdninger = false,
                        harUfoeretrygdKravlinje = true, // har uføretrygd-kravlinje
                        regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
                        sakType = SakTypeEnum.ALDER,
                        personSpecListe = personSpecListe,
                        soekerSpec = personSpecListe[0]
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

        context("skal hente pensjonspoeng og opptjeningsgrunnlag, har ikke uføretrygd-kravlinje") {
            should("ikke legge til opptjeningstyper for inntektsår") {
                val personSpecListe = personSpecListe()
                val result = OpptjeningMedBeholdningService(
                    opptjeningService = arrangeOpptjening(listOf(pensjonspoeng(aar = 2021))),
                    personService = mockk(relaxed = true),
                    time = mockk()
                ).pensjonsopptjening(
                    OpptjeningMedBeholdningSpec(
                        pid = pid,
                        hentPensjonspoeng = true, // skal hente pensjonspoeng
                        hentOpptjeningsgrunnlag = true, // skal hente opptjeningsgrunnlag
                        hentBeholdninger = false,
                        harUfoeretrygdKravlinje = false, // har ikke uføretrygd-kravlinje
                        regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
                        sakType = SakTypeEnum.ALDER,
                        personSpecListe = personSpecListe,
                        soekerSpec = personSpecListe[0]
                    )
                )

                with(result) {
                    opptjeningsgrunnlagListe shouldHaveSize 1
                    with(opptjeningsgrunnlagListe[0]) {
                        opptjeningTypeListe shouldHaveSize 0 // opptjeningstyper ikke lagt til
                    }
                }
            }
        }

        context("skal hente pensjonspoeng men ikke ikke opptjeningsgrunnlag, har uføretrygd-kravlinje") {
            should("gi feilmelding") {
                val personSpecListe = personSpecListe()
                shouldThrow<InvalidArgumentException> {
                    OpptjeningMedBeholdningService(
                        opptjeningService = arrangeOpptjening(listOf(pensjonspoeng(aar = 2021))),
                        personService = mockk(relaxed = true),
                        time = mockk()
                    ).pensjonsopptjening(
                        OpptjeningMedBeholdningSpec(
                            pid = pid,
                            hentPensjonspoeng = true, // skal hente pensjonspoeng
                            hentOpptjeningsgrunnlag = false, // skal ikke hente opptjeningsgrunnlag
                            hentBeholdninger = false,
                            harUfoeretrygdKravlinje = true, // har uføretrygd-kravlinje
                            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
                            sakType = SakTypeEnum.ALDER,
                            personSpecListe = personSpecListe,
                            soekerSpec = personSpecListe[0]
                        )
                    )
                }.message shouldBe "I spesifikasjonen for opptjening med beholdning er det angitt at personen har" +
                        " en uføretrygd-kravlinje og at pensjonspoeng skal hentes" +
                        " - da må det også angis at opptjeningsgrunnlag skal hentes"
            }
        }
    }
})

private fun personSpecListe(
    pid: Pid = TestObjects.pid,
    sisteGyldigeOpptjeningsaar: Int = 2024,
    gjelderSoeker: Boolean = true,
): List<OpptjeningMedBeholdningPersonSpec> =
    listOf(OpptjeningMedBeholdningPersonSpec(pid, sisteGyldigeOpptjeningsaar, gjelderSoeker))

private fun arrangeOpptjening(pensjonspoengListe: List<Opptjeningsgrunnlag>): OpptjeningService =
    mockk {
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
        } returns pensjonspoengListe
    }

private fun pensjonspoeng(aar: Int) =
    Opptjeningsgrunnlag().apply {
        ar = aar
        opptjeningTypeEnum = OpptjeningtypeEnum.PPI // Pensjonspoeng for pensjonsgivende inntekt
        opptjeningTypeListe = mutableListOf()
    }