package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.*
import java.time.LocalDate

class KlpMapperTest : ShouldSpec({

    val idag = LocalDate.now()
    val imorgen = idag.plusDays(1)

    context("fromResponseDto") {
        should("map KLP response DTO to 'simulert tjenestepensjon' domain object") {
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

            val result: SimulertTjenestepensjon = KlpMapper.fromResponseDto(response)

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

        should("map KLP response DTO med ikke siste ordning") {
            val statusBeskrivelse = "Ikke siste ordning. Statens pensjonskasse er siste ordning"
            val ytelseType = "ALLE"
            val response = KlpSimulerTjenestepensjonResponse(
                inkludertOrdningListe = listOf(InkludertOrdning(tpnr = "1000")),
                utbetalingsListe = emptyList(),
                arsakIngenUtbetaling = listOf(
                    ArsakIngenUtbetaling(
                        statusKode = "IKKE_SISTE_ORDNING",
                        statusBeskrivelse = statusBeskrivelse,
                        ytelseType = ytelseType
                    )
                ),
                betingetTjenestepensjonErInkludert = false
            )

            val result: SimulertTjenestepensjon = KlpMapper.fromResponseDto(response)

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

    context("toRequestDto") {
        should("map spec to KLP request DTO with fremtidige inntekter") {
            val uttaksdato = LocalDate.of(2025, 2, 1)
            val request = OffentligTjenestepensjonFra2025SimuleringSpec(
                pid = Pid("12345678901"),
                sisteInntekt = 100000,
                utlandAntallAar = 3,
                epsHarPensjon = true,
                epsHarInntektOver2G = true,
                afpErForespurt = true,
                uttaksdato = uttaksdato,
                foedselsdato = LocalDate.of(1990, 1, 1),
                fremtidigeInntekter = listOf(
                    TjenestepensjonInntektSpec(fom = LocalDate.of(2025, 2, 1), aarligInntekt = 4),
                    TjenestepensjonInntektSpec(fom = LocalDate.of(2026, 3, 1), aarligInntekt = 5),
                    TjenestepensjonInntektSpec(fom = LocalDate.of(2027, 4, 1), aarligInntekt = 6)
                ),
                gjelderApoteker = false
            )

            val result: KlpSimulerTjenestepensjonRequest = KlpMapper.toRequestDto(request)

            val expectedInntekter = request.fremtidigeInntekter!!
            with(result) {
                personId shouldBe request.pid.value
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
                    fraOgMedDato shouldBe expectedInntekter[0].fom
                }
                with(fremtidigInntektsListe[2]) {
                    arligInntekt shouldBe expectedInntekter[1].aarligInntekt
                    fraOgMedDato shouldBe expectedInntekter[1].fom
                }
                with(fremtidigInntektsListe[3]) {
                    arligInntekt shouldBe expectedInntekter[2].aarligInntekt
                    fraOgMedDato shouldBe expectedInntekter[2].fom
                }
                arIUtlandetEtter16 shouldBe request.utlandAntallAar
                epsPensjon shouldBe true
                eps2G shouldBe true
            }
        }

        should("map spec to KLP request DTO without fremtidige inntekter") {
            val uttaksdato = LocalDate.of(2025, 2, 1)
            val request = OffentligTjenestepensjonFra2025SimuleringSpec(
                pid = Pid("12345678901"),
                sisteInntekt = 100000,
                utlandAntallAar = 3,
                epsHarPensjon = true,
                epsHarInntektOver2G = true,
                afpErForespurt = true,
                uttaksdato = uttaksdato,
                foedselsdato = LocalDate.of(1990, 1, 1),
                fremtidigeInntekter = null,
                gjelderApoteker = false
            )

            val result: KlpSimulerTjenestepensjonRequest = KlpMapper.toRequestDto(request)

            with(result) {
                personId shouldBe request.pid.value
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
                arIUtlandetEtter16 shouldBe request.utlandAntallAar
                epsPensjon shouldBe true
                eps2G shouldBe true
            }
        }
    }
})