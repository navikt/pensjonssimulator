package no.nav.pensjon.simulator.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
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
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.domain.regler.to.*
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.regel.client.GenericRegelClient
import org.springframework.cache.caffeine.CaffeineCacheManager
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate

class SimulatorContextTest : ShouldSpec({

    context("beregnAlderspensjon2011FoersteUttak") {
        should("returnere beregningsresultat med nullstilte felter") {
            val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                epsMottarPensjon = true
            }
            val regelmotor =
                arrangeRegler<BeregnAlderspensjon2011ForsteUttakResponse, BeregnAlderspensjon2011ForsteUttakRequest>(
                    response = BeregnAlderspensjon2011ForsteUttakResponse().apply {
                        beregningsResultat = beregningsresultat
                        pakkseddel = Pakkseddel()
                    }
                )

            val result = simulatorContext(regelmotor).beregnAlderspensjon2011FoersteUttak(
                spec = BeregnAlderspensjon2011ForsteUttakRequest(),
                sakId = 123L
            )

            with(result) {
                virkTom shouldBe null
                epsMottarPensjon shouldBe false
                beregningsinformasjon shouldBe null
            }
        }

        should("kaste exception når beregningsresultat er null") {
            val regelmotor =
                arrangeRegler<BeregnAlderspensjon2011ForsteUttakResponse, BeregnAlderspensjon2011ForsteUttakRequest>(
                    response = BeregnAlderspensjon2011ForsteUttakResponse().apply {
                        beregningsResultat = null
                        pakkseddel = Pakkseddel()
                    })

            shouldThrow<RuntimeException> {
                simulatorContext(regelmotor).beregnAlderspensjon2011FoersteUttak(
                    spec = BeregnAlderspensjon2011ForsteUttakRequest(),
                    sakId = 123L
                )
            }.message shouldBe "No beregningsResultat from beregnAlderspensjon2011ForsteUttak"
        }

        should("kaste exception ved valideringsfeil i pakkseddel") {
            val beregningsresultat = BeregningsResultatAlderspensjon2011()
            val regelmotor =
                arrangeRegler<BeregnAlderspensjon2011ForsteUttakResponse, BeregnAlderspensjon2011ForsteUttakRequest>(
                    response = BeregnAlderspensjon2011ForsteUttakResponse().apply {
                        beregningsResultat = beregningsresultat
                        pakkseddel = Pakkseddel().apply {
                            merknadListe = mutableListOf(Merknad().apply { kode = "ERROR_CODE" })
                        }
                    }
                )

            shouldThrow<RegelmotorValideringException> {
                simulatorContext(regelmotor).beregnAlderspensjon2011FoersteUttak(
                    spec = BeregnAlderspensjon2011ForsteUttakRequest(),
                    sakId = 123L
                )
            }
        }
    }

    context("beregnAlderspensjon2016FoersteUttak") {
        should("returnere beregningsresultat med nullstilte felter") {
            val beregningsresultat = BeregningsResultatAlderspensjon2016().apply {
                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                epsMottarPensjon = true
            }
            val regelmotor =
                arrangeRegler<BeregnAlderspensjon2016ForsteUttakResponse, BeregnAlderspensjon2016ForsteUttakRequest>(
                    response = BeregnAlderspensjon2016ForsteUttakResponse().apply {
                        beregningsResultat = beregningsresultat
                        pakkseddel = Pakkseddel()
                    }
                )

            val result = simulatorContext(regelmotor).beregnAlderspensjon2016FoersteUttak(
                spec = BeregnAlderspensjon2016ForsteUttakRequest(),
                sakId = 123L
            )

            with(result) {
                virkTom shouldBe null
                epsMottarPensjon shouldBe false
                beregningsinformasjon shouldBe null
            }
        }

        should("kaste exception når beregningsResultat er null") {
            val regelmotor =
                arrangeRegler<BeregnAlderspensjon2016ForsteUttakResponse, BeregnAlderspensjon2016ForsteUttakRequest>(
                    response = BeregnAlderspensjon2016ForsteUttakResponse().apply {
                        beregningsResultat = null
                        pakkseddel = Pakkseddel()
                    }
                )

            shouldThrow<RuntimeException> {
                simulatorContext(regelmotor).beregnAlderspensjon2016FoersteUttak(
                    spec = BeregnAlderspensjon2016ForsteUttakRequest(),
                    sakId = 123L
                )
            }.message shouldBe "No beregningsResultat from beregnAlderspensjon2016ForsteUttak"
        }
    }

    context("beregnAlderspensjon2025FoersteUttak") {
        should("returnere beregningsresultat med nullstilte felter") {
            val beregningsresultat = BeregningsResultatAlderspensjon2025().apply {
                virkTom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon()
                epsMottarPensjon = true
            }
            val regelmotor =
                arrangeRegler<BeregnAlderspensjon2025ForsteUttakResponse, BeregnAlderspensjon2025ForsteUttakRequest>(
                    response = BeregnAlderspensjon2025ForsteUttakResponse().apply {
                        beregningsResultat = beregningsresultat
                        pakkseddel = Pakkseddel()
                    }
                )

            val result = simulatorContext(regelmotor).beregnAlderspensjon2025FoersteUttak(
                spec = BeregnAlderspensjon2025ForsteUttakRequest(),
                sakId = 123L
            )

            with(result) {
                virkTom shouldBe null
                epsMottarPensjon shouldBe false
                beregningsinformasjon shouldBe null
            }
        }

        should("kaste exception når beregningsResultat er null") {
            val regelmotor =
                arrangeRegler<BeregnAlderspensjon2025ForsteUttakResponse, BeregnAlderspensjon2025ForsteUttakRequest>(
                    response = BeregnAlderspensjon2025ForsteUttakResponse().apply {
                        beregningsResultat = null
                        pakkseddel = Pakkseddel()
                    }
                )

            shouldThrow<RuntimeException> {
                simulatorContext(regelmotor).beregnAlderspensjon2025FoersteUttak(
                    spec = BeregnAlderspensjon2025ForsteUttakRequest(),
                    sakId = 123L
                )
            }.message shouldBe "No beregningsResultat from beregnAlderspensjon2025ForsteUttak"
        }
    }

    context("revurderAlderspensjon2011") {
        should("returnere revurdert beregningsresultat") {
            val revurdertResultat = BeregningsResultatAlderspensjon2011()
            val regelmotor =
                arrangeRegler<RevurderingAlderspensjon2011Response, RevurderingAlderspensjon2011Request>(
                    response = RevurderingAlderspensjon2011Response().apply {
                        revurdertBeregningsResultat = revurdertResultat
                        pakkseddel = Pakkseddel()
                    }
                )

            simulatorContext(regelmotor).revurderAlderspensjon2011(
                spec = RevurderingAlderspensjon2011Request(),
                sakId = 123L
            ) shouldBe revurdertResultat
        }
    }

    context("revurderAlderspensjon2016") {
        should("returnere revurdert beregningsresultat") {
            val revurdertResultat = BeregningsResultatAlderspensjon2016()
            val regelmotor =
                arrangeRegler<RevurderingAlderspensjon2016Response, RevurderingAlderspensjon2016Request>(
                    response = RevurderingAlderspensjon2016Response().apply {
                        revurdertBeregningsResultat = revurdertResultat
                        pakkseddel = Pakkseddel()
                    }
                )

            simulatorContext(regelmotor).revurderAlderspensjon2016(
                spec = RevurderingAlderspensjon2016Request(),
                sakId = 123L
            ) shouldBe revurdertResultat
        }
    }

    context("revurderAlderspensjon2025") {
        should("returnere revurdert beregningsresultat") {
            val revurdertResultat = BeregningsResultatAlderspensjon2025()
            val regelmotor =
                arrangeRegler<RevurderingAlderspensjon2025Response, RevurderingAlderspensjon2025Request>(
                    response = RevurderingAlderspensjon2025Response().apply {
                        revurdertBeregningsResultat = revurdertResultat
                        pakkseddel = Pakkseddel()
                    }
                )

            simulatorContext(regelmotor).revurderAlderspensjon2025(
                spec = RevurderingAlderspensjon2025Request(),
                sakId = 123L
            ) shouldBe revurdertResultat
        }
    }

    context("simulerPre2025OffentligAfp") {
        should("returnere simuleringsresultat") {
            val resultat = Simuleringsresultat()
            val regelmotor = arrangeRegler<SimuleringResponse, SimuleringRequest>(
                serviceName = "simulerAFP",
                response = simuleringsresultat(resultat)
            )

            simulatorContext(regelmotor).simulerPre2025OffentligAfp(spec = SimuleringRequest()) shouldBe resultat
        }

        should("kaste exception når simuleringsResultat er null") {
            val regelmotor = arrangeRegler<SimuleringResponse, SimuleringRequest>(
                serviceName = "simulerAFP",
                response = simuleringsresultat(null)
            )

            shouldThrow<RuntimeException> {
                simulatorContext(regelmotor).simulerPre2025OffentligAfp(SimuleringRequest())
            }.message shouldBe "Simuleringsresultat is null"
        }
    }

    context("simulerVilkarsprovPre2025OffentligAfp") {
        should("returnere simuleringsresultat") {
            val simuleringsResultat = Simuleringsresultat()
            val regelmotor = arrangeRegler<SimuleringResponse, SimuleringRequest>(
                serviceName = "simulerVilkarsprovAFP",
                response = simuleringsresultat(simuleringsResultat)
            )

            simulatorContext(regelmotor).simulerVilkarsprovPre2025OffentligAfp(
                spec = SimuleringRequest()
            ) shouldBe simuleringsResultat
        }
    }

    context("vilkaarsproevUbetingetAlderspensjon") {
        should("returnere vedtaksliste") {
            val vedtak = VilkarsVedtak()
            val regelmotor = arrangeRegler<VilkarsprovResponse, VilkarsprovRequest>(
                serviceName = "vilkarsprovAlderspensjonOver67",
                response = vilkarsproevingsresultat(vedtak)
            )

            val result = simulatorContext(regelmotor).vilkaarsproevUbetingetAlderspensjon(
                spec = VilkarsprovRequest(),
                sakId = 123L
            )

            result shouldHaveSize 1
            result[0] shouldBe vedtak
        }
    }

    context("vilkaarsproevAlderspensjon2011") {
        should("returnere vedtaksliste") {
            val vedtak = VilkarsVedtak()
            val regelmotor = arrangeRegler<VilkarsprovResponse, VilkarsprovAlderpensjon2011Request>(
                serviceName = "vilkarsprovAlderspensjon2011",
                response = vilkarsproevingsresultat(vedtak)
            )

            simulatorContext(regelmotor).vilkaarsproevAlderspensjon2011(
                spec = VilkarsprovAlderpensjon2011Request(),
                sakId = 123L
            ) shouldHaveSize 1
        }
    }

    context("vilkaarsproevAlderspensjon2016") {
        should("returnere vedtaksliste") {
            val vedtak = VilkarsVedtak()
            val regelmotor = arrangeRegler<VilkarsprovResponse, VilkarsprovAlderpensjon2016Request>(
                serviceName = "vilkarsprovAlderspensjon2016",
                response = vilkarsproevingsresultat(vedtak)
            )

            simulatorContext(regelmotor).vilkaarsproevAlderspensjon2016(
                spec = VilkarsprovAlderpensjon2016Request(),
                sakId = 123L
            ) shouldHaveSize 1
        }
    }

    context("vilkaarsproevAlderspensjon2025") {
        should("returnere vedtaksliste") {
            val vedtak = VilkarsVedtak()
            val regelmotor = arrangeRegler<VilkarsprovResponse, VilkarsprovAlderpensjon2025Request>(
                serviceName = "vilkarsprovAlderspensjon2025",
                response = vilkarsproevingsresultat(vedtak)
            )

            simulatorContext(regelmotor).vilkaarsproevAlderspensjon2025(
                spec = VilkarsprovAlderpensjon2025Request(),
                sakId = 123L
            ) shouldHaveSize 1
        }
    }

    context("beregnPrivatAfp") {
        should("returnere beregningsresultat") {
            val resultat = BeregningsResultatAfpPrivat()
            val regelmotor = arrangeRegler<BeregnAfpPrivatResponse, BeregnAfpPrivatRequest>(
                serviceName = "beregnAfpPrivat",
                response = BeregnAfpPrivatResponse().apply {
                    beregningsResultatAfpPrivat = resultat
                    pakkseddel = Pakkseddel()
                }
            )

            simulatorContext(regelmotor).beregnPrivatAfp(
                spec = BeregnAfpPrivatRequest(),
                sakId = 123L
            ) shouldBe resultat
        }
    }

    context("refreshFastsettTrygdetid") {
        should("returnere TrygdetidResponse") {
            val response = TrygdetidResponse().apply { pakkseddel = Pakkseddel() }
            val regelmotor = arrangeRegler<TrygdetidResponse, TrygdetidRequest>(
                serviceName = "fastsettTrygdetid",
                response
            )

            simulatorContext(regelmotor).refreshFastsettTrygdetid(
                spec = TrygdetidRequest(),
                kravIsUfoeretrygd = false,
                sakId = 123L
            ) shouldBe response
        }
    }

    context("beregnOpptjening") {
        should("returnere pensjonsbeholdninger") {
            val beholdning = Pensjonsbeholdning().apply {
                fom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
                tom = LocalDate.of(2025, 12, 31).toNorwegianDateAtNoon()
            }
            val regelmotor = arrangeRegler<BeregnPensjonsBeholdningResponse, BeregnPensjonsBeholdningRequest>(
                serviceName = "beregnPensjonsBeholdning",
                response = BeregnPensjonsBeholdningResponse().apply {
                    beholdninger = arrayListOf(beholdning)
                    pakkseddel = Pakkseddel()
                }
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

            simulatorContext(regelmotor).beregnOpptjening(
                beholdningTom = LocalDate.of(2025, 12, 31),
                persongrunnlag = persongrunnlag,
                beholdning = null
            ) shouldHaveSize 1
        }
    }

    context("fetchGrunnbeloepListe") {
        should("hente grunnbeløp fra regelmotor") {
            val response = SatsResponse()
            val regelmotor = arrangeRegler<SatsResponse, HentGrunnbelopListeRequest>(
                serviceName = "hentGrunnbelopListe",
                response
            )

            simulatorContext(regelmotor).fetchGrunnbeloepListe(
                dato = LocalDate.of(2024, 5, 1)
            ) shouldBe response
        }

        should("bruke cache ved gjentatte kall") {
            val dato = LocalDate.of(2024, 5, 1)
            val regelmotor = arrangeRegler<SatsResponse, HentGrunnbelopListeRequest>(
                serviceName = "hentGrunnbelopListe",
                response = SatsResponse()
            )

            val context = simulatorContext(regelmotor)

            // Første kall
            context.fetchGrunnbeloepListe(dato)

            // Andre kall med samme dato - skal bruke cache
            context.fetchGrunnbeloepListe(dato)

            // Regelservice skal kun kalles én gang pga. caching
            verify(exactly = 1) {
                regelmotor.makeRegelCall<SatsResponse, HentGrunnbelopListeRequest>(
                    any(), any(), eq("hentGrunnbelopListe"), any(), any()
                )
            }
        }
    }

    context("beregnPoengtallBatch") {
        should("kalle regelmotor og returnere opptjeningsgrunnlag") {
            val initialGrunnlag = Opptjeningsgrunnlag().apply {
                ar = 2021
                pi = 500000
            }
            val personOpptjeningsgrunnlag = PersonOpptjeningsgrunnlag().apply {
                opptjening = Opptjeningsgrunnlag().apply {
                    ar = 2021 // has to match initialGrunnlag (ref. updatePersonOpptjeningsFieldFromReglerResponse)
                    pi = 600000
                }
            }
            val regelmotor = arrangeRegler<BeregnPoengtallBatchResponse, BeregnPoengtallBatchRequest>(
                serviceName = "beregnPoengtallBatch",
                response = BeregnPoengtallBatchResponse().apply {
                    personOpptjeningsgrunnlagListe = mutableListOf(personOpptjeningsgrunnlag)
                }
            )

            val result = simulatorContext(regelmotor).beregnPoengtallBatch(
                opptjeningGrunnlagListe = mutableListOf(initialGrunnlag),
                foedselsdato = LocalDate.of(1963, 5, 15)
            )

            result shouldHaveSize 1
            with(result[0]) {
                ar shouldBe 2021
                pi shouldBe 600000
            }
        }
    }
})

private val objectMapper = JsonMapper()

private inline fun <K, reified T : Any> arrangeRegler(response: K): GenericRegelClient =
    mockk<GenericRegelClient>().apply {
        every {
            makeRegelCall<K, T>(
                any(), any(), any(), any(), any()
            )
        } returns response
    }

private inline fun <K, reified T : Any> arrangeRegler(serviceName: String, response: K): GenericRegelClient =
    mockk<GenericRegelClient>().apply {
        every {
            makeRegelCall<K, T>(
                any(), any(), eq(serviceName), any(), any()
            )
        } returns response
    }

private fun simuleringsresultat(resultat: Simuleringsresultat?) =
    SimuleringResponse().apply {
        simuleringsResultat = resultat
        pakkseddel = Pakkseddel()
    }

private fun vilkarsproevingsresultat(vedtak: VilkarsVedtak) =
    VilkarsprovResponse().apply {
        vedtaksliste = mutableListOf(vedtak)
        pakkseddel = Pakkseddel()
    }

private fun simulatorContext(regelmotor: GenericRegelClient) =
    SimulatorContext(
        regelService = regelmotor,
        objectMapper = objectMapper,
        cacheManager = CaffeineCacheManager()
    )
