package no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3

import io.kotest.core.spec.style.FunSpec
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

class AlderspensjonOgPrivatAfpResultMapperV3Test : FunSpec({

    val today = LocalDate.of(2025, 1, 1)

    test("empty result maps to empty lists") {
        AlderspensjonOgPrivatAfpResultMapperV3(
            personService = mockk(relaxed = true),
            time = mockk(relaxed = true)
        ).toDto(
            simuleringResult = SimulatorOutput(),
            pid
        ) shouldBe AlderspensjonOgPrivatAfpResultV3(
            alderspensjonsperioder = emptyList(),
            privatAfpPerioder = emptyList(),
            harNaavaerendeUttak = false,
            harTidligereUttak = false
        )
    }

    test("harUttak should be true if fomDato <= today and uttaksgrad > 0") {
        val result = AlderspensjonOgPrivatAfpResultMapperV3(
            personService = mockk(relaxed = true),
            time = mockk<Time>().apply { every { today() } returns today }
        ).toDto(
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
            pid
        )

        with(result) {
            harNaavaerendeUttak shouldBe true
            harTidligereUttak shouldBe false
        }
    }

    test("harTidligereUttak should be true if tomDato < today and uttaksgrad > 0") {
        val result = AlderspensjonOgPrivatAfpResultMapperV3(
            personService = mockk(relaxed = true),
            time = mockk<Time>().apply { every { today() } returns today }
        ).toDto(
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
            pid
        )

        with(result) {
            harNaavaerendeUttak shouldBe false
            harTidligereUttak shouldBe true
        }
    }

    test("privatAfpPeriodeListe maps to privatAfpPerioder") {
        val result = AlderspensjonOgPrivatAfpResultMapperV3(
            personService = mockk(relaxed = true),
            time = mockk(relaxed = true)
        ).toDto(
            simuleringResult = SimulatorOutput().apply {
                privatAfpPeriodeListe.add(PrivatAfpPeriode(alderAar = 64, aarligBeloep = 1))
                privatAfpPeriodeListe.add(PrivatAfpPeriode(alderAar = 65, aarligBeloep = 2))
            },
            pid
        )

        with(result.privatAfpPerioder!!) {
            size shouldBe 2
            with(this[0]) {
                alder shouldBe 64
                beloep shouldBe 1
            }
            with(this[1]) {
                alder shouldBe 65
                beloep shouldBe 2
            }
        }
    }
})
