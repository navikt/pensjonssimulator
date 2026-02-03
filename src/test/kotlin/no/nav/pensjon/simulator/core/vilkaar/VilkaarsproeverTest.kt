package no.nav.pensjon.simulator.core.vilkaar

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import java.time.LocalDate

class VilkaarsproeverTest : FunSpec({

    context("innvilgetVedtak") {

        test("oppretter vedtak med korrekt forsteVirk") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val virkningFom = LocalDate.of(2025, 1, 1)
            val kravlinje = Kravlinje().apply {
                kravlinjeTypeEnum = KravlinjeTypeEnum.AP
            }

            val vedtak = vilkaarsproever.innvilgetVedtak(kravlinje, virkningFom)

            vedtak.forsteVirk shouldNotBe null
            vedtak.vilkarsvedtakResultatEnum shouldBe VedtakResultatEnum.INNV
            vedtak.kravlinje shouldBe kravlinje
        }

        test("oppretter vedtak med korrekt virkFom") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val virkningFom = LocalDate.of(2025, 6, 15)
            val kravlinje = Kravlinje().apply {
                kravlinjeTypeEnum = KravlinjeTypeEnum.AP
            }

            val vedtak = vilkaarsproever.innvilgetVedtak(kravlinje, virkningFom)

            vedtak.virkFom shouldNotBe null
        }

        test("oppretter vedtak med kravlinjeType fra kravlinje") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val kravlinje = Kravlinje().apply {
                kravlinjeTypeEnum = KravlinjeTypeEnum.GJR
            }

            val vedtak = vilkaarsproever.innvilgetVedtak(kravlinje, LocalDate.of(2025, 1, 1))

            vedtak.kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.GJR
        }

        test("håndterer null kravlinje") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val vedtak = vilkaarsproever.innvilgetVedtak(null, LocalDate.of(2025, 1, 1))

            vedtak.kravlinje shouldBe null
            vedtak.kravlinjeTypeEnum shouldBe null
            vedtak.vilkarsvedtakResultatEnum shouldBe VedtakResultatEnum.INNV
        }
    }

    context("vilkaarsproevKrav") {

        test("vilkårsprøver med ubetinget alderspensjon når virkning er etter normert pensjonsalder") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1960, 5, 15)
            val normertPensjonsdato = LocalDate.of(2027, 5, 15) // 67 år
            val virkningFom = LocalDate.of(2028, 1, 1) // etter normert alder

            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
                )
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato
            every { context.vilkaarsproevUbetingetAlderspensjon(any(), any()) } returns mutableListOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.INNV
                    kravlinje = kravhode.kravlinjeListe[0]
                }
            )

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = LocalDate.of(2025, 1, 1),
                avdoedFoersteVirkning = null,
                sakId = 123L,
                ignoreAvslag = false
            )

            val result = vilkaarsproever.vilkaarsproevKrav(spec)
            val vedtakListe = result.first
            val resultKravhode = result.second

            verify { context.vilkaarsproevUbetingetAlderspensjon(any(), 123L) }
            vedtakListe shouldHaveSize 1
            resultKravhode shouldBe kravhode
        }

        test("vilkårsprøver med betinget alderspensjon 2011 for N_REG_G_OPPTJ regelverk") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1963, 5, 15)
            val normertPensjonsdato = LocalDate.of(2030, 5, 15)
            val virkningFom = LocalDate.of(2025, 1, 1) // før normert alder

            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
                )
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato
            every { context.vilkaarsproevAlderspensjon2011(any(), any()) } returns mutableListOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.INNV
                    kravlinje = kravhode.kravlinjeListe[0]
                }
            )

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = LocalDate.of(2025, 1, 1),
                avdoedFoersteVirkning = null,
                sakId = 123L,
                ignoreAvslag = false
            )

            val result = vilkaarsproever.vilkaarsproevKrav(spec)

            verify { context.vilkaarsproevAlderspensjon2011(any(), 123L) }
            result.first shouldHaveSize 1
        }

        test("vilkårsprøver med betinget alderspensjon 2016 for N_REG_G_N_OPPTJ regelverk") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1965, 5, 15)
            val normertPensjonsdato = LocalDate.of(2032, 5, 15)
            val virkningFom = LocalDate.of(2027, 1, 1)

            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
                )
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato
            every { context.vilkaarsproevAlderspensjon2016(any(), any()) } returns mutableListOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.INNV
                    kravlinje = kravhode.kravlinjeListe[0]
                }
            )

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = LocalDate.of(2025, 1, 1),
                avdoedFoersteVirkning = null,
                sakId = 123L,
                ignoreAvslag = false
            )

            val result = vilkaarsproever.vilkaarsproevKrav(spec)

            verify { context.vilkaarsproevAlderspensjon2016(any(), 123L) }
            result.first shouldHaveSize 1
        }

        test("vilkårsprøver med betinget alderspensjon 2025 for N_REG_N_OPPTJ regelverk") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1970, 5, 15)
            val normertPensjonsdato = LocalDate.of(2040, 5, 15)
            val virkningFom = LocalDate.of(2035, 1, 1)

            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
                )
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato
            every { context.vilkaarsproevAlderspensjon2025(any(), any()) } returns mutableListOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.INNV
                    kravlinje = kravhode.kravlinjeListe[0]
                }
            )

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = LocalDate.of(2025, 1, 1),
                avdoedFoersteVirkning = null,
                sakId = 123L,
                ignoreAvslag = false
            )

            val result = vilkaarsproever.vilkaarsproevKrav(spec)

            verify { context.vilkaarsproevAlderspensjon2025(any(), 123L) }
            result.first shouldHaveSize 1
        }

        test("legger til gjenlevenderett vedtak når GJR kravlinje finnes") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1960, 5, 15)
            val normertPensjonsdato = LocalDate.of(2027, 5, 15)
            val virkningFom = LocalDate.of(2028, 1, 1)

            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP },
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.GJR }
                )
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato
            every { context.vilkaarsproevUbetingetAlderspensjon(any(), any()) } returns mutableListOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.INNV
                    kravlinje = kravhode.kravlinjeListe[0]
                }
            )

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = LocalDate.of(2025, 1, 1),
                avdoedFoersteVirkning = null,
                sakId = 123L,
                ignoreAvslag = false
            )

            val result = vilkaarsproever.vilkaarsproevKrav(spec)
            val vedtakListe = result.first

            // Skal ha både AP og GJR vedtak
            vedtakListe shouldHaveSize 2
            vedtakListe.any { v -> v.kravlinjeTypeEnum == KravlinjeTypeEnum.GJR } shouldBe true
        }

        test("setter kravlinjeStatus og land på vedtak og kravlinjer") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1960, 5, 15)
            val normertPensjonsdato = LocalDate.of(2027, 5, 15)
            val virkningFom = LocalDate.of(2028, 1, 1)

            val apKravlinje = Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(apKravlinje)
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato
            every { context.vilkaarsproevUbetingetAlderspensjon(any(), any()) } returns mutableListOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.INNV
                    kravlinje = apKravlinje
                }
            )

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = LocalDate.of(2025, 1, 1),
                avdoedFoersteVirkning = null,
                sakId = 123L,
                ignoreAvslag = false
            )

            val result = vilkaarsproever.vilkaarsproevKrav(spec)
            val vedtakListe = result.first
            val resultKravhode = result.second

            // Verifiser at kravlinje på vedtak er oppdatert
            vedtakListe[0].kravlinje!!.kravlinjeStatus shouldBe KravlinjeStatus.VILKARSPROVD
            vedtakListe[0].kravlinje!!.land shouldBe LandkodeEnum.NOR

            // Verifiser at kravlinjer i kravhode er oppdatert
            resultKravhode.kravlinjeListe[0].kravlinjeStatus shouldBe KravlinjeStatus.VILKARSPROVD
            resultKravhode.kravlinjeListe[0].land shouldBe LandkodeEnum.NOR
        }

        test("setter vilkarsvedtakResultat fra anbefaltResultat") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1960, 5, 15)
            val normertPensjonsdato = LocalDate.of(2027, 5, 15)
            val virkningFom = LocalDate.of(2028, 1, 1)

            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
                )
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato
            every { context.vilkaarsproevUbetingetAlderspensjon(any(), any()) } returns mutableListOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.INNV
                    vilkarsvedtakResultatEnum = VedtakResultatEnum.VELG // Satt til noe annet
                    kravlinje = kravhode.kravlinjeListe[0]
                }
            )

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = LocalDate.of(2025, 1, 1),
                avdoedFoersteVirkning = null,
                sakId = 123L,
                ignoreAvslag = false
            )

            val result = vilkaarsproever.vilkaarsproevKrav(spec)
            val vedtakListe = result.first

            // vilkarsvedtakResultat skal settes til anbefaltResultat
            vedtakListe[0].vilkarsvedtakResultatEnum shouldBe VedtakResultatEnum.INNV
        }

        test("setter forsteVirk fra soekerFoersteVirkning") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1960, 5, 15)
            val normertPensjonsdato = LocalDate.of(2027, 5, 15)
            val virkningFom = LocalDate.of(2028, 1, 1)
            val soekerFoersteVirkning = LocalDate.of(2020, 6, 1)

            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
                )
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato
            every { context.vilkaarsproevUbetingetAlderspensjon(any(), any()) } returns mutableListOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.INNV
                    kravlinje = kravhode.kravlinjeListe[0]
                }
            )

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = soekerFoersteVirkning,
                avdoedFoersteVirkning = null,
                sakId = 123L,
                ignoreAvslag = false
            )

            val result = vilkaarsproever.vilkaarsproevKrav(spec)
            val vedtakListe = result.first

            vedtakListe[0].forsteVirk shouldNotBe null
        }

        test("bruker avdoedFoersteVirkning for GJR vedtak") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1960, 5, 15)
            val normertPensjonsdato = LocalDate.of(2027, 5, 15)
            val virkningFom = LocalDate.of(2028, 1, 1)
            val soekerFoersteVirkning = LocalDate.of(2020, 6, 1)
            val avdoedFoersteVirkning = LocalDate.of(2018, 3, 1)

            val gjrKravlinje = Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.GJR }
            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP },
                    gjrKravlinje
                )
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato
            every { context.vilkaarsproevUbetingetAlderspensjon(any(), any()) } returns mutableListOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.INNV
                    kravlinje = kravhode.kravlinjeListe[0]
                }
            )

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = soekerFoersteVirkning,
                avdoedFoersteVirkning = avdoedFoersteVirkning,
                sakId = 123L,
                ignoreAvslag = false
            )

            val result = vilkaarsproever.vilkaarsproevKrav(spec)
            val vedtakListe = result.first

            // Skal ha GJR vedtak som bruker avdoedFoersteVirkning
            val gjrVedtak = vedtakListe.first { v -> v.kravlinjeTypeEnum == KravlinjeTypeEnum.GJR }
            gjrVedtak.forsteVirk shouldNotBe null
        }

        test("ignoreAvslag=true hopper over avslag-håndtering") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1960, 5, 15)
            val normertPensjonsdato = LocalDate.of(2027, 5, 15)
            val virkningFom = LocalDate.of(2028, 1, 1)

            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
                )
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato
            // Returnerer avslått vedtak uten begrunnelse, som normalt ville kastet exception
            every { context.vilkaarsproevUbetingetAlderspensjon(any(), any()) } returns mutableListOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.AVSL
                    begrunnelseEnum = null
                    kravlinje = kravhode.kravlinjeListe[0]
                }
            )

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = LocalDate.of(2025, 1, 1),
                avdoedFoersteVirkning = null,
                sakId = 123L,
                ignoreAvslag = true
            )

            // Skal ikke kaste exception når ignoreAvslag=true
            val result = vilkaarsproever.vilkaarsproevKrav(spec)
            result.first shouldHaveSize 1
        }

        test("returnerer tom vedtak liste når regelverk ikke matcher") {
            val context = mockk<SimulatorContext>()
            val normalderService = mockk<NormertPensjonsalderService>()

            val fodselsdato = LocalDate.of(1963, 5, 15)
            val normertPensjonsdato = LocalDate.of(2030, 5, 15)
            val virkningFom = LocalDate.of(2025, 1, 1)

            val soekerGrunnlag = createPersongrunnlag(fodselsdato, GrunnlagsrolleEnum.SOKER)
            val kravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(soekerGrunnlag)
                kravlinjeListe = mutableListOf(
                    Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
                )
                regelverkTypeEnum = null // Ingen regelverkstype satt
            }

            every { normalderService.normertPensjoneringsdato(fodselsdato) } returns normertPensjonsdato

            val vilkaarsproever = Vilkaarsproever(context, normalderService)
            val spec = VilkaarsproevingSpec(
                livsvarigOffentligAfpGrunnlag = null,
                privatAfp = null,
                virkningFom = virkningFom,
                kravhode = kravhode,
                afpFoersteVirkning = null,
                sisteBeregning = null,
                forrigeVedtakListe = emptyList(),
                soekerFoersteVirkning = LocalDate.of(2025, 1, 1),
                avdoedFoersteVirkning = null,
                sakId = 123L,
                ignoreAvslag = true
            )

            val result = vilkaarsproever.vilkaarsproevKrav(spec)
            result.first shouldHaveSize 0
        }
    }
})

private fun createPersongrunnlag(
    fodselsdato: LocalDate,
    rolle: GrunnlagsrolleEnum
) = Persongrunnlag().apply {
    this.fodselsdato = fodselsdato.toNorwegianDateAtNoon()
    personDetaljListe = mutableListOf(
        PersonDetalj().apply {
            grunnlagsrolleEnum = rolle
            bruk = true
        }
    )
}
