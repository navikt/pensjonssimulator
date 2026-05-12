package no.nav.pensjon.simulator.core.beregn

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.to.*
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.vedtak.VilkaarsvedtakKravlinje
import java.time.LocalDate

class AlderspensjonBeregnerTest : ShouldSpec({

    context("førstegangsuttak med forskjellige regelverkstyper") {
        should("call beregnAlderspensjon2011FoersteUttak for N_REG_G_OPPTJ") {
            val expectedResult = BeregningsResultatAlderspensjon2011()
            val context = arrangeAlderspensjon2011FoersteUttak(resultat = expectedResult)

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

        should("call beregnAlderspensjon2016FoersteUttak for N_REG_G_N_OPPTJ") {
            val expectedResult = BeregningsResultatAlderspensjon2016()
            val context = mockk<SimulatorContext> {
                every { beregnAlderspensjon2016FoersteUttak(any(), any()) } returns expectedResult
            }

            AlderspensjonBeregner(context).beregnAlderspensjon(
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
            ) shouldBe expectedResult

            verify(exactly = 1) { context.beregnAlderspensjon2016FoersteUttak(any(), isNull()) }
        }

        should("call beregnAlderspensjon2025FoersteUttak for N_REG_N_OPPTJ") {
            val expectedResult = BeregningsResultatAlderspensjon2025()
            val context = mockk<SimulatorContext> {
                every { beregnAlderspensjon2025FoersteUttak(any(), any()) } returns expectedResult
            }

            AlderspensjonBeregner(context).beregnAlderspensjon(
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
            ) shouldBe expectedResult

            verify(exactly = 1) { context.beregnAlderspensjon2025FoersteUttak(any(), isNull()) }
        }

        should("throw for G_REG regelverkType on first uttak") {
            shouldThrow<RuntimeException> {
                AlderspensjonBeregner(context = mockk()).beregnAlderspensjon(
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

        should("throw for null regelverkType on first uttak") {
            shouldThrow<RuntimeException> {
                AlderspensjonBeregner(context = mockk()).beregnAlderspensjon(
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
    }

    context("revurdering with different regelverkTypes") {
        should("call revurderAlderspensjon2011 for N_REG_G_OPPTJ revurdering") {
            val expectedResult = BeregningsResultatAlderspensjon2011()
            val context = mockk<SimulatorContext> {
                every { revurderAlderspensjon2011(any(), any()) } returns expectedResult
            }

            AlderspensjonBeregner(context).beregnAlderspensjon(
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
            ) shouldBe expectedResult

            verify(exactly = 1) { context.revurderAlderspensjon2011(any(), eq(456L)) }
        }

        should("call revurderAlderspensjon2016 for N_REG_G_N_OPPTJ revurdering") {
            val expectedResult = BeregningsResultatAlderspensjon2016()
            val context = mockk<SimulatorContext> {
                every { revurderAlderspensjon2016(any(), any()) } returns expectedResult
            }

             AlderspensjonBeregner(context).beregnAlderspensjon(
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
            ) shouldBe expectedResult

            verify(exactly = 1) { context.revurderAlderspensjon2016(any(), isNull()) }
        }

        should("call revurderAlderspensjon2025 for N_REG_N_OPPTJ revurdering") {
            val expectedResult = BeregningsResultatAlderspensjon2025()
            val context = mockk<SimulatorContext> {
                every { revurderAlderspensjon2025(any(), any()) } returns expectedResult
            }

             AlderspensjonBeregner(context).beregnAlderspensjon(
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
            ) shouldBe expectedResult

            verify(exactly = 1) { context.revurderAlderspensjon2025(any(), isNull()) }
        }

        should("throw for G_REG regelverkType on revurdering") {
            shouldThrow<RuntimeException> {
                AlderspensjonBeregner(context = mockk()).beregnAlderspensjon(
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
    }

    context("ignoreAvslag functionality") {
        should("innvilge vedtak when ignoreAvslag is true and vedtak is not innvilget") {
            val vedtak = VilkarsVedtak().apply {
                anbefaltResultatEnum = VedtakResultatEnum.AVSL
                vilkarsvedtakResultatEnum = VedtakResultatEnum.AVSL
                begrunnelseEnum = BegrunnelseTypeEnum.ANNULERING
                merknadListe = mutableListOf(Merknad())
            }
            val vedtakListe = mutableListOf(vedtak)
            val context = arrangeAlderspensjon2011FoersteUttak()

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

            with(vedtakListe[0]) {
                anbefaltResultatEnum shouldBe VedtakResultatEnum.INNV
                vilkarsvedtakResultatEnum shouldBe VedtakResultatEnum.INNV
                begrunnelseEnum shouldBe null
                merknadListe shouldBe mutableListOf()
            }
        }

        should("not modify vedtak when ignoreAvslag is true and vedtak is already innvilget") {
            val vedtak = VilkarsVedtak().apply {
                anbefaltResultatEnum = VedtakResultatEnum.INNV
                vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
            }
            val vedtakListe = mutableListOf(vedtak)
            val context = arrangeAlderspensjon2011FoersteUttak()

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

            with(vedtakListe[0]) {
                anbefaltResultatEnum shouldBe VedtakResultatEnum.INNV
                vilkarsvedtakResultatEnum shouldBe VedtakResultatEnum.INNV
            }
        }

        should("not modify vedtak when ignoreAvslag is false") {
            val vedtak = VilkarsVedtak().apply {
                anbefaltResultatEnum = VedtakResultatEnum.AVSL
                vilkarsvedtakResultatEnum = VedtakResultatEnum.AVSL
            }
            val vedtakListe = mutableListOf(vedtak)
            val context = arrangeAlderspensjon2011FoersteUttak()

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

            with(vedtakListe[0]) {
                anbefaltResultatEnum shouldBe VedtakResultatEnum.AVSL
                vilkarsvedtakResultatEnum shouldBe VedtakResultatEnum.AVSL
            }
        }
    }

    context("exception handling of gjenlevenderett") {
        should("throw BadSpecException when gjenlevenderett not supported") {
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

        should("rethrow original exception when merknader do not indicate gjenlevende") {
            val originalException = RegelmotorValideringException(
                message = "Original error",
                merknadListe = listOf(merknad("SOME_OTHER_ERROR"))
            )
            val context = arrangeErrorInRevurderingAlderspensjon2016(originalException)

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

        should("rethrow original exception when no GJR vedtak") {
            val originalException = RegelmotorValideringException(
                message = "Original error",
                merknadListe = listOf(
                    merknad("VILKARSVEDTAK_KontrollerVilkarsVedtakLogiskSammenhengRS.VilkarsVedtakKravlinjeMangler"),
                    merknad("VILKARSVEDTAK_KontrollerVilkarsVedtakLogiskSammenhengRS.VilkarsVedtakRelatertPersonFinnesIkke"),
                )
            )
            val context = arrangeErrorInRevurderingAlderspensjon2016(originalException)

            val alderspensjonsvedtak = VilkarsVedtak().apply {
                kravlinje = VilkaarsvedtakKravlinje(type = KravlinjeTypeEnum.AP, person = null)
                kravlinjeTypeEnum = KravlinjeTypeEnum.AP
            }

            shouldThrow<RegelmotorValideringException> {
                AlderspensjonBeregner(context).beregnAlderspensjon(
                    kravhode = kravhode(RegelverkTypeEnum.N_REG_G_N_OPPTJ),
                    vedtakListe = mutableListOf(alderspensjonsvedtak), // no GJR vedtak
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

        should("rethrow original exception when only one gjenlevende merknad present") {
            val originalException = RegelmotorValideringException(
                message = "Original error",
                merknadListe = listOf(
                    merknad("VILKARSVEDTAK_KontrollerVilkarsVedtakLogiskSammenhengRS.VilkarsVedtakKravlinjeMangler"),
                    // missing VilkarsVedtakRelatertPersonFinnesIkke
                )
            )
            val context = arrangeErrorInRevurderingAlderspensjon2016(originalException)

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
    }

    context("EPS mottar pensjon") {
        should("not create InfoPavirkendeYtelse when EPS does not have pensjon") {
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
    }

    context("privat AFP") {
        should("pass privatAfp to request for first uttak") {
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

        should("pass privatAfp to request for revurdering") {
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
    }

    context("livsvarig offentlig AFP") {
        should("pass livsvarigOffentligAfpGrunnlag to 2025 request") {
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

        should("pass livsvarigOffentligAfpGrunnlag to 2025 revurdering request") {
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
    }

    context("request 'virkning fra og med'-dato") {
        should("set virkFom in request for first uttak") {
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

            capturedRequest!!.virkFomLd shouldNotBe null
        }

        should("set virkFom in request for revurdering") {
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
    }

    context("forrige alderspensjonsberegning i revurdering") {
        should("pass forrigeAldersBeregning to 2011 revurdering request") {
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

        should("pass forrigeAldersBeregning to 2016 revurdering request") {
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

        should("pass sisteAldersBeregning2011 to 2025 revurdering request") {
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
    }
})

private fun arrangeAlderspensjon2011FoersteUttak(
    resultat: BeregningsResultatAlderspensjon2011 = BeregningsResultatAlderspensjon2011()
): SimulatorContext =
    mockk<SimulatorContext> {
        every { beregnAlderspensjon2011FoersteUttak(any(), any()) } returns resultat
    }

private fun arrangeErrorInRevurderingAlderspensjon2016(exception: RegelmotorValideringException): SimulatorContext =
    mockk<SimulatorContext> {
        every { revurderAlderspensjon2016(any(), any()) } throws exception
    }

private fun kravhode(regelverkType: RegelverkTypeEnum) =
    Kravhode().apply {
        regelverkTypeEnum = regelverkType
    }

private fun gjenlevenderettVedtak() =
    VilkarsVedtak().apply {
        kravlinje = VilkaarsvedtakKravlinje(type = KravlinjeTypeEnum.GJR, person = null)
        kravlinjeTypeEnum = KravlinjeTypeEnum.GJR
    }

private fun merknad(kode: String) =
    Merknad().apply {
        this.kode = kode
    }
