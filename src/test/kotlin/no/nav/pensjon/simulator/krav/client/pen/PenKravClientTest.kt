package no.nav.pensjon.simulator.krav.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.mockito.Mockito.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

class PenKravClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    beforeSpec {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        SecurityContextHolder.getContext().authentication = EnrichedAuthentication(
            TestingAuthenticationToken(
                "TEST_USER",
                Jwt("j.w.t", null, null, mapOf("k" to "v"), mapOf("k" to "v"))
            ),
            EgressTokenSuppliersByService(mapOf())
        )

        server = MockWebServer().also { it.start() }
        baseUrl = "http://localhost:${server!!.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    test("fetchKravhode deserializes response") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server!!.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenKravhodeResponse.KRAVHODE)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenKravClient(
                baseUrl!!, retryAttempts = "0", webClientBuilder, mock(TraceAid::class.java)
            )

            val result: Kravhode = client.fetchKravhode(123L)

            result.persongrunnlagListe.size shouldBe 1
            with(result.persongrunnlagListe[0]) {
                personDetaljListe.size shouldBe 1
                personDetaljListe[0].bruk shouldBe true
                fodselsdato shouldBe dateAtNoon(year = 1974, month = Calendar.MARCH, day = 30)
            }
            result.kravlinjeListe.size shouldBe 1
            result.kravlinjeListe[0].kravlinjeType?.kode shouldBe "UT"
        }
    }
})

object PenKravhodeResponse {

