package no.nav.pensjon.simulator.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonOpptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.domain.regler.to.*
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.regel.client.GenericRegelClient
import org.springframework.cache.caffeine.CaffeineCacheManager
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate

class SimulatorContextTest : FunSpec({

    test("beregnAlderspensjon2011FoersteUttak skal returnere beregningsresultat med nullstilte felter") {
        val regelService = mockk<GenericRegelClient>()
        val beregningsResultat = BeregningsResultatAlderspensjon2011().apply {
            virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
            epsMottarPensjon = true
        }
        val response = BeregnAlderspensjon2011ForsteUttakResponse().apply {
            this.beregningsResultat = beregningsResultat
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<BeregnAlderspensjon2011ForsteUttakResponse, BeregnAlderspensjon2011ForsteUttakRequest>(
                any(), any(), any(), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.beregnAlderspensjon2011FoersteUttak(
            spec = BeregnAlderspensjon2011ForsteUttakRequest(),
            sakId = 123L
        )

        result.virkTom shouldBe null
        result.epsMottarPensjon shouldBe false
        result.beregningsinformasjon shouldBe null
    }

    test("beregnAlderspensjon2011FoersteUttak skal kaste exception når beregningsResultat er null") {
        val regelService = mockk<GenericRegelClient>()
        val response = BeregnAlderspensjon2011ForsteUttakResponse().apply {
            beregningsResultat = null
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<BeregnAlderspensjon2011ForsteUttakResponse, BeregnAlderspensjon2011ForsteUttakRequest>(
                any(), any(), any(), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        shouldThrow<RuntimeException> {
            context.beregnAlderspensjon2011FoersteUttak(
                spec = BeregnAlderspensjon2011ForsteUttakRequest(),
                sakId = 123L
            )
        }.message shouldBe "No beregningsResultat from beregnAlderspensjon2011ForsteUttak"
    }

    test("beregnAlderspensjon2016FoersteUttak skal returnere beregningsresultat med nullstilte felter") {
        val regelService = mockk<GenericRegelClient>()
        val beregningsResultat = BeregningsResultatAlderspensjon2016().apply {
            virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
            epsMottarPensjon = true
        }
        val response = BeregnAlderspensjon2016ForsteUttakResponse().apply {
            this.beregningsResultat = beregningsResultat
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<BeregnAlderspensjon2016ForsteUttakResponse, BeregnAlderspensjon2016ForsteUttakRequest>(
                any(), any(), any(), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.beregnAlderspensjon2016FoersteUttak(
            spec = BeregnAlderspensjon2016ForsteUttakRequest(),
            sakId = 123L
        )

        result.virkTom shouldBe null
        result.epsMottarPensjon shouldBe false
        result.beregningsinformasjon shouldBe null
    }

    test("beregnAlderspensjon2016FoersteUttak skal kaste exception når beregningsResultat er null") {
        val regelService = mockk<GenericRegelClient>()
        val response = BeregnAlderspensjon2016ForsteUttakResponse().apply {
            beregningsResultat = null
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<BeregnAlderspensjon2016ForsteUttakResponse, BeregnAlderspensjon2016ForsteUttakRequest>(
                any(), any(), any(), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        shouldThrow<RuntimeException> {
            context.beregnAlderspensjon2016FoersteUttak(
                spec = BeregnAlderspensjon2016ForsteUttakRequest(),
                sakId = 123L
            )
        }.message shouldBe "No beregningsResultat from beregnAlderspensjon2016ForsteUttak"
    }

    test("beregnAlderspensjon2025FoersteUttak skal returnere beregningsresultat med nullstilte felter") {
        val regelService = mockk<GenericRegelClient>()
        val beregningsResultat = BeregningsResultatAlderspensjon2025().apply {
            virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
            epsMottarPensjon = true
        }
        val response = BeregnAlderspensjon2025ForsteUttakResponse().apply {
            this.beregningsResultat = beregningsResultat
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<BeregnAlderspensjon2025ForsteUttakResponse, BeregnAlderspensjon2025ForsteUttakRequest>(
                any(), any(), any(), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.beregnAlderspensjon2025FoersteUttak(
            spec = BeregnAlderspensjon2025ForsteUttakRequest(),
            sakId = 123L
        )

        result.virkTom shouldBe null
        result.epsMottarPensjon shouldBe false
        result.beregningsinformasjon shouldBe null
    }

    test("beregnAlderspensjon2025FoersteUttak skal kaste exception når beregningsResultat er null") {
        val regelService = mockk<GenericRegelClient>()
        val response = BeregnAlderspensjon2025ForsteUttakResponse().apply {
            beregningsResultat = null
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<BeregnAlderspensjon2025ForsteUttakResponse, BeregnAlderspensjon2025ForsteUttakRequest>(
                any(), any(), any(), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        shouldThrow<RuntimeException> {
            context.beregnAlderspensjon2025FoersteUttak(
                spec = BeregnAlderspensjon2025ForsteUttakRequest(),
                sakId = 123L
            )
        }.message shouldBe "No beregningsResultat from beregnAlderspensjon2025ForsteUttak"
    }

    test("revurderAlderspensjon2011 skal returnere revurdert beregningsresultat") {
        val regelService = mockk<GenericRegelClient>()
        val revurdertResultat = BeregningsResultatAlderspensjon2011()
        val response = RevurderingAlderspensjon2011Response().apply {
            revurdertBeregningsResultat = revurdertResultat
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<RevurderingAlderspensjon2011Response, RevurderingAlderspensjon2011Request>(
                any(), any(), any(), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.revurderAlderspensjon2011(
            spec = RevurderingAlderspensjon2011Request(),
            sakId = 123L
        )

        result shouldBe revurdertResultat
    }

    test("revurderAlderspensjon2016 skal returnere revurdert beregningsresultat") {
        val regelService = mockk<GenericRegelClient>()
        val revurdertResultat = BeregningsResultatAlderspensjon2016()
        val response = RevurderingAlderspensjon2016Response().apply {
            revurdertBeregningsResultat = revurdertResultat
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<RevurderingAlderspensjon2016Response, RevurderingAlderspensjon2016Request>(
                any(), any(), any(), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.revurderAlderspensjon2016(
            spec = RevurderingAlderspensjon2016Request(),
            sakId = 123L
        )

        result shouldBe revurdertResultat
    }

    test("revurderAlderspensjon2025 skal returnere revurdert beregningsresultat") {
        val regelService = mockk<GenericRegelClient>()
        val revurdertResultat = BeregningsResultatAlderspensjon2025()
        val response = RevurderingAlderspensjon2025Response().apply {
            revurdertBeregningsResultat = revurdertResultat
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<RevurderingAlderspensjon2025Response, RevurderingAlderspensjon2025Request>(
                any(), any(), any(), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.revurderAlderspensjon2025(
            spec = RevurderingAlderspensjon2025Request(),
            sakId = 123L
        )

        result shouldBe revurdertResultat
    }

    test("simulerPre2025OffentligAfp skal returnere simuleringsresultat") {
        val regelService = mockk<GenericRegelClient>()
        val simuleringsResultat = Simuleringsresultat()
        val response = SimuleringResponse().apply {
            this.simuleringsResultat = simuleringsResultat
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<SimuleringResponse, SimuleringRequest>(
                any(), any(), eq("simulerAFP"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.simulerPre2025OffentligAfp(SimuleringRequest())

        result shouldBe simuleringsResultat
    }

    test("simulerPre2025OffentligAfp skal kaste exception når simuleringsResultat er null") {
        val regelService = mockk<GenericRegelClient>()
        val response = SimuleringResponse().apply {
            simuleringsResultat = null
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<SimuleringResponse, SimuleringRequest>(
                any(), any(), eq("simulerAFP"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        shouldThrow<RuntimeException> {
            context.simulerPre2025OffentligAfp(SimuleringRequest())
        }.message shouldBe "Simuleringsresultat is null"
    }

    test("simulerVilkarsprovPre2025OffentligAfp skal returnere simuleringsresultat") {
        val regelService = mockk<GenericRegelClient>()
        val simuleringsResultat = Simuleringsresultat()
        val response = SimuleringResponse().apply {
            this.simuleringsResultat = simuleringsResultat
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<SimuleringResponse, SimuleringRequest>(
                any(), any(), eq("simulerVilkarsprovAFP"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.simulerVilkarsprovPre2025OffentligAfp(SimuleringRequest())

        result shouldBe simuleringsResultat
    }

    test("vilkaarsproevUbetingetAlderspensjon skal returnere vedtaksliste") {
        val regelService = mockk<GenericRegelClient>()
        val vedtak = VilkarsVedtak()
        val response = VilkarsprovResponse().apply {
            vedtaksliste = mutableListOf(vedtak)
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<VilkarsprovResponse, VilkarsprovRequest>(
                any(), any(), eq("vilkarsprovAlderspensjonOver67"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.vilkaarsproevUbetingetAlderspensjon(
            spec = VilkarsprovRequest(),
            sakId = 123L
        )

        result.size shouldBe 1
        result[0] shouldBe vedtak
    }

    test("vilkaarsproevAlderspensjon2011 skal returnere vedtaksliste") {
        val regelService = mockk<GenericRegelClient>()
        val vedtak = VilkarsVedtak()
        val response = VilkarsprovResponse().apply {
            vedtaksliste = mutableListOf(vedtak)
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<VilkarsprovResponse, VilkarsprovAlderpensjon2011Request>(
                any(), any(), eq("vilkarsprovAlderspensjon2011"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.vilkaarsproevAlderspensjon2011(
            spec = VilkarsprovAlderpensjon2011Request(),
            sakId = 123L
        )

        result.size shouldBe 1
    }

    test("vilkaarsproevAlderspensjon2016 skal returnere vedtaksliste") {
        val regelService = mockk<GenericRegelClient>()
        val vedtak = VilkarsVedtak()
        val response = VilkarsprovResponse().apply {
            vedtaksliste = mutableListOf(vedtak)
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<VilkarsprovResponse, VilkarsprovAlderpensjon2016Request>(
                any(), any(), eq("vilkarsprovAlderspensjon2016"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.vilkaarsproevAlderspensjon2016(
            spec = VilkarsprovAlderpensjon2016Request(),
            sakId = 123L
        )

        result.size shouldBe 1
    }

    test("vilkaarsproevAlderspensjon2025 skal returnere vedtaksliste") {
        val regelService = mockk<GenericRegelClient>()
        val vedtak = VilkarsVedtak()
        val response = VilkarsprovResponse().apply {
            vedtaksliste = mutableListOf(vedtak)
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<VilkarsprovResponse, VilkarsprovAlderpensjon2025Request>(
                any(), any(), eq("vilkarsprovAlderspensjon2025"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.vilkaarsproevAlderspensjon2025(
            spec = VilkarsprovAlderpensjon2025Request(),
            sakId = 123L
        )

        result.size shouldBe 1
    }

    test("beregnPrivatAfp skal returnere beregningsresultat") {
        val regelService = mockk<GenericRegelClient>()
        val afpResultat = BeregningsResultatAfpPrivat()
        val response = BeregnAfpPrivatResponse().apply {
            beregningsResultatAfpPrivat = afpResultat
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<BeregnAfpPrivatResponse, BeregnAfpPrivatRequest>(
                any(), any(), eq("beregnAfpPrivat"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.beregnPrivatAfp(
            spec = BeregnAfpPrivatRequest(),
            sakId = 123L
        )

        result shouldBe afpResultat
    }

    test("refreshFastsettTrygdetid skal returnere TrygdetidResponse") {
        val regelService = mockk<GenericRegelClient>()
        val response = TrygdetidResponse().apply {
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<TrygdetidResponse, TrygdetidRequest>(
                any(), any(), eq("fastsettTrygdetid"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.refreshFastsettTrygdetid(
            spec = TrygdetidRequest(),
            kravIsUfoeretrygd = false,
            sakId = 123L
        )

        result shouldBe response
    }

    test("beregnOpptjening skal returnere pensjonsbeholdninger") {
        val regelService = mockk<GenericRegelClient>()
        val beholdning = Pensjonsbeholdning().apply {
            fom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
            tom = LocalDate.of(2025, 12, 31).toNorwegianDateAtNoon()
        }
        val response = BeregnPensjonsBeholdningResponse().apply {
            beholdninger = arrayListOf(beholdning)
            pakkseddel = Pakkseddel()
        }
        every {
            regelService.makeRegelCall<BeregnPensjonsBeholdningResponse, BeregnPensjonsBeholdningRequest>(
                any(), any(), eq("beregnPensjonsBeholdning"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val persongrunnlag = Persongrunnlag().apply {
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    virkFom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                    virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                }
            )
        }

        val result = context.beregnOpptjening(
            beholdningTom = LocalDate.of(2025, 12, 31),
            persongrunnlag = persongrunnlag,
            beholdning = null
        )

        result.size shouldBe 1
    }

    test("fetchGrunnbeloepListe skal hente grunnbeløp fra regelService") {
        val regelService = mockk<GenericRegelClient>()
        val satsResponse = SatsResponse()
        every {
            regelService.makeRegelCall<SatsResponse, HentGrunnbelopListeRequest>(
                any(), any(), eq("hentGrunnbelopListe"), any(), any()
            )
        } returns satsResponse

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val result = context.fetchGrunnbeloepListe(LocalDate.of(2024, 5, 1))

        result shouldBe satsResponse
    }

    test("fetchGrunnbeloepListe skal bruke cache ved gjentatte kall") {
        val regelService = mockk<GenericRegelClient>()
        val satsResponse = SatsResponse()
        every {
            regelService.makeRegelCall<SatsResponse, HentGrunnbelopListeRequest>(
                any(), any(), eq("hentGrunnbelopListe"), any(), any()
            )
        } returns satsResponse

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val dato = LocalDate.of(2024, 5, 1)

        // Første kall
        context.fetchGrunnbeloepListe(dato)

        // Andre kall med samme dato - skal bruke cache
        context.fetchGrunnbeloepListe(dato)

        // Regelservice skal kun kalles én gang pga caching
        verify(exactly = 1) {
            regelService.makeRegelCall<SatsResponse, HentGrunnbelopListeRequest>(
                any(), any(), eq("hentGrunnbelopListe"), any(), any()
            )
        }
    }

    test("beregnPoengtallBatch skal kalle regelService og returnere opptjeningsgrunnlag") {
        val regelService = mockk<GenericRegelClient>()
        val opptjeningsgrunnlag = Opptjeningsgrunnlag().apply {
            ar = 2020
            pi = 500000
        }
        val personOpptjeningsgrunnlag = PersonOpptjeningsgrunnlag().apply {
            opptjening = opptjeningsgrunnlag
        }
        val response = BeregnPoengtallBatchResponse().apply {
            personOpptjeningsgrunnlagListe = mutableListOf(personOpptjeningsgrunnlag)
        }
        every {
            regelService.makeRegelCall<BeregnPoengtallBatchResponse, BeregnPoengtallBatchRequest>(
                any(), any(), eq("beregnPoengtallBatch"), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        val inputOpptjeningsgrunnlag = Opptjeningsgrunnlag().apply {
            ar = 2020
            pi = 500000
        }

        val result = context.beregnPoengtallBatch(
            opptjeningGrunnlagListe = mutableListOf(inputOpptjeningsgrunnlag),
            foedselsdato = LocalDate.of(1963, 5, 15)
        )

        result.size shouldBe 1
        result[0].ar shouldBe 2020
    }

    test("beregnAlderspensjon2011FoersteUttak skal kaste exception ved valideringsfeil i pakkseddel") {
        val regelService = mockk<GenericRegelClient>()
        val beregningsResultat = BeregningsResultatAlderspensjon2011()
        val response = BeregnAlderspensjon2011ForsteUttakResponse().apply {
            this.beregningsResultat = beregningsResultat
            pakkseddel = Pakkseddel().apply {
                merknadListe = mutableListOf(Merknad().apply { kode = "ERROR_CODE" })
            }
        }
        every {
            regelService.makeRegelCall<BeregnAlderspensjon2011ForsteUttakResponse, BeregnAlderspensjon2011ForsteUttakRequest>(
                any(), any(), any(), any(), any()
            )
        } returns response

        val context = SimulatorContext(
            regelService = regelService,
            objectMapper = JsonMapper(),
            cacheManager = CaffeineCacheManager()
        )

        shouldThrow<RegelmotorValideringException> {
            context.beregnAlderspensjon2011FoersteUttak(
                spec = BeregnAlderspensjon2011ForsteUttakRequest(),
                sakId = 123L
            )
        }
    }
})
