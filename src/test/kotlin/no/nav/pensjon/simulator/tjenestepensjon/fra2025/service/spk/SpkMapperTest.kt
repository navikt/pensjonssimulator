package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.*
import java.time.LocalDate

class SpkMapperTest : ShouldSpec({

    val idag = LocalDate.now()
    val fjoraaretsFoersteDag = LocalDate.of(idag.year - 1, 1, 1)

    context("toRequestDto") {
        should("mappe spesifikasjon til request-DTO hvor bruker ber om å beregne AFP") {
            val uttaksdato = LocalDate.of(2025, 2, 1)

            val spec = OffentligTjenestepensjonFra2025SimuleringSpec(
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

            val result: SpkSimulerTjenestepensjonRequest = SpkMapper.toRequestDto(spec)

            with(result) {
                personId shouldBe "12345678901"
                uttaksListe shouldHaveSize 5
                with(uttaksListe[0]) {
                    ytelseType shouldBe "APOF2020"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[1]) {
                    ytelseType shouldBe "OAFP"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[2]) {
                    ytelseType shouldBe "OT6370"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[3]) {
                    ytelseType shouldBe "PAASLAG"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[4]) {
                    ytelseType shouldBe "SAERALDERSPAASLAG"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(fremtidigInntektListe[0]) {
                    aarligInntekt shouldBe 100000
                    fraOgMedDato shouldBe fjoraaretsFoersteDag
                }
                with(fremtidigInntektListe[1]) {
                    aarligInntekt shouldBe 0
                    fraOgMedDato shouldBe uttaksdato
                }
                aarIUtlandetEtter16 shouldBe 3
                epsPensjon shouldBe true
                eps2G shouldBe true
            }
        }

        should("mappe spesifikasjon til request-DTO hvor bruker ikke ber om å beregne AFP") {
            val uttaksdato = LocalDate.of(2025, 2, 1)
            val spec = OffentligTjenestepensjonFra2025SimuleringSpec(
                pid = Pid("12345678901"),
                sisteInntekt = 100000,
                utlandAntallAar = 3,
                epsHarPensjon = true,
                epsHarInntektOver2G = true,
                afpErForespurt = false,
                uttaksdato = uttaksdato,
                foedselsdato = LocalDate.of(1990, 1, 1),
                fremtidigeInntekter = null,
                gjelderApoteker = false
            )

            val result: SpkSimulerTjenestepensjonRequest = SpkMapper.toRequestDto(spec)

            with(result) {
                personId shouldBe "12345678901"
                uttaksListe shouldHaveSize 5
                with(uttaksListe[0]) {
                    ytelseType shouldBe "APOF2020"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[1]) {
                    ytelseType shouldBe "BTP"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[2]) {
                    ytelseType shouldBe "OT6370"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[3]) {
                    ytelseType shouldBe "PAASLAG"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[4]) {
                    ytelseType shouldBe "SAERALDERSPAASLAG"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(fremtidigInntektListe[0]) {
                    aarligInntekt shouldBe 100000
                    fraOgMedDato shouldBe fjoraaretsFoersteDag
                }
                with(fremtidigInntektListe[1]) {
                    aarligInntekt shouldBe 0
                    fraOgMedDato shouldBe uttaksdato
                }
                aarIUtlandetEtter16 shouldBe 3
                epsPensjon shouldBe true
                eps2G shouldBe true
            }
        }

        should("mappe spesifikasjon til request-DTO hvor bruker har ulike fremtidige inntekter") {
            val uttaksdato = LocalDate.of(2025, 2, 1)
            val spec = OffentligTjenestepensjonFra2025SimuleringSpec(
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

            val result: SpkSimulerTjenestepensjonRequest = SpkMapper.toRequestDto(spec)

            with(result) {
                personId shouldBe "12345678901"
                fremtidigInntektListe shouldHaveSize 4
                with(fremtidigInntektListe[0]) {
                    aarligInntekt shouldBe spec.sisteInntekt
                    fraOgMedDato.isBefore(idag.minusYears(1)) shouldBe true
                }
                with(fremtidigInntektListe[1]) {
                    aarligInntekt shouldBe 4
                    fraOgMedDato shouldBe LocalDate.of(2025, 2, 1)
                }
                with(fremtidigInntektListe[2]) {
                    aarligInntekt shouldBe 5
                    fraOgMedDato shouldBe LocalDate.of(2026, 3, 1)
                }
                with(fremtidigInntektListe[3]) {
                    aarligInntekt shouldBe 6
                    fraOgMedDato shouldBe LocalDate.of(2027, 4, 1)
                }
            }
        }
    }

    context("fromResponseDto") {
        should("mappe respons-DTO til 'simulert tjenestepensjon' domeneobjekt") {
            val response = SpkSimulerTjenestepensjonResponse(
                listOf(InkludertOrdning(tpnr = "3010")),
                listOf(
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 2, 1),
                        delytelseListe = listOf(
                            Delytelse(ytelseType = "BTP", maanedligBelop = 141),
                            Delytelse(ytelseType = "PAASLAG", maanedligBelop = 268)
                        )
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2030, 2, 1),
                        delytelseListe = listOf(
                            Delytelse(ytelseType = "OT6370", maanedligBelop = 779),
                            Delytelse(ytelseType = "PAASLAG", maanedligBelop = 268)
                        )
                    )
                ),
                listOf(
                    AarsakIngenUtbetaling(
                        statusKode = "IKKE_STOETTET",
                        statusBeskrivelse = "Ikke stoettet",
                        ytelseType = "SAERALDERSPAASLAG"
                    )
                )
            )

            val result: SimulertTjenestepensjon = SpkMapper.fromResponseDto(response)

            with(result) {
                ordningsListe shouldHaveSize 1
                ordningsListe[0].tpNummer shouldBe "3010"
                utbetalingsperioder shouldHaveSize 4
                with(utbetalingsperioder[0]) {
                    fom shouldBe LocalDate.of(2025, 2, 1)
                    maanedligBelop shouldBe 141
                    ytelseType shouldBe "BTP"
                }
                with(utbetalingsperioder[1]) {
                    maanedligBelop shouldBe 268
                    ytelseType shouldBe "PAASLAG"
                }
                with(utbetalingsperioder[2]) {
                    fom shouldBe LocalDate.of(2030, 2, 1)
                    maanedligBelop shouldBe 779
                    ytelseType shouldBe "OT6370"
                }
                with(utbetalingsperioder[3]) {
                    maanedligBelop shouldBe 268
                    ytelseType shouldBe "PAASLAG"
                }
                aarsakIngenUtbetaling shouldHaveSize 1
                aarsakIngenUtbetaling[0] shouldBe "Ikke stoettet: SAERALDERSPAASLAG"
            }
        }

        should("indikere 'er siste ordning' når årsak for 'ingen utbetaling' tilsier det") {
            val response = SpkSimulerTjenestepensjonResponse(
                listOf(InkludertOrdning(tpnr = "3010")),
                listOf(
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 2, 1),
                        delytelseListe = listOf(
                            Delytelse(ytelseType = "BTP", maanedligBelop = 141),
                            Delytelse(ytelseType = "PAASLAG", maanedligBelop = 268)
                        )
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2030, 2, 1),
                        delytelseListe = listOf(
                            Delytelse(ytelseType = "OT6370", maanedligBelop = 779),
                            Delytelse(ytelseType = "PAASLAG", maanedligBelop = 268)
                        )
                    )
                ),
                listOf(
                    AarsakIngenUtbetaling(
                        statusKode = "IKKE_STOETTET",
                        statusBeskrivelse = "Ikke stoettet",
                        ytelseType = "SAERALDERSPAASLAG"
                    )
                )
            )

            SpkMapper.fromResponseDto(response).erSisteOrdning shouldBe true
        }

        should("indikere 'ikke siste ordning' når årsak for 'ingen utbetaling' tilsier det") {
            val response = SpkSimulerTjenestepensjonResponse(
                listOf(InkludertOrdning(tpnr = "3010")),
                listOf(
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 2, 1),
                        delytelseListe = listOf(
                            Delytelse(ytelseType = "BTP", maanedligBelop = 141),
                            Delytelse(ytelseType = "PAASLAG", maanedligBelop = 268)
                        )
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2030, 2, 1),
                        delytelseListe = listOf(
                            Delytelse(ytelseType = "OT6370", maanedligBelop = 779),
                            Delytelse(ytelseType = "PAASLAG", maanedligBelop = 268)
                        )
                    )
                ),
                listOf(
                    AarsakIngenUtbetaling(
                        statusKode = "IKKE_SISTE_ORDNING",
                        statusBeskrivelse = "Ikke siste ordning",
                        ytelseType = ""
                    )
                )
            )

            SpkMapper.fromResponseDto(response).erSisteOrdning shouldBe false
        }
    }
})