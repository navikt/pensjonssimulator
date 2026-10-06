package no.nav.pensjon.simulator.inntekt

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.opptjening.client.OpptjeningClient
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class InntektServiceTest : ShouldSpec({

    val service = InntektService(
        opptjeningClient = arrangeInntekt(
            LoependeInntekt(
                aarligBeloep = 321000,
                fom = LocalDate.of(2025, 1, 1)
            )
        ),
        grunnbeloepService = arrangeGrunnbeloep,
        time = { idag }
    )

    context("hentSisteLignetInntekt") {
        context("normal respons") {
            should("returnere inntekten") {
                val inntekt = LoependeInntekt(
                    aarligBeloep = 321000,
                    fom = LocalDate.of(2025, 1, 1)
                )

                InntektService(
                    opptjeningClient = arrangeInntekt(inntekt),
                    grunnbeloepService = arrangeGrunnbeloep,
                    time = { idag }
                ).hentSisteLignetInntekt(pid) shouldBe inntekt
            }
        }

        context("udefinert respons") {
            should("returnere 0 inntekt med f.o.m.-dato lik 1. januar inneværende år") {
                InntektService(
                    opptjeningClient = arrangeInntekt(null),
                    grunnbeloepService = arrangeGrunnbeloep,
                    time = { idag }
                ).hentSisteLignetInntekt(pid) shouldBe LoependeInntekt(
                    aarligBeloep = 0,
                    fom = LocalDate.of(idag.year, 1, 1)
                )
            }
        }
    }

    context("hentSisteMaanedsInntektOver1G") {
        context("har inntekt siste måned over grunnbeløpet") {
            should("returnere beløp høyere enn grunnbeløpet") {
                service.hentSisteMaanedsInntektOver1G(
                    harInntektSisteMaanedOver1G = true
                ) shouldBeGreaterThan (GRUNNBELOEP / MAANEDER_PER_AAR).toInt()
            }
        }

        context("har ikke inntekt siste måned over grunnbeløpet") {
            should("returnere beløp mellom 0 og grunnbeløpet") {
                val result = service.hentSisteMaanedsInntektOver1G(harInntektSisteMaanedOver1G = false)

                result shouldBeLessThan (GRUNNBELOEP / MAANEDER_PER_AAR).toInt()
                result shouldBeGreaterThan 0
            }
        }
    }
})

private const val GRUNNBELOEP = 123000.0

private val idag = LocalDate.of(2024, 6, 15)

private val arrangeGrunnbeloep: GrunnbeloepService =
    mockk { every { naavaerendeGrunnbeloep() } returns GRUNNBELOEP.toInt() }

private fun arrangeInntekt(inntekt: LoependeInntekt?): OpptjeningClient =
    mockk { every { fetchSistLignedeInntekt(pid) } returns inntekt }