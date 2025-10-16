package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerTjenestepensjonFremtidigInntektDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpMapper.ANNEN_TP_ORDNING_BURDE_SIMULERE
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.*
import java.time.LocalDate

class KlpMapperTest : ShouldSpec({

    val idag = LocalDate.now()
    val imorgen = idag.plusDays(1)

    context("mapToResponse") {
        should("map KLP response") {
            val response = KlpSimulerTjenestepensjonResponse(
                inkludertOrdningListe = listOf(InkludertOrdning(tpnr = "1000")),
                utbetalingsListe = listOf(
                    Utbetaling(
                        LocalDate.of(2025, 2, 6),
                        manedligUtbetaling = 1,
                        arligUtbetaling = 12,
                        ytelseType = "BTP"
                    ),
                    Utbetaling(
                        LocalDate.of(2028, 3, 7),
                        manedligUtbetaling = 2,
                        arligUtbetaling = 24,
                        ytelseType = "PAASLAG"
                    ),
                    Utbetaling(
                        LocalDate.of(2030, 4, 8),
                        manedligUtbetaling = 3,
                        arligUtbetaling = 36,
                        ytelseType = "OT6370"
                    ),
                ),
                arsakIngenUtbetaling = listOf(
                    ArsakIngenUtbetaling(
                        statusKode = "IKKE_STOETTET",
                        statusBeskrivelse = "Ikke stoettet",
                        ytelseType = "SAERALDERSPAASLAG"
                    )
                ),
                betingetTjenestepensjonErInkludert = true
            )

            val result = KlpMapper.mapToResponse(response)

            with(result) {
                ordningsListe shouldHaveSize 1
                ordningsListe[0].tpNummer shouldBe "1000"
                aarsakIngenUtbetaling shouldHaveSize 1
                aarsakIngenUtbetaling.first() shouldBe "Ikke stoettet: SAERALDERSPAASLAG"
                erSisteOrdning shouldBe true
                utbetalingsperioder shouldHaveSize 3

                with(utbetalingsperioder[0]) {
                    fom shouldBe response.utbetalingsListe[0].fraOgMedDato
                    maanedligBelop shouldBe response.utbetalingsListe[0].manedligUtbetaling
                    ytelseType shouldBe response.utbetalingsListe[0].ytelseType
                }
                with(utbetalingsperioder[1]) {
                    fom shouldBe response.utbetalingsListe[1].fraOgMedDato
                    maanedligBelop shouldBe response.utbetalingsListe[1].manedligUtbetaling
                    ytelseType shouldBe response.utbetalingsListe[1].ytelseType
                }
                with(utbetalingsperioder[2]) {
                    fom shouldBe response.utbetalingsListe[2].fraOgMedDato
                    maanedligBelop shouldBe response.utbetalingsListe[2].manedligUtbetaling
                    ytelseType shouldBe response.utbetalingsListe[2].ytelseType
                }
            }
        }

        should("map KLP response med ikke siste ordning") {
            val statusBeskrivelse = "Ikke siste ordning. Statens pensjonskasse er siste ordning"
            val ytelseType = "ALLE"
            val response = KlpSimulerTjenestepensjonResponse(
                inkludertOrdningListe = listOf(InkludertOrdning(tpnr = "1000")),
                utbetalingsListe = emptyList(),
                arsakIngenUtbetaling = listOf(
                    ArsakIngenUtbetaling(
                        statusKode = ANNEN_TP_ORDNING_BURDE_SIMULERE,
                        statusBeskrivelse = statusBeskrivelse,
                        ytelseType = ytelseType
                    )
                ),
                betingetTjenestepensjonErInkludert = false
            )

            val result: SimulertTjenestepensjon = KlpMapper.mapToResponse(response)

            with(result) {
                ordningsListe shouldHaveSize 1
                ordningsListe[0].tpNummer shouldBe "1000"
                aarsakIngenUtbetaling shouldHaveSize 1
                aarsakIngenUtbetaling.first() shouldBe "$statusBeskrivelse: $ytelseType"
                erSisteOrdning shouldBe false
                utbetalingsperioder shouldHaveSize 0
            }
        }
    }

    context("mapToRequest") {
        should("map request to KLP request with fremtidige inntekter") {
            val uttaksdato = LocalDate.of(2025, 2, 1)
            val request = SimulerOffentligTjenestepensjonFra2025SpecV1(
                pid = "12345678901",
                sisteInntekt = 100000,
                aarIUtlandetEtter16 = 3,
                epsPensjon = true,
                eps2G = true,
                brukerBaOmAfp = true,
                uttaksdato = uttaksdato,
                foedselsdato = LocalDate.of(1990, 1, 1),
                fremtidigeInntekter = listOf(
                    SimulerTjenestepensjonFremtidigInntektDto(fraOgMed = LocalDate.of(2025, 2, 1), aarligInntekt = 4),
                    SimulerTjenestepensjonFremtidigInntektDto(fraOgMed = LocalDate.of(2026, 3, 1), aarligInntekt = 5),
                    SimulerTjenestepensjonFremtidigInntektDto(fraOgMed = LocalDate.of(2027, 4, 1), aarligInntekt = 6)
                ),
                erApoteker = false
            )

            val result: KlpSimulerTjenestepensjonRequest = KlpMapper.mapToRequest(request)

            val expectedInntekter = request.fremtidigeInntekter!!
            with(result) {
                personId shouldBe request.pid
                uttaksListe shouldHaveSize 1
                with(uttaksListe[0]) {
                    ytelseType shouldBe "ALLE"
                    fraOgMedDato shouldBe uttaksdato
                }
                fremtidigInntektsListe shouldHaveSize 4
                with(fremtidigInntektsListe[0]) {
                    arligInntekt shouldBe request.sisteInntekt
                    fraOgMedDato.isEqual(idag) || fraOgMedDato.isBefore(imorgen) shouldBe true
                }
                with(fremtidigInntektsListe[1]) {
                    arligInntekt shouldBe expectedInntekter[0].aarligInntekt
                    fraOgMedDato shouldBe expectedInntekter[0].fraOgMed
                }
                with(fremtidigInntektsListe[2]) {
                    arligInntekt shouldBe expectedInntekter[1].aarligInntekt
                    fraOgMedDato shouldBe expectedInntekter[1].fraOgMed
                }
                with(fremtidigInntektsListe[3]) {
                    arligInntekt shouldBe expectedInntekter[2].aarligInntekt
                    fraOgMedDato shouldBe expectedInntekter[2].fraOgMed
                }
                arIUtlandetEtter16 shouldBe request.aarIUtlandetEtter16
                epsPensjon shouldBe true
                eps2G shouldBe true
            }
        }

        should("map request to KLP request without fremtidige inntekter") {
            val uttaksdato = LocalDate.of(2025, 2, 1)
            val request = SimulerOffentligTjenestepensjonFra2025SpecV1(
                pid = "12345678901",
                sisteInntekt = 100000,
                aarIUtlandetEtter16 = 3,
                epsPensjon = true,
                eps2G = true,
                brukerBaOmAfp = true,
                uttaksdato = uttaksdato,
                foedselsdato = LocalDate.of(1990, 1, 1),
                fremtidigeInntekter = null,
                erApoteker = false
            )

            val result: KlpSimulerTjenestepensjonRequest = KlpMapper.mapToRequest(request)

            with(result) {
                personId shouldBe request.pid
                uttaksListe shouldHaveSize 1
                with(uttaksListe[0]) {
                    ytelseType shouldBe "ALLE"
                    fraOgMedDato shouldBe uttaksdato
                }
                fremtidigInntektsListe shouldHaveSize 1
                with(fremtidigInntektsListe[0]) {
                    arligInntekt shouldBe request.sisteInntekt
                    fraOgMedDato.isEqual(idag) || fraOgMedDato.isBefore(imorgen) shouldBe true
                }
                arIUtlandetEtter16 shouldBe request.aarIUtlandetEtter16
                epsPensjon shouldBe true
                eps2G shouldBe true
            }
        }
    }
})