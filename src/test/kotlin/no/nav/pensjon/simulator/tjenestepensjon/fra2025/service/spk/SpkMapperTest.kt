package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerTjenestepensjonFremtidigInntektDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.*
import java.time.LocalDate

class SpkMapperTest : ShouldSpec({

    val idag = LocalDate.now()
    val foersteJanuarIfjor = LocalDate.of(idag.year - 1, 1, 1)

    context("mapToRequest") {
        should("map request hvor bruker ber om å beregne AFP") {
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
                erApoteker = false
            )

            val result: SpkSimulerTjenestepensjonRequest = SpkMapper.mapToRequest(request)

            with(result) {
                personId shouldBe "12345678901"
                uttaksListe shouldHaveSize 5
                with(uttaksListe[0]) {
                    ytelseType shouldBe "PAASLAG"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[1]) {
                    ytelseType shouldBe "APOF2020"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[2]) {
                    ytelseType shouldBe "OT6370"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[3]) {
                    ytelseType shouldBe "SAERALDERSPAASLAG"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[4]) {
                    ytelseType shouldBe "OAFP"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(fremtidigInntektListe[0]) {
                    aarligInntekt shouldBe 100000
                    fraOgMedDato shouldBe foersteJanuarIfjor
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

        should("map request hvor bruker ikke ber om å beregne AFP") {
            val uttaksdato = LocalDate.of(2025, 2, 1)
            val request = SimulerOffentligTjenestepensjonFra2025SpecV1(
                pid = "12345678901",
                sisteInntekt = 100000,
                aarIUtlandetEtter16 = 3,
                epsPensjon = true,
                eps2G = true,
                brukerBaOmAfp = false,
                uttaksdato = uttaksdato,
                foedselsdato = LocalDate.of(1990, 1, 1),
                erApoteker = false
            )

            val result: SpkSimulerTjenestepensjonRequest = SpkMapper.mapToRequest(request)

            with(result) {
                personId shouldBe "12345678901"
                uttaksListe shouldHaveSize 5
                with(uttaksListe[0]) {
                    ytelseType shouldBe "PAASLAG"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[1]) {
                    ytelseType shouldBe "APOF2020"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[2]) {
                    ytelseType shouldBe "OT6370"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[3]) {
                    ytelseType shouldBe "SAERALDERSPAASLAG"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(uttaksListe[4]) {
                    ytelseType shouldBe "BTP"
                    fraOgMedDato shouldBe uttaksdato
                }
                with(fremtidigInntektListe[0]) {
                    aarligInntekt shouldBe 100000
                    fraOgMedDato shouldBe foersteJanuarIfjor
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

        should("map request hvor bruker har ulike fremtidige inntekter") {
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

            val result: SpkSimulerTjenestepensjonRequest = SpkMapper.mapToRequest(request)

            with(result) {
                personId shouldBe "12345678901"
                fremtidigInntektListe shouldHaveSize 4
                with(fremtidigInntektListe[0]) {
                    aarligInntekt shouldBe request.sisteInntekt
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

    context("mapToResponse") {
        should("map to response") {
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

            val result = SpkMapper.mapToResponse(response)

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

        should("map response som er siste ordning") {
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

            val result = SpkMapper.mapToResponse(response)

            result.erSisteOrdning shouldBe true
        }

        should("map response som ikke er siste ordning") {
            val resp = SpkSimulerTjenestepensjonResponse(
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

            val result = SpkMapper.mapToResponse(resp)

            result.erSisteOrdning shouldBe false
        }
    }
})