package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonResultMapperV3Test : ShouldSpec({

    val today = LocalDate.of(2025, 1, 1)

    /**
     * Tester at
     * - sisteGyldigeOpptjeningAar mappes
     * - hvis alderspensjon mangler, inkluderes et 'default' tomt alderspensjonFraFolketrygden-objekt
     * - øvrige manglende lister mappes til tomme lister
     */
    should("map sisteGyldigeOpptjeningAar og default alderspensjonFraFolketrygden") {
        AlderspensjonResultMapperV3(
            personService = mockk(relaxed = true),
            normertPensjonsalderService = mockk(relaxed = true),
            time = mockk(relaxed = true)
        ).map(
            simuleringResult = SimulatorOutput().apply { sisteGyldigeOpptjeningAar = 2023 },
            pid,
            foersteUttakFom = LocalDate.of(2037, 3, 1),
            heltUttakFom = null
        ) shouldBe AlderspensjonResultV3(
            pensjonsperioder = emptyList(),
            simuleringsdataListe = emptyList(),
            pensjonsbeholdningsperioder = emptyList(),
            alderspensjonFraFolketrygden = listOf(
                AlderspensjonFraFolketrygdenResultV3(
                    datoFom = null,
                    delytelser = emptyList(),
                    uttaksgrad = null
                )
            ),
            harUttak = false,
            harTidligereUttak = false,
            afpPrivatBeholdningVedUttak = null,
            sisteGyldigeOpptjeningsAr = 2023
        )
    }

    /**
     * Test for mapping av 'simulert beregninginformasjon' til 'simuleringsdata'.
     * Tester at
     * - når foersteUttakFom < normertPensjoneringsdato, så brukes normertPensjoneringsdato som foersteSimuleringsdataDato
     * - når SimulertBeregningInformasjon.datoFom >= foersteSimuleringsdataDato, så mappes elementet
     */
    should("map simuleringsdataListe for ikke-gradert uttak - normertPensjoneringsdato") {
        val normertPensjoneringsdato = LocalDate.of(2037, 3, 1)

        AlderspensjonResultMapperV3(
            personService = Arrange.foedselsdato(1970, 1, 15),
            normertPensjonsalderService = arrangeNormertPensjoneringsdato(normertPensjoneringsdato),
            time = mockk(relaxed = true)
        ).map(
            simuleringResult = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    simulertBeregningInformasjonListe = listOf(
                        SimulertBeregningInformasjon().apply {
                            pa_f92 = 1
                            pa_e91 = 2
                            spt = 3.1
                            tt_anv_kap19 = 4
                            basisGrunnpensjon = 5.2
                            basisTilleggspensjon = 6.3
                            basisPensjonstillegg = 7.4
                            forholdstall = 8.5
                            delingstall = 9.6
                            ufoereGrad = 10
                            datoFom = normertPensjoneringsdato
                        }
                    )
                }
            },
            pid,
            foersteUttakFom = normertPensjoneringsdato.minusYears(2),
            heltUttakFom = null
        ).simuleringsdataListe shouldBe listOf(
            SimuleringsdataResultV3(
                poengArTom1991 = 1,
                poengArFom1992 = 2,
                sluttpoengtall = 3.1,
                anvendtTrygdetid = 4,
                basisgp = 5.2,
                basistp = 6.3,
                basispt = 7.4,
                forholdstallUttak = 8.5,
                delingstallUttak = 9.6,
                uforegradVedOmregning = 10,
                datoFom = "2037-03-01",
            )
        )
    }

    /**
     * Test for mapping av 'simulert beregninginformasjon' til 'simuleringsdata'.
     * Tester at
     * - når foersteUttakFom >= normertPensjoneringsdato, så brukes foersteUttakFom som foersteSimuleringsdataDato
     * - når SimulertBeregningInformasjon.datoFom >= foersteSimuleringsdataDato, så mappes elementet
     */
    should("map simuleringsdataListe for ikke-gradert uttak - foersteUttakFom") {
        val normertPensjoneringsdato = LocalDate.of(2037, 2, 1)
        val foersteUttakFom = normertPensjoneringsdato.plusYears(1) // > normertPensjoneringsdato

        val result = AlderspensjonResultMapperV3(
            personService = Arrange.foedselsdato(1970, 1, 15),
            normertPensjonsalderService = arrangeNormertPensjoneringsdato(normertPensjoneringsdato),
            time = mockk(relaxed = true)
        ).map(
            simuleringResult = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    simulertBeregningInformasjonListe = listOf(
                        // Skal ikke mappes, siden datoFom < foersteUttakFom:
                        SimulertBeregningInformasjon().apply {
                            ufoereGrad = 1
                            datoFom = foersteUttakFom.minusYears(1)
                        },
                        // Skal mappes, siden datoFom >= foersteUttakFom:
                        SimulertBeregningInformasjon().apply {
                            ufoereGrad = 2
                            datoFom = foersteUttakFom
                        }
                    )
                }
            },
            pid,
            foersteUttakFom,
            heltUttakFom = null
        ).simuleringsdataListe

        with(result!!) {
            size shouldBe 1
            get(0).uforegradVedOmregning shouldBe 2
        }
    }

    should("make 'har uttak' true if fomDato <= today and uttaksgrad > 0") {
        val result = AlderspensjonResultMapperV3(
            personService = mockk(relaxed = true),
            normertPensjonsalderService = mockk(relaxed = true),
            time = mockk<Time>().apply { every { today() } returns today }
        ).map(
            simuleringResult = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    uttakGradListe = listOf(
                        Uttaksgrad().apply {
                            fomDato = today.toNorwegianDateAtNoon()
                            tomDato = null
                            uttaksgrad = 50
                        })
                }
            },
            pid,
            foersteUttakFom = LocalDate.of(2038, 1, 1),
            heltUttakFom = null
        )

        with(result) {
            harUttak shouldBe true
            harTidligereUttak shouldBe false
        }
    }

    should("make 'har tidligere uttak' true if tomDato < today and uttaksgrad > 0") {
        val result = AlderspensjonResultMapperV3(
            personService = mockk(relaxed = true),
            normertPensjonsalderService = mockk(relaxed = true),
            time = mockk<Time>().apply { every { today() } returns today }
        ).map(
            simuleringResult = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    uttakGradListe = listOf(
                        // Skal ikke medføre 'har uttak', siden uttaksgrad = 0:
                        Uttaksgrad().apply {
                            fomDato = today.toNorwegianDateAtNoon()
                            tomDato = null
                            uttaksgrad = 0
                        },
                        // Skal medføre 'har tidligere uttak', siden både fomDato og tomDato er før dagens dato:
                        Uttaksgrad().apply {
                            fomDato = today.minusYears(1).toNorwegianDateAtNoon() // fomDato < dagens dato
                            tomDato = today.minusDays(1).toNorwegianDateAtNoon() // tomDato < dagens dato
                            uttaksgrad = 20
                        })
                }
            },
            pid,
            foersteUttakFom = LocalDate.of(2038, 1, 1),
            heltUttakFom = null
        )

        with(result) {
            harUttak shouldBe false
            harTidligereUttak shouldBe true
        }
    }

    should("give 1 element in 'alderspensjon fra folketrygden' when ikke gradert") {
        val result = AlderspensjonResultMapperV3(
            personService = mockk(relaxed = true),
            normertPensjonsalderService = mockk(relaxed = true),
            time = mockk(relaxed = true)
        ).map(
            simuleringResult = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    simulertBeregningInformasjonListe = listOf(
                        SimulertBeregningInformasjon().apply {
                            grunnpensjon = 1
                            tilleggspensjon = 2
                            pensjonstillegg = 3
                            individueltMinstenivaaTillegg = 4
                            inntektspensjon = 5
                            garantipensjon = 6
                            garantitillegg = 7
                            skjermingstillegg = 8
                            uttakGrad = 100.0
                            datoFom = LocalDate.of(2037, 2, 1)
                        }
                    )
                }
            },
            pid,
            foersteUttakFom = LocalDate.of(2037, 2, 1),
            heltUttakFom = null // ikke-gradert
        ).alderspensjonFraFolketrygden

        result!! shouldHaveSize 1
        with(result[0]) {
            datoFom shouldBe "2037-02-01"
            delytelser!! shouldHaveSize 8
            assertDelytelse(actual = delytelser[0], pensjonstype = "GP", beloep = 1.0)
            assertDelytelse(actual = delytelser[1], pensjonstype = "TP", beloep = 2.0)
            assertDelytelse(actual = delytelser[2], pensjonstype = "PT", beloep = 3.0)
            assertDelytelse(actual = delytelser[3], pensjonstype = "MIN_NIVA_TILL_INDV", beloep = 4.0)
            assertDelytelse(actual = delytelser[4], pensjonstype = "IP", beloep = 5.0)
            assertDelytelse(actual = delytelser[5], pensjonstype = "GAP", beloep = 6.0)
            assertDelytelse(actual = delytelser[6], pensjonstype = "GAT", beloep = 7.0)
            assertDelytelse(actual = delytelser[7], pensjonstype = "SKJERMT", beloep = 8.0)
            uttaksgrad shouldBe 100
        }
    }

    should("give 2 elements in 'alderspensjon fra folketrygden' when gradert") {
        val foersteUttakFom = LocalDate.of(2036, 2, 1)
        val heltUttakFom = LocalDate.of(2037, 2, 1)

        val result = AlderspensjonResultMapperV3(
            personService = mockk(relaxed = true),
            normertPensjonsalderService = mockk(relaxed = true),
            time = mockk(relaxed = true)
        ).map(
            simuleringResult = SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    simulertBeregningInformasjonListe = listOf(
                        // Skal ikke mappes, siden datoFom ikke matcher foersteUttakFom/heltUttakFom:
                        SimulertBeregningInformasjon().apply {
                            grunnpensjon = 1
                            uttakGrad = 20.0
                            datoFom = LocalDate.of(2035, 2, 1)
                        },
                        // Skal mappes, siden datoFom matcher foersteUttakFom:
                        SimulertBeregningInformasjon().apply {
                            grunnpensjon = 2
                            uttakGrad = 50.0
                            datoFom = foersteUttakFom
                        },
                        // Skal mappes, siden datoFom matcher heltUttakFom:
                        SimulertBeregningInformasjon().apply {
                            grunnpensjon = 3
                            uttakGrad = 100.0
                            datoFom = heltUttakFom
                        }
                    )
                }
            },
            pid,
            foersteUttakFom,
            heltUttakFom
        ).alderspensjonFraFolketrygden

        result!! shouldHaveSize 2
        with(result[0]) {
            datoFom shouldBe "2036-02-01"
            delytelser!! shouldHaveSize 1
            assertDelytelse(actual = delytelser[0], pensjonstype = "GP", beloep = 2.0)
            uttaksgrad shouldBe 50
        }
        with(result[1]) {
            datoFom shouldBe "2037-02-01"
            delytelser!! shouldHaveSize 1
            assertDelytelse(actual = delytelser[0], pensjonstype = "GP", beloep = 3.0)
            uttaksgrad shouldBe 100
        }
    }

    should("use first element in list for 'privat AFP-beholdning ved uttak'") {
        AlderspensjonResultMapperV3(
            personService = mockk(relaxed = true),
            normertPensjonsalderService = mockk(relaxed = true),
            time = mockk(relaxed = true)
        ).map(
            simuleringResult = SimulatorOutput().apply {
                // Første element skal brukes i mappingen:
                privatAfpPeriodeListe.add(PrivatAfpPeriode(afpOpptjening = 2))
                // Øvrige elementer skal ikke brukes i mappingen:
                privatAfpPeriodeListe.add(PrivatAfpPeriode(afpOpptjening = 3))
                privatAfpPeriodeListe.add(PrivatAfpPeriode(afpOpptjening = 1))
            },
            pid,
            foersteUttakFom = LocalDate.of(2036, 2, 1),
            heltUttakFom = null
        ).afpPrivatBeholdningVedUttak shouldBe 2
    }
})

private fun arrangeNormertPensjoneringsdato(dato: LocalDate): NormertPensjonsalderService =
    mockk<NormertPensjonsalderService>().apply {
        every { normertPensjoneringsdato(any()) } returns dato
    }

private fun assertDelytelse(actual: DelytelseResultV3, pensjonstype: String, beloep: Double) {
    actual.pensjonstype shouldBe pensjonstype
    actual.belop shouldBe beloep
}
