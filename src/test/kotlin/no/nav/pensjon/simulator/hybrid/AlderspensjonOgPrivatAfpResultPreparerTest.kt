package no.nav.pensjon.simulator.hybrid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonOgPrivatAfpResultPreparerTest : ShouldSpec({

    val today = LocalDate.of(2025, 1, 1)

    should("map empty result to empty lists") {
        AlderspensjonOgPrivatAfpResultPreparer(
            personService = mockk(relaxed = true),
            time = mockk(relaxed = true)
        ).result(
            simulatorOutput = SimulatorOutput(),
            pid,
            harLoependePrivatAfp = false
        ) shouldBe AlderspensjonOgPrivatAfpResult(
            suksess = true,
            alderspensjonsperiodeListe = emptyList(),
            privatAfpPeriodeListe = emptyList(),
            harNaavaerendeUttak = false,
            harTidligereUttak = false,
            harLoependePrivatAfp = false
        )
    }

    should("give 'har uttak' = true if fomDato <= today and uttaksgrad > 0") {
        val result = AlderspensjonOgPrivatAfpResultPreparer(
            personService = mockk(relaxed = true),
            time = mockk<Time>().apply { every { today() } returns today }
        ).result(
            simulatorOutput = SimulatorOutput().apply {
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
            harLoependePrivatAfp = true
        )

        with(result) {
            harNaavaerendeUttak shouldBe true
            harTidligereUttak shouldBe false
            harLoependePrivatAfp shouldBe true
        }
    }

    should("give 'har tidligere uttak' = true if tomDato < today and uttaksgrad > 0") {
        val result = AlderspensjonOgPrivatAfpResultPreparer(
            personService = mockk(relaxed = true),
            time = mockk<Time>().apply { every { today() } returns today }
        ).result(
            simulatorOutput = SimulatorOutput().apply {
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
            harLoependePrivatAfp = false
        )

        with(result) {
            harNaavaerendeUttak shouldBe false
            harTidligereUttak shouldBe true
        }
    }

    should("map privatAfpPeriodeListe to privatAfpPerioder") {
        val result = AlderspensjonOgPrivatAfpResultPreparer(
            personService = mockk(relaxed = true),
            time = mockk(relaxed = true)
        ).result(
            simulatorOutput = SimulatorOutput().apply {
                privatAfpPeriodeListe.add(PrivatAfpPeriode(alderAar = 64, aarligBeloep = 1))
                privatAfpPeriodeListe.add(PrivatAfpPeriode(alderAar = 65, aarligBeloep = 2))
            },
            pid,
            harLoependePrivatAfp = false
        )

        with(result) {
            privatAfpPeriodeListe shouldHaveSize 2
            with(privatAfpPeriodeListe[0]) {
                alderAar shouldBe 64
                beloep shouldBe 1
            }
            with(privatAfpPeriodeListe[1]) {
                alderAar shouldBe 65
                beloep shouldBe 2
            }
        }
    }
})

