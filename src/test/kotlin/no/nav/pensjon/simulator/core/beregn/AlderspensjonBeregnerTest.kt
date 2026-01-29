package no.nav.pensjon.simulator.core.beregn

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.to.*
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate

class AlderspensjonBeregnerTest : FunSpec({

    // ===========================================
    // Tests for first uttak with different regelverkTypes
    // ===========================================

    test("beregnAlderspensjon should call beregnAlderspensjon2011FoersteUttak for N_REG_G_OPPTJ") {
        val expectedResult = BeregningsResultatAlderspensjon2011()
        val context = mockk<SimulatorContext> {
            every { beregnAlderspensjon2011FoersteUttak(any(), any()) } returns expectedResult
        }

        val result = AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = null,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = 123L,
            isFoersteUttak = true,
            ignoreAvslag = false
        )

        result shouldBe expectedResult
        verify(exactly = 1) { context.beregnAlderspensjon2011FoersteUttak(any(), eq(123L)) }
    }

    test("beregnAlderspensjon should call beregnAlderspensjon2016FoersteUttak for N_REG_G_N_OPPTJ") {
        val expectedResult = BeregningsResultatAlderspensjon2016()
        val context = mockk<SimulatorContext> {
            every { beregnAlderspensjon2016FoersteUttak(any(), any()) } returns expectedResult
        }

        val result = AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = null,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = true,
            ignoreAvslag = false
        )

        result shouldBe expectedResult
        verify(exactly = 1) { context.beregnAlderspensjon2016FoersteUttak(any(), isNull()) }
    }

    test("beregnAlderspensjon should call beregnAlderspensjon2025FoersteUttak for N_REG_N_OPPTJ") {
        val expectedResult = BeregningsResultatAlderspensjon2025()
        val context = mockk<SimulatorContext> {
            every { beregnAlderspensjon2025FoersteUttak(any(), any()) } returns expectedResult
        }

        val result = AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_N_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = null,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = true,
            ignoreAvslag = false
        )

        result shouldBe expectedResult
        verify(exactly = 1) { context.beregnAlderspensjon2025FoersteUttak(any(), isNull()) }
    }

    test("beregnAlderspensjon should throw for G_REG regelverkType on first uttak") {
        val context = mockk<SimulatorContext>()

        shouldThrow<RuntimeException> {
            AlderspensjonBeregner(context).beregnAlderspensjon(
                kravhode = kravhode(RegelverkTypeEnum.G_REG),
                vedtakListe = mutableListOf(),
                virkningDato = LocalDate.of(2025, 1, 1),
                sisteAldersberegning2011 = null,
                privatAfp = null,
                livsvarigOffentligAfpGrunnlag = null,
                simuleringSpec = simuleringSpec(),
                sakId = null,
                isFoersteUttak = true,
                ignoreAvslag = false
            )
        }.message shouldBe "Unexpected regelverkType: G_REG"
    }

    test("beregnAlderspensjon should throw for null regelverkType on first uttak") {
        val context = mockk<SimulatorContext>()

        shouldThrow<RuntimeException> {
            AlderspensjonBeregner(context).beregnAlderspensjon(
                kravhode = Kravhode(), // no regelverkType
                vedtakListe = mutableListOf(),
                virkningDato = LocalDate.of(2025, 1, 1),
                sisteAldersberegning2011 = null,
                privatAfp = null,
                livsvarigOffentligAfpGrunnlag = null,
                simuleringSpec = simuleringSpec(),
                sakId = null,
                isFoersteUttak = true,
                ignoreAvslag = false
            )
        }.message shouldBe "Undefined regelverkTypeEnum"
    }

    // ===========================================
    // Tests for revurdering with different regelverkTypes
    // ===========================================

    test("beregnAlderspensjon should call revurderAlderspensjon2011 for N_REG_G_OPPTJ revurdering") {
        val expectedResult = BeregningsResultatAlderspensjon2011()
        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2011(any(), any()) } returns expectedResult
        }

        val result = AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = SisteAldersberegning2011(),
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = 456L,
            isFoersteUttak = false,
            ignoreAvslag = false
        )

        result shouldBe expectedResult
        verify(exactly = 1) { context.revurderAlderspensjon2011(any(), eq(456L)) }
    }

    test("beregnAlderspensjon should call revurderAlderspensjon2016 for N_REG_G_N_OPPTJ revurdering") {
        val expectedResult = BeregningsResultatAlderspensjon2016()
        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2016(any(), any()) } returns expectedResult
        }

        val result = AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = SisteAldersberegning2016(),
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = false,
            ignoreAvslag = false
        )

        result shouldBe expectedResult
        verify(exactly = 1) { context.revurderAlderspensjon2016(any(), isNull()) }
    }

    test("beregnAlderspensjon should call revurderAlderspensjon2025 for N_REG_N_OPPTJ revurdering") {
        val expectedResult = BeregningsResultatAlderspensjon2025()
        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2025(any(), any()) } returns expectedResult
        }

        val result = AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_N_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = SisteAldersberegning2011(),
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = false,
            ignoreAvslag = false
        )

        result shouldBe expectedResult
        verify(exactly = 1) { context.revurderAlderspensjon2025(any(), isNull()) }
    }

    test("beregnAlderspensjon should throw for G_REG regelverkType on revurdering") {
        val context = mockk<SimulatorContext>()

        shouldThrow<RuntimeException> {
            AlderspensjonBeregner(context).beregnAlderspensjon(
                kravhode = kravhode(RegelverkTypeEnum.G_REG),
                vedtakListe = mutableListOf(),
                virkningDato = LocalDate.of(2025, 1, 1),
                sisteAldersberegning2011 = SisteAldersberegning2011(),
                privatAfp = null,
                livsvarigOffentligAfpGrunnlag = null,
                simuleringSpec = simuleringSpec(),
                sakId = null,
                isFoersteUttak = false,
                ignoreAvslag = false
            )
        }.message shouldBe "Unexpected regelverkType: G_REG"
    }

    // ===========================================
    // Tests for ignoreAvslag functionality
    // ===========================================

    test("beregnAlderspensjon should innvilge vedtak when ignoreAvslag is true and vedtak is not innvilget") {
        val vedtak = VilkarsVedtak().apply {
            anbefaltResultatEnum = VedtakResultatEnum.AVSL
            vilkarsvedtakResultatEnum = VedtakResultatEnum.AVSL
            begrunnelseEnum = BegrunnelseTypeEnum.ANNULERING
            merknadListe = mutableListOf(Merknad())
        }
        val vedtakListe = mutableListOf(vedtak)

        val context = mockk<SimulatorContext> {
            every { beregnAlderspensjon2011FoersteUttak(any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_OPPTJ),
            vedtakListe = vedtakListe,
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = null,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = true,
            ignoreAvslag = true
        )

        vedtakListe[0].anbefaltResultatEnum shouldBe VedtakResultatEnum.INNV
        vedtakListe[0].vilkarsvedtakResultatEnum shouldBe VedtakResultatEnum.INNV
        vedtakListe[0].begrunnelseEnum shouldBe null
        vedtakListe[0].merknadListe shouldBe mutableListOf<Merknad>()
    }

    test("beregnAlderspensjon should not modify vedtak when ignoreAvslag is true and vedtak is already innvilget") {
        val vedtak = VilkarsVedtak().apply {
            anbefaltResultatEnum = VedtakResultatEnum.INNV
            vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
        }
        val vedtakListe = mutableListOf(vedtak)

        val context = mockk<SimulatorContext> {
            every { beregnAlderspensjon2011FoersteUttak(any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_OPPTJ),
            vedtakListe = vedtakListe,
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = null,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = true,
            ignoreAvslag = true
        )

        vedtakListe[0].anbefaltResultatEnum shouldBe VedtakResultatEnum.INNV
        vedtakListe[0].vilkarsvedtakResultatEnum shouldBe VedtakResultatEnum.INNV
    }

    test("beregnAlderspensjon should not modify vedtak when ignoreAvslag is false") {
        val vedtak = VilkarsVedtak().apply {
            anbefaltResultatEnum = VedtakResultatEnum.AVSL
            vilkarsvedtakResultatEnum = VedtakResultatEnum.AVSL
        }
        val vedtakListe = mutableListOf(vedtak)

        val context = mockk<SimulatorContext> {
            every { beregnAlderspensjon2011FoersteUttak(any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_OPPTJ),
            vedtakListe = vedtakListe,
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = null,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = true,
            ignoreAvslag = false
        )

        vedtakListe[0].anbefaltResultatEnum shouldBe VedtakResultatEnum.AVSL
        vedtakListe[0].vilkarsvedtakResultatEnum shouldBe VedtakResultatEnum.AVSL
    }

    // ===========================================
    // Tests for exception handling with gjenlevenderett
    // ===========================================

    test("beregnAlderspensjon should throw BadSpecException when gjenlevenderett not supported") {
        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2016(any(), any()) } throws RegelmotorValideringException(
                message = "Original error",
                merknadListe = listOf(
                    merknad("VILKARSVEDTAK_KontrollerVilkarsVedtakLogiskSammenhengRS.VilkarsVedtakKravlinjeMangler"),
                    merknad("VILKARSVEDTAK_KontrollerVilkarsVedtakLogiskSammenhengRS.VilkarsVedtakRelatertPersonFinnesIkke"),
                )
            )
        }

        shouldThrow<BadSpecException> {
            AlderspensjonBeregner(context).beregnAlderspensjon(
                kravhode = kravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ),
                vedtakListe = mutableListOf(gjenlevenderettVedtak()),
                virkningDato = LocalDate.of(2025, 3, 1),
                sisteAldersberegning2011 = SisteAldersberegning2016(),
                privatAfp = null,
                livsvarigOffentligAfpGrunnlag = null,
                simuleringSpec = simuleringSpec(type = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT),
                sakId = null,
                isFoersteUttak = false,
                ignoreAvslag = false
            )
        }.message shouldBe "Pensjonen involverer gjenlevenderett, noe som ikke støttes for simuleringstype ALDER_M_AFP_PRIVAT"
    }

    test("beregnAlderspensjon should rethrow original exception when merknader do not indicate gjenlevende") {
        val originalException = RegelmotorValideringException(
            message = "Original error",
            merknadListe = listOf(merknad("SOME_OTHER_ERROR"))
        )

        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2016(any(), any()) } throws originalException
        }

        shouldThrow<RegelmotorValideringException> {
            AlderspensjonBeregner(context).beregnAlderspensjon(
                kravhode = kravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ),
                vedtakListe = mutableListOf(gjenlevenderettVedtak()),
                virkningDato = LocalDate.of(2025, 3, 1),
                sisteAldersberegning2011 = SisteAldersberegning2016(),
                privatAfp = null,
                livsvarigOffentligAfpGrunnlag = null,
                simuleringSpec = simuleringSpec(),
                sakId = null,
                isFoersteUttak = false,
                ignoreAvslag = false
            )
        } shouldBe originalException
    }

    test("beregnAlderspensjon should rethrow original exception when no GJR vedtak") {
        val originalException = RegelmotorValideringException(
            message = "Original error",
            merknadListe = listOf(
                merknad("VILKARSVEDTAK_KontrollerVilkarsVedtakLogiskSammenhengRS.VilkarsVedtakKravlinjeMangler"),
                merknad("VILKARSVEDTAK_KontrollerVilkarsVedtakLogiskSammenhengRS.VilkarsVedtakRelatertPersonFinnesIkke"),
            )
        )

        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2016(any(), any()) } throws originalException
        }

        val apVedtak = VilkarsVedtak().apply {
            kravlinje = Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
        }

        shouldThrow<RegelmotorValideringException> {
            AlderspensjonBeregner(context).beregnAlderspensjon(
                kravhode = kravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ),
                vedtakListe = mutableListOf(apVedtak), // no GJR vedtak
                virkningDato = LocalDate.of(2025, 3, 1),
                sisteAldersberegning2011 = SisteAldersberegning2016(),
                privatAfp = null,
                livsvarigOffentligAfpGrunnlag = null,
                simuleringSpec = simuleringSpec(),
                sakId = null,
                isFoersteUttak = false,
                ignoreAvslag = false
            )
        } shouldBe originalException
    }

    test("beregnAlderspensjon should rethrow original exception when only one gjenlevende merknad present") {
        val originalException = RegelmotorValideringException(
            message = "Original error",
            merknadListe = listOf(
                merknad("VILKARSVEDTAK_KontrollerVilkarsVedtakLogiskSammenhengRS.VilkarsVedtakKravlinjeMangler"),
                // missing VilkarsVedtakRelatertPersonFinnesIkke
            )
        )

        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2016(any(), any()) } throws originalException
        }

        shouldThrow<RegelmotorValideringException> {
            AlderspensjonBeregner(context).beregnAlderspensjon(
                kravhode = kravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ),
                vedtakListe = mutableListOf(gjenlevenderettVedtak()),
                virkningDato = LocalDate.of(2025, 3, 1),
                sisteAldersberegning2011 = SisteAldersberegning2016(),
                privatAfp = null,
                livsvarigOffentligAfpGrunnlag = null,
                simuleringSpec = simuleringSpec(),
                sakId = null,
                isFoersteUttak = false,
                ignoreAvslag = false
            )
        } shouldBe originalException
    }

    // ===========================================
    // Tests for EPS mottar pensjon functionality
    // ===========================================

    test("beregnAlderspensjon should not create InfoPavirkendeYtelse when EPS does not have pensjon") {
        var capturedRequest: BeregnAlderspensjon2011ForsteUttakRequest? = null
        val context = mockk<SimulatorContext> {
            every { beregnAlderspensjon2011FoersteUttak(any(), any()) } answers {
                capturedRequest = firstArg()
                BeregningsResultatAlderspensjon2011()
            }
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = null,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(epsHarPensjon = false),
            sakId = null,
            isFoersteUttak = true,
            ignoreAvslag = false
        )

        capturedRequest shouldNotBe null
        capturedRequest!!.infoPavirkendeYtelse shouldBe null
    }

    // ===========================================
    // Tests for privat AFP
    // ===========================================

    test("beregnAlderspensjon should pass privatAfp to request for first uttak") {
        var capturedRequest: BeregnAlderspensjon2011ForsteUttakRequest? = null
        val privatAfp = AfpPrivatLivsvarig()

        val context = mockk<SimulatorContext> {
            every { beregnAlderspensjon2011FoersteUttak(any(), any()) } answers {
                capturedRequest = firstArg()
                BeregningsResultatAlderspensjon2011()
            }
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = null,
            privatAfp = privatAfp,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = true,
            ignoreAvslag = false
        )

        capturedRequest!!.afpPrivatLivsvarig shouldBe privatAfp
    }

    test("beregnAlderspensjon should pass privatAfp to request for revurdering") {
        var capturedRequest: RevurderingAlderspensjon2011Request? = null
        val privatAfp = AfpPrivatLivsvarig()

        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2011(any(), any()) } answers {
                capturedRequest = firstArg()
                BeregningsResultatAlderspensjon2011()
            }
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = SisteAldersberegning2011(),
            privatAfp = privatAfp,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = false,
            ignoreAvslag = false
        )

        capturedRequest!!.afpPrivatLivsvarig shouldBe privatAfp
    }

    // ===========================================
    // Tests for livsvarig offentlig AFP
    // ===========================================

    test("beregnAlderspensjon should pass livsvarigOffentligAfpGrunnlag to 2025 request") {
        var capturedRequest: BeregnAlderspensjon2025ForsteUttakRequest? = null
        val offentligAfp = AfpOffentligLivsvarigGrunnlag(sistRegulertG = 100000, bruttoPerAr = 50000.0)

        val context = mockk<SimulatorContext> {
            every { beregnAlderspensjon2025FoersteUttak(any(), any()) } answers {
                capturedRequest = firstArg()
                BeregningsResultatAlderspensjon2025()
            }
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_N_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = null,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = offentligAfp,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = true,
            ignoreAvslag = false
        )

        capturedRequest!!.afpOffentligLivsvarigGrunnlag shouldBe offentligAfp
    }

    test("beregnAlderspensjon should pass livsvarigOffentligAfpGrunnlag to 2025 revurdering request") {
        var capturedRequest: RevurderingAlderspensjon2025Request? = null
        val offentligAfp = AfpOffentligLivsvarigGrunnlag(sistRegulertG = 100000, bruttoPerAr = 50000.0)

        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2025(any(), any()) } answers {
                capturedRequest = firstArg()
                BeregningsResultatAlderspensjon2025()
            }
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_N_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = SisteAldersberegning2011(),
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = offentligAfp,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = false,
            ignoreAvslag = false
        )

        capturedRequest!!.afpOffentligLivsvarigGrunnlag shouldBe offentligAfp
    }

    // ===========================================
    // Tests for request virkFom
    // ===========================================

    test("beregnAlderspensjon should set virkFom in request for first uttak") {
        var capturedRequest: BeregnAlderspensjon2011ForsteUttakRequest? = null
        val context = mockk<SimulatorContext> {
            every { beregnAlderspensjon2011FoersteUttak(any(), any()) } answers {
                capturedRequest = firstArg()
                BeregningsResultatAlderspensjon2011()
            }
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 6, 15),
            sisteAldersberegning2011 = null,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = true,
            ignoreAvslag = false
        )

        capturedRequest!!.virkFom shouldNotBe null
    }

    test("beregnAlderspensjon should set virkFom in request for revurdering") {
        var capturedRequest: RevurderingAlderspensjon2016Request? = null
        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2016(any(), any()) } answers {
                capturedRequest = firstArg()
                BeregningsResultatAlderspensjon2016()
            }
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 6, 15),
            sisteAldersberegning2011 = SisteAldersberegning2016(),
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = false,
            ignoreAvslag = false
        )

        capturedRequest!!.virkFom shouldNotBe null
    }

    // ===========================================
    // Tests for forrige aldersberegning in revurdering
    // ===========================================

    test("beregnAlderspensjon should pass forrigeAldersBeregning to 2011 revurdering request") {
        var capturedRequest: RevurderingAlderspensjon2011Request? = null
        val sisteBeregning = SisteAldersberegning2011().apply { tt_anv = 40 }

        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2011(any(), any()) } answers {
                capturedRequest = firstArg()
                BeregningsResultatAlderspensjon2011()
            }
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = sisteBeregning,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = false,
            ignoreAvslag = false
        )

        capturedRequest!!.forrigeAldersBeregning shouldBe sisteBeregning
    }

    test("beregnAlderspensjon should pass forrigeAldersBeregning to 2016 revurdering request") {
        var capturedRequest: RevurderingAlderspensjon2016Request? = null
        val sisteBeregning = SisteAldersberegning2016()

        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2016(any(), any()) } answers {
                capturedRequest = firstArg()
                BeregningsResultatAlderspensjon2016()
            }
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = sisteBeregning,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = false,
            ignoreAvslag = false
        )

        capturedRequest!!.forrigeAldersBeregning shouldBe sisteBeregning
    }

    test("beregnAlderspensjon should pass sisteAldersBeregning2011 to 2025 revurdering request") {
        var capturedRequest: RevurderingAlderspensjon2025Request? = null
        val sisteBeregning = SisteAldersberegning2011()

        val context = mockk<SimulatorContext> {
            every { revurderAlderspensjon2025(any(), any()) } answers {
                capturedRequest = firstArg()
                BeregningsResultatAlderspensjon2025()
            }
        }

        AlderspensjonBeregner(context).beregnAlderspensjon(
            kravhode = kravhode(RegelverkTypeEnum.N_REG_N_OPPTJ),
            vedtakListe = mutableListOf(),
            virkningDato = LocalDate.of(2025, 1, 1),
            sisteAldersberegning2011 = sisteBeregning,
            privatAfp = null,
            livsvarigOffentligAfpGrunnlag = null,
            simuleringSpec = simuleringSpec(),
            sakId = null,
            isFoersteUttak = false,
            ignoreAvslag = false
        )

        capturedRequest!!.sisteAldersBeregning2011 shouldBe sisteBeregning
    }
})

// ===========================================
// Helper functions
// ===========================================

private fun kravhode(regelverkType: RegelverkTypeEnum) =
    Kravhode().apply {
        regelverkTypeEnum = regelverkType
    }

private fun gjenlevenderettVedtak() =
    VilkarsVedtak().apply {
        kravlinje = Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.GJR }
    }

private fun merknad(kode: String) =
    Merknad().apply {
        this.kode = kode
    }
