package no.nav.pensjon.simulator.core.endring

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SpecialBeregningInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.person.PersongrunnlagMapper
import no.nav.pensjon.simulator.core.person.eps.EpsService
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagResult
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagService
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class EndringPersongrunnlagTest : ShouldSpec({

    /**
     * En persondetalj er irrelevant hvis enten:
     * - bruk = false, eller
     * - penRolleTom er i fortid
     * -----------------------------------------
     * NB: Interessant forskjell mellom EndringPersongrunnlag og Pre2025OffentligAfpPersongrunnlag:
     * - EndringPersongrunnlag bruker penRolleTom
     * - Pre2025OffentligAfpPersongrunnlag bruker virkTom
     */
    should("fjerne irrelevante persondetaljer") {
        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = arrangeKrav(), // kravet inneholder irrelevante persondetaljer
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = simuleringSpec(),
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        with(persongrunnlag!!) {
            personDetaljListe shouldHaveSize 3
            personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
            personDetaljListe[1].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.FAR
            personDetaljListe[2].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.BARN
        }
    }

    should("returnere null når forrigeAlderspensjonBeregningResultat er null") {
        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = mockk(),
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = simuleringSpec(),
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = null
        )

        persongrunnlag shouldBe null
    }

    should("returnere null når kravId er null") {
        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = mockk(),
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = simuleringSpec(),
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = null }
        )

        persongrunnlag shouldBe null
    }

    should("sette bosattLandEnum til NOR") {
        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = arrangeKrav(),
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = simuleringSpec(),
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.bosattLandEnum shouldBe LandkodeEnum.NOR
    }

    should("sette fortsattMedlemFT til true i inngangOgEksportGrunnlag") {
        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = arrangeKrav(),
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = simuleringSpec(),
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.inngangOgEksportGrunnlag?.fortsattMedlemFT shouldBe true
    }

    should("opprette enke-persondetalj for ENDR_ALDER_M_GJEN") {
        val spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN)
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
                        personDetaljListe = mutableListOf(
                            PersonDetalj().apply {
                                bruk = true
                                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                                sivilstandTypeEnum = SivilstandEnum.ENKE
                                virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                            }
                        )
                    }
                )
            }
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.personDetaljListe shouldHaveSize 1
        persongrunnlag.personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.ENKE
    }

    should("opprette ny enke-persondetalj fra avdoed.doedDato når ingen eksisterende ENKE finnes for ENDR_ALDER_M_GJEN") {
        val avdoed = Avdoed(
            pid = Pid("12345678901"),
            antallAarUtenlands = 0,
            inntektFoerDoed = 0,
            doedDato = LocalDate.of(2023, 6, 15)
        )
        val spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN).copy(avdoed = avdoed)
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
                        personDetaljListe = mutableListOf(
                            PersonDetalj().apply {
                                bruk = true
                                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                                sivilstandTypeEnum = SivilstandEnum.GIFT // Not ENKE
                                virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                            }
                        )
                    }
                )
            }
        }
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.personDetaljListe shouldHaveSize 1
        with(persongrunnlag.personDetaljListe[0]) {
            sivilstandTypeEnum shouldBe SivilstandEnum.ENKE
            grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
            grunnlagKildeEnum shouldBe GrunnlagkildeEnum.BRUKER
            bruk shouldBe true
            penRolleFom shouldBe LocalDate.of(2023, 6, 15).toNorwegianDateAtNoon()
        }
    }

    should("opprette ny enke-persondetalj når eksisterende ENKE har bruk=false for ENDR_ALDER_M_GJEN") {
        val avdoed = Avdoed(
            pid = Pid("12345678901"),
            antallAarUtenlands = 0,
            inntektFoerDoed = 0,
            doedDato = LocalDate.of(2023, 6, 15)
        )
        val spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN).copy(avdoed = avdoed)
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
                        personDetaljListe = mutableListOf(
                            PersonDetalj().apply {
                                bruk = true // Need at least one SOKER with bruk=true for hentPersongrunnlagForSoker
                                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                                sivilstandTypeEnum = SivilstandEnum.GIFT
                                virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                            },
                            PersonDetalj().apply {
                                bruk = false // bruk=false makes this ENKE invalid
                                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                                sivilstandTypeEnum = SivilstandEnum.ENKE
                                virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                            }
                        )
                    }
                )
            }
        }
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.personDetaljListe shouldHaveSize 1
        // Should create new enke from avdoed.doedDato since existing ENKE has bruk=false
        persongrunnlag.personDetaljListe[0].penRolleFom shouldBe LocalDate.of(2023, 6, 15).toNorwegianDateAtNoon()
    }

    should("opprette ny enke-persondetalj når eksisterende ENKE er utenfor gyldig periode for ENDR_ALDER_M_GJEN") {
        val avdoed = Avdoed(
            pid = Pid("12345678901"),
            antallAarUtenlands = 0,
            inntektFoerDoed = 0,
            doedDato = LocalDate.of(2023, 6, 15)
        )
        val spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN).copy(avdoed = avdoed)
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
                        personDetaljListe = mutableListOf(
                            PersonDetalj().apply {
                                bruk = true
                                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                                sivilstandTypeEnum = SivilstandEnum.ENKE
                                virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                                virkTom = LocalDate.of(2024, 1, 1).toNorwegianDateAtNoon() // Before today (2025-01-01)
                            }
                        )
                    }
                )
            }
        }
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.personDetaljListe shouldHaveSize 1
        // Should create new enke from avdoed.doedDato since existing ENKE is not valid today
        persongrunnlag.personDetaljListe[0].penRolleFom shouldBe LocalDate.of(2023, 6, 15).toNorwegianDateAtNoon()
    }

    should("bruke eksisterende gyldig ENKE PersonDetalj for ENDR_ALDER_M_GJEN") {
        val avdoed = Avdoed(
            pid = Pid("12345678901"),
            antallAarUtenlands = 0,
            inntektFoerDoed = 0,
            doedDato = LocalDate.of(2023, 6, 15)
        )
        val eksisterendeEnkeVirkFom = LocalDate.of(2020, 5, 1).toNorwegianDateAtNoon()
        val spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN).copy(avdoed = avdoed)
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
                        personDetaljListe = mutableListOf(
                            PersonDetalj().apply {
                                bruk = true
                                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                                sivilstandTypeEnum = SivilstandEnum.ENKE
                                virkFom = eksisterendeEnkeVirkFom
                                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                            }
                        )
                    }
                )
            }
        }
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.personDetaljListe shouldHaveSize 1
        // Should use existing ENKE, so virkFom should match the existing one, not avdoed.doedDato
        persongrunnlag.personDetaljListe[0].virkFom shouldBe eksisterendeEnkeVirkFom
    }

    should("fjerne andre persondetaljer og beholde kun ENKE for ENDR_ALDER_M_GJEN") {
        val spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN)
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
                        personDetaljListe = mutableListOf(
                            PersonDetalj().apply {
                                bruk = true
                                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                                sivilstandTypeEnum = SivilstandEnum.GIFT
                                virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                            },
                            PersonDetalj().apply {
                                bruk = true
                                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                                sivilstandTypeEnum = SivilstandEnum.ENKE
                                virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                            },
                            PersonDetalj().apply {
                                bruk = true
                                grunnlagsrolleEnum = GrunnlagsrolleEnum.BARN
                                sivilstandTypeEnum = SivilstandEnum.UGIF
                                virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                            }
                        )
                    }
                )
            }
        }
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        // Should only have single ENKE PersonDetalj, others removed
        persongrunnlag!!.personDetaljListe shouldHaveSize 1
        persongrunnlag.personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.ENKE
    }

    should("sette flyktning fra spec for ENDR_ALDER_M_GJEN") {
        val spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN).copy(flyktning = true)
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
                        personDetaljListe = mutableListOf(
                            PersonDetalj().apply {
                                bruk = true
                                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                                sivilstandTypeEnum = SivilstandEnum.ENKE
                                virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                            }
                        )
                    }
                )
            }
        }
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.flyktning shouldBe true
    }

    // Tests for addPersongrunnlagForEpsToKravhode

    should("bruke epsService når isTpOrigSimulering er true") {
        val spec = simuleringSpec().copy(isTpOrigSimulering = true)
        val epsService = mockk<EpsService>(relaxed = true)
        val endringKravhode = Kravhode()

        EndringPersongrunnlag(
            context = mockk(),
            kravService = mockk(),
            beholdningService = mockk(),
            epsService = epsService,
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).addPersongrunnlagForEpsToKravhode(
            spec = spec,
            endringKravhode = endringKravhode,
            forrigeAlderspensjonBeregningResultat = null,
            grunnbeloep = 118620
        )

        verify { epsService.addPersongrunnlagForEpsToKravhode(spec, endringKravhode, 118620) }
    }

    should("legge til EPS persongrunnlag når gyldig EKTEF finnes") {
        val eksisterendeEps = Persongrunnlag().apply {
            penPerson = PenPerson(penPersonId = 2L)
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                    virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                    virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                    penRolleTom = null
                }
            )
            inntektsgrunnlagListe = mutableListOf()
        }
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(eksisterendeEps)
            }
        }
        val endringKravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val beregningsResultat = BeregningsResultatAlderspensjon2011().apply {
            kravId = 1L
            beregningsinformasjon = SpecialBeregningInformasjon(
                epsMottarPensjon = false,
                epsHarInntektOver2G = false,
                harGjenlevenderett = false
            )
        }

        EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).addPersongrunnlagForEpsToKravhode(
            spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER),
            endringKravhode = endringKravhode,
            forrigeAlderspensjonBeregningResultat = beregningsResultat,
            grunnbeloep = 118620
        )

        endringKravhode.persongrunnlagListe shouldHaveSize 1
        endringKravhode.persongrunnlagListe[0].penPerson?.penPersonId shouldBe 2L
    }

    should("legge til AVDOD persongrunnlag når gyldig AVDOD finnes") {
        val eksisterendeAvdoed = Persongrunnlag().apply {
            penPerson = PenPerson(penPersonId = 3L)
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
                    virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                    virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                    penRolleTom = null
                }
            )
            inntektsgrunnlagListe = mutableListOf()
        }
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(eksisterendeAvdoed)
            }
        }
        val endringKravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }

        EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).addPersongrunnlagForEpsToKravhode(
            spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER),
            endringKravhode = endringKravhode,
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L },
            grunnbeloep = 118620
        )

        endringKravhode.persongrunnlagListe shouldHaveSize 1
        endringKravhode.persongrunnlagListe[0].penPerson?.penPersonId shouldBe 3L
    }

    should("legge til inntektsgrunnlag for EPS når epsPaavirkerBeregningen er true") {
        val eksisterendeEps = Persongrunnlag().apply {
            penPerson = PenPerson(penPersonId = 2L)
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                    virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                    virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                    penRolleTom = null
                }
            )
            inntektsgrunnlagListe = mutableListOf()
        }
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(eksisterendeEps)
            }
        }
        val endringKravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val beregningsResultat = BeregningsResultatAlderspensjon2011().apply {
            kravId = 1L
            beregningsinformasjon = SpecialBeregningInformasjon(
                epsMottarPensjon = true, // epsPaavirkerBeregningen = true
                epsHarInntektOver2G = false,
                harGjenlevenderett = false
            )
        }

        EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).addPersongrunnlagForEpsToKravhode(
            spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER),
            endringKravhode = endringKravhode,
            forrigeAlderspensjonBeregningResultat = beregningsResultat,
            grunnbeloep = 118620
        )

        endringKravhode.persongrunnlagListe shouldHaveSize 1
        endringKravhode.persongrunnlagListe[0].inntektsgrunnlagListe shouldHaveSize 1
        endringKravhode.persongrunnlagListe[0].inntektsgrunnlagListe[0].inntektTypeEnum shouldBe InntekttypeEnum.FPI
        endringKravhode.persongrunnlagListe[0].inntektsgrunnlagListe[0].belop shouldBe 3 * 118620
    }

    should("filtrere ut FPI inntektsgrunnlag fra eksisterende persongrunnlag") {
        val eksisterendeEps = Persongrunnlag().apply {
            penPerson = PenPerson(penPersonId = 2L)
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                    virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                    virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                    penRolleTom = null
                }
            )
            inntektsgrunnlagListe = mutableListOf(
                Inntektsgrunnlag().apply {
                    bruk = true
                    inntektTypeEnum = InntekttypeEnum.FPI // Should be filtered out
                    belop = 100000
                },
                Inntektsgrunnlag().apply {
                    bruk = true
                    inntektTypeEnum = InntekttypeEnum.PGI // Should be kept
                    belop = 200000
                }
            )
        }
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(eksisterendeEps)
            }
        }
        val endringKravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }

        EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).addPersongrunnlagForEpsToKravhode(
            spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER),
            endringKravhode = endringKravhode,
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L },
            grunnbeloep = 118620
        )

        val epsInntektsgrunnlag = endringKravhode.persongrunnlagListe[0].inntektsgrunnlagListe
        epsInntektsgrunnlag shouldHaveSize 1
        epsInntektsgrunnlag[0].inntektTypeEnum shouldBe InntekttypeEnum.PGI
    }

    should("konvertere EPS til AVDOD for ENDR_ALDER_M_GJEN") {
        val avdoed = Avdoed(
            pid = Pid("12345678901"),
            antallAarUtenlands = 0,
            inntektFoerDoed = 500000,
            doedDato = LocalDate.of(2024, 6, 15),
            erMedlemAvFolketrygden = true,
            harInntektOver1G = true
        )
        val spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN).copy(avdoed = avdoed)
        val eksisterendeEps = Persongrunnlag().apply {
            penPerson = PenPerson(penPersonId = 2L)
            fodselsdato = LocalDate.of(1960, 1, 1).toNorwegianDateAtNoon()
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                    virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                    virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                    penRolleTom = null
                }
            )
            inntektsgrunnlagListe = mutableListOf()
            opptjeningsgrunnlagListe = mutableListOf()
        }
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(eksisterendeEps)
            }
        }
        val endringKravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }

        EndringPersongrunnlag(
            context = mockk(relaxed = true),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).addPersongrunnlagForEpsToKravhode(
            spec = spec,
            endringKravhode = endringKravhode,
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L },
            grunnbeloep = 118620
        )

        endringKravhode.persongrunnlagListe shouldHaveSize 1
        val konvertertPersongrunnlag = endringKravhode.persongrunnlagListe[0]
        konvertertPersongrunnlag.dodsdato shouldNotBe null
        konvertertPersongrunnlag.arligPGIMinst1G shouldBe true
        konvertertPersongrunnlag.medlemIFolketrygdenSiste3Ar shouldBe true
        konvertertPersongrunnlag.personDetaljListe shouldHaveSize 1
        konvertertPersongrunnlag.personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.AVDOD
    }

    should("ikke legge til persongrunnlag når ingen gyldig EPS eller AVDOD finnes") {
        val eksisterendePersongrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson(penPersonId = 2L)
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.BARN // Not EPS or AVDOD
                    virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                    virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                    penRolleTom = null
                }
            )
            inntektsgrunnlagListe = mutableListOf()
        }
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(eksisterendePersongrunnlag)
            }
        }
        val endringKravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }

        EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).addPersongrunnlagForEpsToKravhode(
            spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER),
            endringKravhode = endringKravhode,
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L },
            grunnbeloep = 118620
        )

        endringKravhode.persongrunnlagListe.shouldBeEmpty()
    }

    should("ikke legge til EPS når persondetalj har penRolleTom satt") {
        val eksisterendeEps = Persongrunnlag().apply {
            penPerson = PenPerson(penPersonId = 2L)
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                    virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                    virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                    penRolleTom = LocalDate.of(2024, 1, 1).toNorwegianDateAtNoon() // Has penRolleTom set
                }
            )
            inntektsgrunnlagListe = mutableListOf()
        }
        val kravService = mockk<KravService>().apply {
            every { fetchKravhode(1L) } returns Kravhode().apply {
                persongrunnlagListe = mutableListOf(eksisterendeEps)
            }
        }
        val endringKravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }

        EndringPersongrunnlag(
            context = mockk(),
            kravService = kravService,
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).addPersongrunnlagForEpsToKravhode(
            spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER),
            endringKravhode = endringKravhode,
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L },
            grunnbeloep = 118620
        )

        endringKravhode.persongrunnlagListe.shouldBeEmpty()
    }

    // Tests for opptjeningGrunnlagListe() with non-null pid and foedselsdato

    should("hente opptjeningsgrunnlag fra beholdningService når pid og foedselsdato er satt") {
        val person = PenPerson().apply {
            foedselsdato = LocalDate.of(1963, 5, 15)
        }
        val spec = simuleringSpec().copy(pid = Pid("12345678901"))
        val opptjeningsgrunnlag1 = Opptjeningsgrunnlag().apply {
            ar = 2020
            pi = 500000
            opptjeningTypeEnum = OpptjeningtypeEnum.PPI
        }
        val opptjeningsgrunnlag2 = Opptjeningsgrunnlag().apply {
            ar = 2021
            pi = 550000
            opptjeningTypeEnum = OpptjeningtypeEnum.PPI
        }
        val beholdningResult = BeholdningerMedGrunnlagResult(
            beholdningListe = emptyList(),
            opptjeningGrunnlagListe = listOf(opptjeningsgrunnlag1, opptjeningsgrunnlag2),
            inntektGrunnlagListe = emptyList(),
            dagpengerGrunnlagListe = emptyList(),
            omsorgGrunnlagListe = emptyList(),
            forstegangstjeneste = null
        )
        val beholdningService = mockk<BeholdningerMedGrunnlagService>().apply {
            every { getBeholdningerMedGrunnlag(any()) } returns beholdningResult
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper>().apply {
            every { mapToPersongrunnlag(any(), any()) } returns Persongrunnlag().apply {
                penPerson = PenPerson().apply { pid = Pid("12345678901") }
            }
        }
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = arrangeKrav(),
            beholdningService = beholdningService,
            epsService = mockk(),
            persongrunnlagMapper = persongrunnlagMapper,
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = person,
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.opptjeningsgrunnlagListe shouldHaveSize 2
        persongrunnlag.opptjeningsgrunnlagListe[0].ar shouldBe 2020
        persongrunnlag.opptjeningsgrunnlagListe[0].pi shouldBe 500000
        persongrunnlag.opptjeningsgrunnlagListe[1].ar shouldBe 2021
        persongrunnlag.opptjeningsgrunnlagListe[1].pi shouldBe 550000
    }

    should("returnere tom opptjeningsgrunnlagListe når pid er null (anonym simulering)") {
        val person = PenPerson().apply {
            foedselsdato = LocalDate.of(1963, 5, 15)
        }
        val spec = simuleringSpec().copy(pid = null, erAnonym = true)
        val beholdningService = mockk<BeholdningerMedGrunnlagService>()
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = arrangeKrav(),
            beholdningService = beholdningService,
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = person,
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.opptjeningsgrunnlagListe.shouldBeEmpty()
        verify(exactly = 0) { beholdningService.getBeholdningerMedGrunnlag(any()) }
    }

    should("returnere tom opptjeningsgrunnlagListe når person.foedselsdato er null") {
        val person = PenPerson().apply {
            foedselsdato = null
        }
        val spec = simuleringSpec().copy(pid = Pid("12345678901"))
        val beholdningService = mockk<BeholdningerMedGrunnlagService>()
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = arrangeKrav(),
            beholdningService = beholdningService,
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = person,
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.opptjeningsgrunnlagListe.shouldBeEmpty()
        verify(exactly = 0) { beholdningService.getBeholdningerMedGrunnlag(any()) }
    }

    should("hente tom opptjeningsgrunnlagListe fra beholdningService") {
        val person = PenPerson().apply {
            foedselsdato = LocalDate.of(1963, 5, 15)
        }
        val spec = simuleringSpec().copy(pid = Pid("12345678901"))
        val beholdningResult = BeholdningerMedGrunnlagResult(
            beholdningListe = emptyList(),
            opptjeningGrunnlagListe = emptyList(),
            inntektGrunnlagListe = emptyList(),
            dagpengerGrunnlagListe = emptyList(),
            omsorgGrunnlagListe = emptyList(),
            forstegangstjeneste = null
        )
        val beholdningService = mockk<BeholdningerMedGrunnlagService>().apply {
            every { getBeholdningerMedGrunnlag(any()) } returns beholdningResult
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper>().apply {
            every { mapToPersongrunnlag(any(), any()) } returns Persongrunnlag().apply {
                penPerson = PenPerson().apply { pid = Pid("12345678901") }
            }
        }
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = arrangeKrav(),
            beholdningService = beholdningService,
            epsService = mockk(),
            persongrunnlagMapper = persongrunnlagMapper,
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = person,
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.opptjeningsgrunnlagListe.shouldBeEmpty()
        verify(exactly = 1) { beholdningService.getBeholdningerMedGrunnlag(any()) }
    }

    should("kalle persongrunnlagMapper med riktige argumenter") {
        val person = PenPerson().apply {
            foedselsdato = LocalDate.of(1963, 5, 15)
            penPersonId = 123L
        }
        val spec = simuleringSpec().copy(pid = Pid("12345678901"))
        val beholdningResult = BeholdningerMedGrunnlagResult(
            beholdningListe = emptyList(),
            opptjeningGrunnlagListe = emptyList(),
            inntektGrunnlagListe = emptyList(),
            dagpengerGrunnlagListe = emptyList(),
            omsorgGrunnlagListe = emptyList(),
            forstegangstjeneste = null
        )
        val beholdningService = mockk<BeholdningerMedGrunnlagService>().apply {
            every { getBeholdningerMedGrunnlag(any()) } returns beholdningResult
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper>().apply {
            every { mapToPersongrunnlag(any(), any()) } returns Persongrunnlag().apply {
                penPerson = PenPerson().apply { pid = Pid("12345678901") }
            }
        }
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        EndringPersongrunnlag(
            context = mockk(),
            kravService = arrangeKrav(),
            beholdningService = beholdningService,
            epsService = mockk(),
            persongrunnlagMapper = persongrunnlagMapper,
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = person,
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        verify(exactly = 1) { persongrunnlagMapper.mapToPersongrunnlag(person, spec) }
    }

    should("hente opptjeningsgrunnlag med ulike opptjeningtyper") {
        val person = PenPerson().apply {
            foedselsdato = LocalDate.of(1963, 5, 15)
        }
        val spec = simuleringSpec().copy(pid = Pid("12345678901"))
        val opptjeningsgrunnlagPPI = Opptjeningsgrunnlag().apply {
            ar = 2020
            pi = 500000
            opptjeningTypeEnum = OpptjeningtypeEnum.PPI
        }
        val opptjeningsgrunnlagOBU6 = Opptjeningsgrunnlag().apply {
            ar = 2019
            pi = 0
            opptjeningTypeEnum = OpptjeningtypeEnum.OBU6
        }
        val beholdningResult = BeholdningerMedGrunnlagResult(
            beholdningListe = emptyList(),
            opptjeningGrunnlagListe = listOf(opptjeningsgrunnlagPPI, opptjeningsgrunnlagOBU6),
            inntektGrunnlagListe = emptyList(),
            dagpengerGrunnlagListe = emptyList(),
            omsorgGrunnlagListe = emptyList(),
            forstegangstjeneste = null
        )
        val beholdningService = mockk<BeholdningerMedGrunnlagService>().apply {
            every { getBeholdningerMedGrunnlag(any()) } returns beholdningResult
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper>().apply {
            every { mapToPersongrunnlag(any(), any()) } returns Persongrunnlag().apply {
                penPerson = PenPerson().apply { pid = Pid("12345678901") }
            }
        }
        val generelleDataHolder = mockk<GenerelleDataHolder>().apply {
            every { getSisteGyldigeOpptjeningsaar() } returns 2024
        }

        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = arrangeKrav(),
            beholdningService = beholdningService,
            epsService = mockk(),
            persongrunnlagMapper = persongrunnlagMapper,
            generelleDataHolder = generelleDataHolder,
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = person,
            spec = spec,
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        persongrunnlag!!.opptjeningsgrunnlagListe shouldHaveSize 2
        persongrunnlag.opptjeningsgrunnlagListe[0].opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
        persongrunnlag.opptjeningsgrunnlagListe[1].opptjeningTypeEnum shouldBe OpptjeningtypeEnum.OBU6
    }
})

private fun arrangeKrav(): KravService =
    mockk<KravService>().apply {
        every { fetchKravhode(1L) } returns
                Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlag()) }
    }

private fun persongrunnlag() =
    Persongrunnlag().apply {
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                penRolleTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon()
                virkTom = LocalDate.of(1901, 1, 1).toNorwegianDateAtNoon() // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = false // => dvs. denne persondetaljen er irrelevant
                grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                penRolleTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon()
                virkTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon() // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.MOR
                penRolleTom = LocalDate.of(2024, 1, 1).toNorwegianDateAtNoon() // => i fortid => irrelevant
                virkTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon() // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.FAR
                penRolleTom = null
                virkTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon() // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.BARN
                penRolleTom = LocalDate.of(2025, 1, 1).toNorwegianDateAtNoon() // => tom = "i dag" => detaljen er relevant
                virkTom = LocalDate.of(1901, 1, 1).toNorwegianDateAtNoon() // NB: virkTom has no effect
            }
        )
    }