    @Language("json")
    const val KRAVHODE = """{
    "persongrunnlagListe": [
        {
            "penPerson": {
                "penPersonId": 1234
            },
            "fodselsdato": "1974-03-30T12:00:00+0100",
            "dodsdato": null,
            "statsborgerskap": {
                "kode": "NOR",
                "dekode": null,
                "dato_fom": null,
                "dato_tom": null,
                "er_gyldig": true,
                "kommentar": null
            },
            "flyktning": false,
            "personDetaljListe": [
                {
                    "grunnlagsrolle": {
                        "kode": "SOKER",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "rolleFomDato": "1974-04-01T12:00:00+0100",
                    "rolleTomDato": null,
                    "sivilstandType": {
                        "kode": "UGIF",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "sivilstandRelatertPerson": null,
                    "borMed": null,
                    "barnDetalj": null,
                    "tillegg": false,
                    "bruk": true,
                    "grunnlagKilde": {
                        "kode": "TPS",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "serskiltSatsUtenET": null,
                    "epsAvkallEgenPensjon": null,
                    "eps": false
                }
            ],
            "sistMedlITrygden": null,
            "sisteGyldigeOpptjeningsAr": 2016,
            "hentetPopp": true,
            "hentetInnt": false,
            "hentetInst": false,
            "hentetTT": true,
            "hentetArbeid": false,
            "overkompUtl": null,
            "opptjeningsgrunnlagListe": [
                {
                    "ar": 2010,
                    "pi": 653500,
                    "pia": 516717,
                    "pp": 5.92,
                    "opptjeningType": {
                        "kode": "PPI",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "maksUforegrad": 0,
                    "bruk": true,
                    "grunnlagKilde": {
                        "kode": "POPP",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "opptjeningTypeListe": [
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "INN_LON",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        },
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "AI",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        }
                    ]
                },
                {
                    "ar": 2011,
                    "pi": 665000,
                    "pia": 533763,
                    "pp": 5.84,
                    "opptjeningType": {
                        "kode": "PPI",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "maksUforegrad": 0,
                    "bruk": true,
                    "grunnlagKilde": {
                        "kode": "POPP",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "opptjeningTypeListe": [
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "INN_LON",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        },
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "AI",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        }
                    ]
                },
                {
                    "ar": 2012,
                    "pi": 649100,
                    "pia": 540979,
                    "pp": 5.67,
                    "opptjeningType": {
                        "kode": "PPI",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "maksUforegrad": 0,
                    "bruk": true,
                    "grunnlagKilde": {
                        "kode": "POPP",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "opptjeningTypeListe": [
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "INN_LON",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        },
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "AI",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        }
                    ]
                },
                {
                    "ar": 2013,
                    "pi": 627300,
                    "pia": 545916,
                    "pp": 5.48,
                    "opptjeningType": {
                        "kode": "PPI",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "maksUforegrad": 0,
                    "bruk": true,
                    "grunnlagKilde": {
                        "kode": "POPP",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "opptjeningTypeListe": [
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "INN_LON",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        },
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "AI",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        }
                    ]
                },
                {
                    "ar": 2014,
                    "pi": 631200,
                    "pia": 559712,
                    "pp": 5.41,
                    "opptjeningType": {
                        "kode": "PPI",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "maksUforegrad": 0,
                    "bruk": true,
                    "grunnlagKilde": {
                        "kode": "POPP",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "opptjeningTypeListe": [
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "INN_LON",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        },
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "AI",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        }
                    ]
                },
                {
                    "ar": 2015,
                    "pi": 589300,
                    "pia": 554441,
                    "pp": 5.19,
                    "opptjeningType": {
                        "kode": "PPI",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "maksUforegrad": 0,
                    "bruk": true,
                    "grunnlagKilde": {
                        "kode": "POPP",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "opptjeningTypeListe": [
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "INN_LON",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        },
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "AI",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        }
                    ]
                },
                {
                    "ar": 2016,
                    "pi": 556600,
                    "pia": 552493,
                    "pp": 5.02,
                    "opptjeningType": {
                        "kode": "PPI",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "maksUforegrad": 0,
                    "bruk": true,
                    "grunnlagKilde": {
                        "kode": "POPP",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "opptjeningTypeListe": [
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "INN_LON",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        },
                        {
                            "opptjeningPOPPTypeCti": {
                                "kode": "AI",
                                "dekode": null,
                                "dato_fom": null,
                                "dato_tom": null,
                                "er_gyldig": true,
                                "kommentar": null
                            }
                        }
                    ]
                }
            ],
            "inntektsgrunnlagListe": [],
            "trygdetidPerioder": [
                {
                    "fom": "2010-01-01T12:00:00+0100",
                    "tom": "2016-12-31T12:00:00+0100",
                    "changed": null,
                    "poengIInnAr": false,
                    "poengIUtAr": false,
                    "land": {
                        "kode": "NOR",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "ikkeProRata": false,
                    "bruk": true,
                    "grunnlagKilde": {
                        "kode": "SAKSB",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    }
                }
            ],
            "trygdetidPerioderKapittel20": [],
            "trygdetid": null,
            "trygdetidKapittel20": null,
            "trygdetidAlternativ": null,
            "uforegrunnlag": null,
            "uforeHistorikk": null,
            "yrkesskadegrunnlag": null,
            "dodAvYrkesskade": false,
            "generellHistorikk": {
                "generellHistorikkId": 52634486,
                "fravik_19_3": null,
                "fpp_eos": 0.0,
                "ventetilleggsgrunnlag": null,
                "poengtillegg": null,
                "eosEkstra": null,
                "garantiTrygdetid": null,
                "sertillegg1943kull": null,
                "giftFor2011": false
            },
            "afpHistorikkListe": [],
            "barnekull": null,
            "barnetilleggVurderingsperiode": null,
            "antallArUtland": 0,
            "medlemIFolketrygdenSiste3Ar": null,
            "over60ArKanIkkeForsorgesSelv": null,
            "utenlandsoppholdListe": [
                {
                    "fom": "1998-01-01T12:00:00+0100",
                    "tom": "2002-12-31T12:00:00+0100",
                    "land": {
                        "kode": "SWE",
                        "dekode": null,
                        "dato_fom": null,
                        "dato_tom": null,
                        "er_gyldig": true,
                        "kommentar": null
                    },
                    "pensjonsordning": null,
                    "bodd": true,
                    "arbeidet": true
                }
            ],
            "trygdeavtale": null,
            "trygdeavtaledetaljer": null,
            "inngangOgEksportGrunnlag": null,
            "forsteVirkningsdatoGrunnlagListe": [],
            "arligPGIMinst1G": null,
            "artikkel10": null,
            "vernepliktAr": [],
            "skiltesDelAvAvdodesTP": -99,
            "instOpphReduksjonsperiodeListe": [],
            "instOpphFasteUtgifterperiodeListe": [],
            "bosattLand": null,
            "pensjonsbeholdning": {
                "type": "Pensjonsbeholdning",
                "ar": 2018,
                "totalbelop": 844321.0148069302,
                "opptjening": {
                    "ar": 2016,
                    "opptjeningsgrunnlag": 556600.0,
                    "anvendtOpptjeningsgrunnlag": 556600.0,
                    "arligOpptjening": 102820.11680059433,
                    "lonnsvekstInformasjon": null,
                    "poengtall": {
                        "type": "Poengtall",
                        "pp": 0.0,
                        "pia": 0,
                        "pi": 0,
                        "ar": 0,
                        "bruktIBeregning": false,
                        "gv": 0,
                        "poengtallType": null,
                        "maksUforegrad": 0,
                        "uforear": false,
                        "merknadListe": [],
                        "omsorg": false,
                        "opptjeningsar": 0,
                        "inntektIAvtaleland": false,
                        "justertBelop": 0.0,
                        "verdi": 0.0
                    },
                    "inntektUtenDagpenger": 556600.0,
                    "uforeOpptjening": null,
                    "dagpenger": 0.0,
                    "dagpengerFiskerOgFangstmenn": 0.0,
                    "omsorg": 0.0,
                    "forstegangstjeneste": 0.0,
                    "arligOpptjeningOmsorg": 0.0,
                    "arligOpptjeningUtenOmsorg": 0.0,
                    "psatsOpptjening": 0.0
                },
                "lonnsvekstInformasjon": {
                    "lonnsvekst": 0.0,
                    "reguleringsDato": "2018-05-01T12:00:00+0200",
                    "uttaksgradVedRegulering": 0
                },
                "reguleringsInformasjon": {
                    "lonnsvekst": 0.0,
                    "fratrekksfaktor": 0.0,
                    "gammelG": 0,
                    "nyG": 0,
                    "reguleringsfaktor": 0.0,
                    "gjennomsnittligUttaksgradSisteAr": 0.0,
                    "reguleringsbelop": 28315.395007055602,
                    "prisOgLonnsvekst": 0.0
                },
                "formelkode": null,
                "beholdningsType": {
                    "kode": "PEN_B",
                    "dekode": null,
                    "dato_fom": null,
                    "dato_tom": null,
                    "er_gyldig": true,
                    "kommentar": null
                },
                "merknadListe": []
            },
            "forstegangstjenestegrunnlag": null,
            "dagpengegrunnlagListe": [],
            "omsorgsgrunnlagListe": [],
            "arbeidsforholdsgrunnlagListe": [],
            "arbeidsforholdEtterUforgrunnlagListe": [],
            "overgangsInfoUPtilUT": null,
            "utbetalingsgradUTListe": [],
            "sortedTrygdetidPerioderKapittel20": [],
            "vernepliktArAsList": [],
            "barnOrFosterbarn": false,
            "afpTpoUpGrunnlag": null,
            "soker": true,
            "avdod": false,
            "p67": false,
            "eps": false
        }
    ],
    "kravlinjeListe": [
        {
            "kravlinjeType": {
                "kode": "UT",
                "dekode": null,
                "dato_fom": null,
                "dato_tom": null,
                "er_gyldig": true,
                "kommentar": null,
                "hovedKravlinje": true
            },
            "relatertPerson": {
                "penPersonId": 2345
            },
            "kravlinjeAvbrutt": false
        }
    ],
    "afpOrdning": null,
    "afptillegg": false,
    "brukOpptjeningFra65I66Aret": false,
    "kravVelgType": {
        "kode": "VARIG",
        "dekode": null,
        "dato_fom": null,
        "dato_tom": null,
        "er_gyldig": true,
        "kommentar": null,
        "hovedKravlinje": false
    },
    "boddEllerArbeidetIUtlandet": true,
    "boddArbeidUtlandFar": false,
    "boddArbeidUtlandMor": false,
    "boddArbeidUtlandAvdod": false,
    "uttaksgradListe": [],
    "regelverkTypeCti": null,
    "sisteSakstypeForAP": null,
    "overstyrendeP_satsGP": 0.0,
    "btVurderingsperiodeBenyttet": true,
    "uforetrygd": true
}"""
}
