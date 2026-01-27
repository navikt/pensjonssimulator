package no.nav.pensjon.simulator.core.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.beregning.Grunnpensjon
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengrekke
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.*
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate
import java.util.*

class SimulatorOutputMapperTest : FunSpec({

    test("mapToSimulertOpptjening should map poengtall.pp to pensjonsgivendeInntektPensjonspoeng") {
        SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = listOf(
                Poengtall().apply {
                    ar = 2024
                    pp = 1.23
                },
            ),
            useNullAsDefaultPensjonspoeng = false
        ).pensjonsgivendeInntektPensjonspoeng shouldBe 1.23
    }

    test("mapToSimulertOpptjening should return undefined pensjonpoeng when no poengtall-liste is present and useNullAsDefaultPensjonspoeng is true") {
        SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2023, // not present in poengtallListe
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).pensjonsgivendeInntektPensjonspoeng shouldBe null
    }

    test("mapToSimulertOpptjening should return zero pensjonpoeng when no poengtall for angitt aar and useNullAsDefaultPensjonspoeng is false") {
        SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2023, // not present in poengtallListe
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = listOf(
                Poengtall().apply {
                    ar = 2024
                    pp = 1.23
                },
            ),
            useNullAsDefaultPensjonspoeng = false
        ).pensjonsgivendeInntektPensjonspoeng shouldBe 0.0
    }

    // =====================================================
    // Tests for mapToSimulatorOutput
    // =====================================================

    test("mapToSimulatorOutput should map epsHarInntektOver2G from spec") {
        val spec = createSimuleringSpec(epsHarInntektOver2G = true)
        val persongrunnlag = createPersongrunnlagWithSoeker(SivilstandEnum.GIFT)

        val result = SimulatorOutputMapper.mapToSimulatorOutput(
            simuleringSpec = spec,
            soekerGrunnlag = persongrunnlag,
            grunnbeloep = 118620
        )

        result.epsHarInntektOver2G shouldBe true
    }

    test("mapToSimulatorOutput should map epsHarPensjon from spec") {
        val spec = createSimuleringSpec(epsHarPensjon = true)
        val persongrunnlag = createPersongrunnlagWithSoeker(SivilstandEnum.GIFT)

        val result = SimulatorOutputMapper.mapToSimulatorOutput(
            simuleringSpec = spec,
            soekerGrunnlag = persongrunnlag,
            grunnbeloep = 118620
        )

        result.epsHarPensjon shouldBe true
    }

    test("mapToSimulatorOutput should map grunnbeloep") {
        val spec = createSimuleringSpec()
        val persongrunnlag = createPersongrunnlagWithSoeker(SivilstandEnum.UGIF)

        val result = SimulatorOutputMapper.mapToSimulatorOutput(
            simuleringSpec = spec,
            soekerGrunnlag = persongrunnlag,
            grunnbeloep = 118620
        )

        result.grunnbeloep shouldBe 118620
    }

    test("mapToSimulatorOutput should map sivilstand from first person detail") {
        val spec = createSimuleringSpec()
        val persongrunnlag = createPersongrunnlagWithSoeker(SivilstandEnum.GIFT)

        val result = SimulatorOutputMapper.mapToSimulatorOutput(
            simuleringSpec = spec,
            soekerGrunnlag = persongrunnlag,
            grunnbeloep = 118620
        )

        result.sivilstand shouldBe SivilstandEnum.GIFT
    }

    // =====================================================
    // Tests for simulertPrivatAfpPeriode
    // =====================================================

    test("simulertPrivatAfpPeriode should map aarligBeloep and alderAar") {
        val resultat = createBeregningsResultatAfpPrivat()

        val result = SimulatorOutputMapper.simulertPrivatAfpPeriode(
            aarligBeloep = 250000,
            resultat = resultat,
            alder = 62
        )

        result.aarligBeloep shouldBe 250000
        result.alderAar shouldBe 62
    }

    test("simulertPrivatAfpPeriode should map maanedligBeloep from pensjonUnderUtbetaling") {
        val resultat = createBeregningsResultatAfpPrivat(totalbelopNetto = 20833)

        val result = SimulatorOutputMapper.simulertPrivatAfpPeriode(
            aarligBeloep = 250000,
            resultat = resultat,
            alder = 62
        )

        result.maanedligBeloep shouldBe 20833
    }

    test("simulertPrivatAfpPeriode should map livsvarig from AfpPrivatLivsvarig") {
        val resultat = createBeregningsResultatAfpPrivat()
        resultat.pensjonUnderUtbetaling?.addYtelseskomponent(
            AfpPrivatLivsvarig().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.AFP_PRIVAT_LIVSVARIG
                netto = 15000
                afpForholdstall = 1.045
                justeringsbelop = 500
            }
        )

        val result = SimulatorOutputMapper.simulertPrivatAfpPeriode(
            aarligBeloep = 250000,
            resultat = resultat,
            alder = 62
        )

        result.livsvarig shouldBe 15000
        result.afpForholdstall shouldBe 1.045
        result.justeringBeloep shouldBe 500
    }

    test("simulertPrivatAfpPeriode should map kronetillegg") {
        val resultat = createBeregningsResultatAfpPrivat()
        resultat.pensjonUnderUtbetaling?.addYtelseskomponent(
            AfpKronetillegg().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.AFP_KRONETILLEGG
                netto = 1200
            }
        )

        val result = SimulatorOutputMapper.simulertPrivatAfpPeriode(
            aarligBeloep = 250000,
            resultat = resultat,
            alder = 62
        )

        result.kronetillegg shouldBe 1200
    }

    test("simulertPrivatAfpPeriode should map kompensasjonstillegg") {
        val resultat = createBeregningsResultatAfpPrivat()
        resultat.pensjonUnderUtbetaling?.addYtelseskomponent(
            AfpKompensasjonstillegg().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.AFP_KOMP_TILLEGG
                netto = 800
            }
        )

        val result = SimulatorOutputMapper.simulertPrivatAfpPeriode(
            aarligBeloep = 250000,
            resultat = resultat,
            alder = 62
        )

        result.kompensasjonstillegg shouldBe 800
    }

    test("simulertPrivatAfpPeriode should map afpOpptjening from afpPrivatBeregning") {
        val resultat = createBeregningsResultatAfpPrivat()
        resultat.afpPrivatBeregning = AfpPrivatBeregning().apply {
            afpOpptjening = AfpOpptjening().apply {
                totalbelop = 3500000.0
            }
        }

        val result = SimulatorOutputMapper.simulertPrivatAfpPeriode(
            aarligBeloep = 250000,
            resultat = resultat,
            alder = 62
        )

        result.afpOpptjening shouldBe 3500000
    }

    test("simulertPrivatAfpPeriode should return zero afpOpptjening when not present") {
        val resultat = createBeregningsResultatAfpPrivat()

        val result = SimulatorOutputMapper.simulertPrivatAfpPeriode(
            aarligBeloep = 250000,
            resultat = resultat,
            alder = 62
        )

        result.afpOpptjening shouldBe 0
    }

    // =====================================================
    // Tests for mapToSimulertBeregningsinformasjon - AP2011
    // =====================================================

    test("mapToSimulertBeregningsinformasjon should handle AP2011 beregning") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        val simulertAlderspensjon = SimulertAlderspensjon().apply {
            kapittel19Andel = 1.0
            kapittel20Andel = 0.0
        }

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.datoFom shouldBe LocalDate.of(2025, 7, 1)
        result.uttakGrad shouldBe 100.0
    }

    test("mapToSimulertBeregningsinformasjon should map kapittel19Pensjon for AP2011") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011(totalbelopNettoAr = 350000.0)
        val simulertAlderspensjon = SimulertAlderspensjon().apply {
            kapittel19Andel = 1.0
            kapittel20Andel = 0.0
        }

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.kapittel19Pensjon shouldBe 350000
        result.vektetKapittel19Pensjon shouldBe 350000
    }

    test("mapToSimulertBeregningsinformasjon should map vinnendeBeregning based on gjenlevenderettAnvendt") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        beregningsResultat.beregningsInformasjonKapittel19 = BeregningsInformasjon().apply {
            gjenlevenderettAnvendt = true
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.vinnendeBeregning shouldBe GrunnlagsrolleEnum.AVDOD
    }

    test("mapToSimulertBeregningsinformasjon should map vinnendeBeregning to SOKER when gjenlevenderettAnvendt is false") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        beregningsResultat.beregningsInformasjonKapittel19 = BeregningsInformasjon().apply {
            gjenlevenderettAnvendt = false
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.vinnendeBeregning shouldBe GrunnlagsrolleEnum.SOKER
    }

    test("mapToSimulertBeregningsinformasjon should map basispensjon for AP2011") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        beregningsResultat.beregningKapittel19 = AldersberegningKapittel19().apply {
            basispensjon = Basispensjon().apply {
                totalbelop = 300000.0
                gp = BasisGrunnpensjon().apply { bruttoPerAr = 120000.0 }
                tp = BasisTilleggspensjon().apply { bruttoPerAr = 150000.0 }
                pt = BasisPensjonstillegg().apply {
                    bruttoPerAr = 30000.0
                }
            }
            tt_anv = 40
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.basispensjon shouldBe 300000
        result.basisGrunnpensjon shouldBe 120000.0
        result.basisTilleggspensjon shouldBe 150000.0
        result.basisPensjonstillegg shouldBe 30000.0
        result.tt_anv_kap19 shouldBe 40
    }

    test("mapToSimulertBeregningsinformasjon should map restpensjon for AP2011") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        beregningsResultat.beregningKapittel19 = AldersberegningKapittel19().apply {
            restpensjon = Basispensjon().apply {
                gp = BasisGrunnpensjon().apply { bruttoPerAr = 50000.0 }
                tp = BasisTilleggspensjon().apply { bruttoPerAr = 75000.0 }
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.restBasisPensjon shouldBe 125000
    }

    test("mapToSimulertBeregningsinformasjon should map spt and poengaar for AP2011") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        beregningsResultat.beregningsInformasjonKapittel19 = BeregningsInformasjon().apply {
            forholdstallUttak = 1.035
            spt = Sluttpoengtall().apply {
                pt = 5.25
                poengrekke = Poengrekke().apply {
                    pa_f92 = 10
                    pa_e91 = 25
                }
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.spt shouldBe 5.25
        result.pa_f92 shouldBe 10
        result.pa_e91 shouldBe 25
        result.forholdstall shouldBe 1.035
    }

    // =====================================================
    // Tests for mapToSimulertBeregningsinformasjon - AP2016
    // =====================================================

    test("mapToSimulertBeregningsinformasjon should handle AP2016 beregning") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2016(
            kap19TotalNettoAr = 245000.0,
            kap20TotalNettoAr = 255000.0
        )
        val simulertAlderspensjon = SimulertAlderspensjon().apply {
            kapittel19Andel = 0.7
            kapittel20Andel = 0.3
        }

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.kapittel19Pensjon shouldBe 245000
        result.vektetKapittel19Pensjon shouldBe 171500
        result.kapittel20Pensjon shouldBe 255000
        result.vektetKapittel20Pensjon shouldBe 76500
    }

    test("mapToSimulertBeregningsinformasjon should map gjenlevendetilleggAP for AP2016") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2016()
        beregningsResultat.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            gjenlevendetilleggAP = GjenlevendetilleggAP().apply {
                apKap19MedGJR = 280000
                apKap19UtenGJR = 250000
                bruttoPerAr = 30000.0
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.apKap19medGJR shouldBe 280000
        result.apKap19utenGJR shouldBe 250000
        result.gjtAP shouldBe 30000
    }

    test("mapToSimulertBeregningsinformasjon should map gjenlevendetilleggAPKap19 for AP2016") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2016()
        // gjenlevendetilleggAPKap19 must be set on the top-level pensjonUnderUtbetaling
        beregningsResultat.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            gjenlevendetilleggAPKap19 = GjenlevendetilleggAPKap19().apply {
                bruttoPerAr = 25000.0
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.gjtAPKap19 shouldBe 25000
    }

    // =====================================================
    // Tests for mapToSimulertBeregningsinformasjon - AP2025
    // =====================================================

    test("mapToSimulertBeregningsinformasjon should handle AP2025 beregning") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_N_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2025(totalbelopNettoAr = 400000.0)
        val simulertAlderspensjon = SimulertAlderspensjon().apply {
            kapittel19Andel = 0.0
            kapittel20Andel = 1.0
        }

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1970, 6, 15),
            knekkpunkt = LocalDate.of(2037, 7, 1)
        )

        result.kapittel20Pensjon shouldBe 400000
        result.vektetKapittel20Pensjon shouldBe 400000
    }

    test("mapToSimulertBeregningsinformasjon should map delingstall and tt_anv for AP2025") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_N_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2025()
        beregningsResultat.beregningKapittel20 = AldersberegningKapittel20().apply {
            delingstall = 18.75
            tt_anv = 40
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1970, 6, 15),
            knekkpunkt = LocalDate.of(2037, 7, 1)
        )

        result.delingstall shouldBe 18.75
        result.tt_anv_kap20 shouldBe 40
    }

    test("mapToSimulertBeregningsinformasjon should map garantipensjonssats for AP2025") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_N_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2025()
        beregningsResultat.beregningKapittel20 = AldersberegningKapittel20().apply {
            beholdninger = Beholdninger().apply {
                beholdninger = listOf(
                    Pensjonsbeholdning().apply {
                        ar = 2037
                        totalbelop = 4500000.0
                    },
                    Garantipensjonsbeholdning().apply {
                        ar = 2037
                        sats = 2.0
                    }
                )
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1970, 6, 15),
            knekkpunkt = LocalDate.of(2037, 7, 1)
        )

        result.garantipensjonssats shouldBe 2.0
        result.pensjonBeholdningEtterUttak shouldBe 4500000
    }

    // =====================================================
    // Tests for mapToSimulertBeregningsinformasjon - ytelseskomponenter
    // =====================================================

    test("mapToSimulertBeregningsinformasjon should map inntektspensjon") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_N_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2025()
        beregningsResultat.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            totalbelopNettoAr = 400000.0
            totalbelopNetto = 33333
            inntektspensjon = Inntektspensjon().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.IP
                bruttoPerAr = 280000.0
                brutto = 23333
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1970, 6, 15),
            knekkpunkt = LocalDate.of(2037, 7, 1)
        )

        result.inntektspensjon shouldBe 280000
        result.inntektspensjonPerMaaned shouldBe 23333
        result.aarligBeloep shouldBe 400000
        result.maanedligBeloep shouldBe 33333
    }

    test("mapToSimulertBeregningsinformasjon should map garantipensjon") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_N_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2025()
        beregningsResultat.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            garantipensjon = Garantipensjon().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.GAP
                bruttoPerAr = 120000.0
                brutto = 10000
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1970, 6, 15),
            knekkpunkt = LocalDate.of(2037, 7, 1)
        )

        result.garantipensjon shouldBe 120000
        result.garantipensjonPerMaaned shouldBe 10000
    }

    test("mapToSimulertBeregningsinformasjon should map garantitillegg") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_N_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2025()
        beregningsResultat.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            garantitillegg = Garantitillegg().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.GAT
                bruttoPerAr = 15000.0
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1970, 6, 15),
            knekkpunkt = LocalDate.of(2037, 7, 1)
        )

        result.garantitillegg shouldBe 15000
    }

    test("mapToSimulertBeregningsinformasjon should map grunnpensjon") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        beregningsResultat.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            grunnpensjon = Grunnpensjon().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.GP
                bruttoPerAr = 118620.0
                brutto = 9885
                pSats_gp = 1.0
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.grunnpensjon shouldBe 118620
        result.grunnpensjonPerMaaned shouldBe 9885
        result.grunnpensjonsats shouldBe 1.0
    }

    test("mapToSimulertBeregningsinformasjon should map tilleggspensjon") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        beregningsResultat.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            tilleggspensjon = Tilleggspensjon().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.TP
                bruttoPerAr = 180000.0
                brutto = 15000
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.tilleggspensjon shouldBe 180000
        result.tilleggspensjonPerMaaned shouldBe 15000
    }

    test("mapToSimulertBeregningsinformasjon should map pensjonstillegg") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        beregningsResultat.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            pensjonstillegg = Pensjonstillegg().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.PT
                bruttoPerAr = 50000.0
                brutto = 4166
                minstepensjonsnivaSats = 2.48
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.pensjonstillegg shouldBe 50000
        result.pensjonstilleggPerMaaned shouldBe 4166
        result.minstePensjonsnivaSats shouldBe 2.48
    }

    test("mapToSimulertBeregningsinformasjon should map skjermingstillegg") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2016()
        beregningsResultat.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            skjermingstillegg = Skjermingstillegg().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.SKJERMT
                bruttoPerAr = 20000.0
                ufg = 50
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.skjermingstillegg shouldBe 20000
        result.ufoereGrad shouldBe 50
    }

    test("mapToSimulertBeregningsinformasjon should map minstenivaatillegg") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        beregningsResultat.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            minstenivatilleggIndividuelt = MinstenivatilleggIndividuelt().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.MIN_NIVA_TILL_INDV
                bruttoPerAr = 12000.0
            }
            minstenivatilleggPensjonistpar = MinstenivatilleggPensjonistpar().apply {
                ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.MIN_NIVA_TILL_PPAR
                bruttoPerAr = 8000.0
            }
        }
        val simulertAlderspensjon = SimulertAlderspensjon()

        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.individueltMinstenivaaTillegg shouldBe 12000
        result.pensjonistParMinstenivaaTillegg shouldBe 8000
    }

    // =====================================================
    // Tests for mapToSimulertBeregningsinformasjon - startMaaned calculation
    // =====================================================

    test("mapToSimulertBeregningsinformasjon should calculate startMaaned correctly") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        val simulertAlderspensjon = SimulertAlderspensjon()

        // Born June 15, knekkpunkt July 1 = 1 month after birth month
        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 7, 1)
        )

        result.startMaaned shouldBe 1
    }

    test("mapToSimulertBeregningsinformasjon should return 12 for startMaaned when knekkpunkt is in birth month") {
        val kravhode = createKravhode(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsResultat = createBeregningsResultatAlderspensjon2011()
        val simulertAlderspensjon = SimulertAlderspensjon()

        // Born June 15, knekkpunkt June 1 = 0 months, should return 12
        val result = SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
            kravhode = kravhode,
            beregningResultat = beregningsResultat,
            simulertAlderspensjon = simulertAlderspensjon,
            foedselsdato = LocalDate.of(1960, 6, 15),
            knekkpunkt = LocalDate.of(2025, 6, 1)
        )

        result.startMaaned shouldBe 12
    }

    // =====================================================
    // Additional mapToSimulertOpptjening tests
    // =====================================================

    test("mapToSimulertOpptjening should map pensjonsgivendeInntekt from opptjeningsgrunnlag") {
        val persongrunnlag = Persongrunnlag().apply {
            opptjeningsgrunnlagListe = mutableListOf(
                Opptjeningsgrunnlag().apply {
                    ar = 2024
                    pi = 750000
                    opptjeningTypeEnum = OpptjeningtypeEnum.PPI
                }
            )
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.pensjonsgivendeInntekt shouldBe 750000
    }

    test("mapToSimulertOpptjening should map omsorgPensjonspoeng with priority") {
        val persongrunnlag = Persongrunnlag().apply {
            opptjeningsgrunnlagListe = mutableListOf(
                Opptjeningsgrunnlag().apply {
                    ar = 2024
                    pp = 3.5
                    opptjeningTypeEnum = OpptjeningtypeEnum.OBU7 // lower priority
                },
                Opptjeningsgrunnlag().apply {
                    ar = 2024
                    pp = 3.0
                    opptjeningTypeEnum = OpptjeningtypeEnum.OSFE // highest priority
                }
            )
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.omsorgPensjonspoeng shouldBe 3.0 // OSFE has highest priority
    }

    test("mapToSimulertOpptjening should map pensjonBeholdning from soekerGrunnlag") {
        val persongrunnlag = Persongrunnlag().apply {
            beholdninger = mutableListOf(
                Pensjonsbeholdning().apply {
                    ar = 2024
                    totalbelop = 3500000.0
                    beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
                }
            )
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.pensjonBeholdning shouldBe 3500000
    }

    test("mapToSimulertOpptjening should map omsorg from omsorgsgrunnlagListe") {
        val persongrunnlag = Persongrunnlag().apply {
            omsorgsgrunnlagListe = mutableListOf(
                Omsorgsgrunnlag().apply { ar = 2024 }
            )
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.omsorg shouldBe true
    }

    test("mapToSimulertOpptjening should return false for omsorg when no omsorgsgrunnlag for year") {
        val persongrunnlag = Persongrunnlag().apply {
            omsorgsgrunnlagListe = mutableListOf(
                Omsorgsgrunnlag().apply { ar = 2023 } // different year
            )
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.omsorg shouldBe false
    }

    test("mapToSimulertOpptjening should map dagpenger") {
        val persongrunnlag = Persongrunnlag().apply {
            dagpengegrunnlagListe = mutableListOf(
                Dagpengegrunnlag().apply {
                    ar = 2024
                    dagpengetypeEnum = DagpengetypeEnum.DP
                }
            )
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.dagpenger shouldBe true
    }

    test("mapToSimulertOpptjening should map dagpengerFiskere") {
        val persongrunnlag = Persongrunnlag().apply {
            dagpengegrunnlagListe = mutableListOf(
                Dagpengegrunnlag().apply {
                    ar = 2024
                    dagpengetypeEnum = DagpengetypeEnum.DP_FF
                }
            )
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.dagpengerFiskere shouldBe true
    }

    test("mapToSimulertOpptjening should map foerstegangstjeneste") {
        val persongrunnlag = Persongrunnlag().apply {
            forstegangstjenestegrunnlag = Forstegangstjeneste().apply {
                periodeListe = mutableListOf(
                    ForstegangstjenestePeriode().apply {
                        fomDato = Date(124, 0, 1) // Jan 1, 2024
                    }
                )
            }
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.foerstegangstjeneste shouldBe true
    }

    test("mapToSimulertOpptjening should map harUfoere") {
        val persongrunnlag = Persongrunnlag().apply {
            uforeHistorikk = Uforehistorikk().apply {
                uforeperiodeListe = mutableListOf(
                    Uforeperiode().apply {
                        ufgFom = Date(124, 0, 1) // Jan 1, 2024
                        ufgTom = Date(124, 11, 31) // Dec 31, 2024
                        uforeTypeEnum = UforetypeEnum.UF_M_YRKE
                    }
                )
            }
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.harUfoere shouldBe true
    }

    test("mapToSimulertOpptjening should filter out VIRK_IKKE_UFOR from uforeperioder") {
        val persongrunnlag = Persongrunnlag().apply {
            uforeHistorikk = Uforehistorikk().apply {
                uforeperiodeListe = mutableListOf(
                    Uforeperiode().apply {
                        ufgFom = Date(124, 0, 1)
                        ufgTom = Date(124, 11, 31)
                        uforeTypeEnum = UforetypeEnum.VIRK_IKKE_UFOR // should be filtered
                    }
                )
            }
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.harUfoere shouldBe false
    }

    test("mapToSimulertOpptjening should map harOffentligAfp") {
        val persongrunnlag = Persongrunnlag().apply {
            afpHistorikkListe = listOf(
                AfpHistorikk().apply {
                    virkFom = Date(124, 0, 1) // Jan 1, 2024
                    virkTom = Date(124, 11, 31) // Dec 31, 2024
                }
            )
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.harOffentligAfp shouldBe true
    }

    test("mapToSimulertOpptjening should return false for harOffentligAfp when afpHistorikkListe is empty") {
        val persongrunnlag = Persongrunnlag().apply {
            afpHistorikkListe = emptyList()
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.harOffentligAfp shouldBe false
    }

    test("mapToSimulertOpptjening should get pensjonBeholdning from beregningsresultat when not in persongrunnlag") {
        val persongrunnlag = Persongrunnlag()
        val beregningsResultat = BeregningsResultatAlderspensjon2025().apply {
            virkFom = Date(124, 0, 1) // Jan 1, 2024
            uttaksgrad = 100
            beregningKapittel20 = AldersberegningKapittel20().apply {
                beholdninger = Beholdninger().apply {
                    beholdninger = listOf(
                        Pensjonsbeholdning().apply {
                            ar = 2024
                            totalbelop = 4200000.0
                            beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
                        }
                    )
                }
            }
        }

        val result = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = listOf(beregningsResultat),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        )

        result.pensjonBeholdning shouldBe 4200000
    }
})

// =====================================================
// Helper functions
// =====================================================

private fun createSimuleringSpec(
    epsHarPensjon: Boolean = false,
    epsHarInntektOver2G: Boolean = false
) = SimuleringSpec(
    type = SimuleringTypeEnum.ALDER,
    sivilstatus = SivilstatusType.UGIF,
    epsHarPensjon = epsHarPensjon,
    foersteUttakDato = LocalDate.of(2025, 7, 1),
    heltUttakDato = null,
    pid = Pid("12906498357"),
    foedselDato = LocalDate.of(1964, 6, 12),
    avdoed = null,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = UttakGradKode.P_100,
    forventetInntektBeloep = 0,
    inntektUnderGradertUttakBeloep = 0,
    inntektEtterHeltUttakBeloep = 0,
    inntektEtterHeltUttakAntallAar = 0,
    foedselAar = 1964,
    utlandAntallAar = 0,
    utlandPeriodeListe = mutableListOf(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = false,
    epsHarInntektOver2G = epsHarInntektOver2G,
    livsvarigOffentligAfp = null,
    pre2025OffentligAfp = null,
    erAnonym = false,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = false,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false
)

private fun createPersongrunnlagWithSoeker(sivilstand: SivilstandEnum): Persongrunnlag =
    Persongrunnlag().apply {
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                sivilstandTypeEnum = sivilstand
                bruk = true
            }
        )
    }

private fun createKravhode(regelverkType: RegelverkTypeEnum): Kravhode =
    Kravhode().apply {
        regelverkTypeEnum = regelverkType
    }

private fun createBeregningsResultatAfpPrivat(
    totalbelopNetto: Int = 0
): BeregningsResultatAfpPrivat =
    BeregningsResultatAfpPrivat().apply {
        uttaksgrad = 100
        pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            this.totalbelopNetto = totalbelopNetto
        }
    }

private fun createBeregningsResultatAlderspensjon2011(
    totalbelopNettoAr: Double = 0.0
): BeregningsResultatAlderspensjon2011 =
    BeregningsResultatAlderspensjon2011().apply {
        uttaksgrad = 100
        pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            this.totalbelopNettoAr = totalbelopNettoAr
        }
    }

private fun createBeregningsResultatAlderspensjon2016(
    kap19TotalNettoAr: Double = 0.0,
    kap20TotalNettoAr: Double = 0.0
): BeregningsResultatAlderspensjon2016 =
    BeregningsResultatAlderspensjon2016().apply {
        uttaksgrad = 100
        beregningsResultat2011 = BeregningsResultatAlderspensjon2011().apply {
            uttaksgrad = 100
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNettoAr = kap19TotalNettoAr
            }
        }
        beregningsResultat2025 = BeregningsResultatAlderspensjon2025().apply {
            uttaksgrad = 100
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNettoAr = kap20TotalNettoAr
            }
        }
    }

private fun createBeregningsResultatAlderspensjon2025(
    totalbelopNettoAr: Double = 0.0
): BeregningsResultatAlderspensjon2025 =
    BeregningsResultatAlderspensjon2025().apply {
        uttaksgrad = 100
        pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            this.totalbelopNettoAr = totalbelopNettoAr
        }
    }
