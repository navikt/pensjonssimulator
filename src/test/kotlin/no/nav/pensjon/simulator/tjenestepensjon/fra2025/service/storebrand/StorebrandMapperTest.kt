package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.AarsakIngenUtbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.Delytelse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.InkludertOrdning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.StorebrandSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.StorebrandSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.Utbetaling
import java.time.LocalDate

class StorebrandMapperTest : ShouldSpec({

    context("toRequestDto") {
        should("mappe spesifikasjon med alle ytelsestyper og fremtidige inntekter") {
            val spec = spec(
                afpErForespurt = true,
                fremtidigeInntekter = listOf(
                    TjenestepensjonInntektSpec(
                        fom = LocalDate.of(2027, 4, 1),
                        aarligInntekt = 600000
                    )
                )
            )

            val result: StorebrandSimulerTjenestepensjonRequest = StorebrandMapper.toRequestDto(spec)

            with(result) {
                personId shouldBe spec.pid.value
                uttaksListe.map { it.ytelseType } shouldBe listOf("ALLE")
                uttaksListe.forEach {
                    it.fraOgMedDato shouldBe spec.uttaksdato
                    it.uttaksgrad shouldBe 100
                }
                fremtidigInntektListe shouldHaveSize 2
                fremtidigInntektListe[0].aarligInntekt shouldBe spec.sisteInntekt
                fremtidigInntektListe[0].fraOgMedDato shouldBe LocalDate.now().minusYears(1).withDayOfYear(1)
                fremtidigInntektListe[1].aarligInntekt shouldBe 600000
                fremtidigInntektListe[1].fraOgMedDato shouldBe LocalDate.of(2027, 4, 1)
                aarIUtlandetEtter16 shouldBe spec.utlandAntallAar
                epsPensjon shouldBe spec.epsHarPensjon
                eps2G shouldBe spec.epsHarInntektOver2G
            }
        }

        should("mappe alle ytelsestyper uavhengig av om AFP er forespurt") {
            val spec = spec(afpErForespurt = false)

            val result = StorebrandMapper.toRequestDto(spec)

            result.uttaksListe.map { it.ytelseType } shouldBe listOf("ALLE")
        }
    }

    context("fromResponseDto") {
        should("mappe Storebrand-respons til simulert tjenestepensjon") {
            val response = response()
            val request = StorebrandMapper.toRequestDto(spec(afpErForespurt = false))

            val result: SimulertTjenestepensjon = StorebrandMapper.fromResponseDto(response, request)

            with(result) {
                tpLeverandoer shouldBe "Storebrand"
                ordningsListe.map { it.tpNummer } shouldBe listOf("4081")
                utbetalingsperioder shouldHaveSize 3
                utbetalingsperioder.map { it.maanedligBelop } shouldBe listOf(1000, 2000, 3000)
                utbetalingsperioder.map { it.ytelseType } shouldBe listOf("BTP", "PAASLAG", "OT6370")
                aarsakIngenUtbetaling shouldBe listOf("Ikke stoettet: SAERALDERSPAASLAG")
                betingetTjenestepensjonErInkludert shouldBe true
                erSisteOrdning shouldBe true
                serviceData shouldHaveSize 2
            }
        }

        should("tolke null aarsakIngenUtbetaling som siste ordning uten aarsaker") {
            val response = response().copy(aarsakIngenUtbetaling = null)

            val result = StorebrandMapper.fromResponseDto(response)

            result.erSisteOrdning shouldBe true
            result.aarsakIngenUtbetaling shouldBe emptyList()
        }

        should("tolke tom aarsakIngenUtbetaling som siste ordning uten aarsaker") {
            val response = response().copy(aarsakIngenUtbetaling = emptyList())

            val result = StorebrandMapper.fromResponseDto(response)

            result.erSisteOrdning shouldBe true
            result.aarsakIngenUtbetaling shouldBe emptyList()
        }

        should("markere respons med ikke siste ordning") {
            val response = StorebrandSimulerTjenestepensjonResponse(
                inkludertOrdningListe = listOf(InkludertOrdning(tpnr = "4081")),
                utbetalingListe = emptyList(),
                aarsakIngenUtbetaling = listOf(
                    AarsakIngenUtbetaling(
                        statusKode = "IKKE_SISTE_ORDNING",
                        statusBeskrivelse = "Ikke siste ordning",
                        ytelseType = "OAFP"
                    )
                )
            )

            val result = StorebrandMapper.fromResponseDto(response)

            result.erSisteOrdning shouldBe false
            result.aarsakIngenUtbetaling shouldBe listOf("Ikke siste ordning: OAFP")
        }

        should("mappe ikke siste ordning når ordninger og utbetalinger er null") {
            val response = StorebrandSimulerTjenestepensjonResponse(
                inkludertOrdningListe = null,
                utbetalingListe = null,
                aarsakIngenUtbetaling = listOf(
                    AarsakIngenUtbetaling(
                        statusKode = "IKKE_SISTE_ORDNING",
                        statusBeskrivelse = "Vi er ikke siste ordning",
                        ytelseType = "ALLE"
                    )
                )
            )

            val result = StorebrandMapper.fromResponseDto(response)

            with(result) {
                tpLeverandoer shouldBe "Storebrand"
                ordningsListe shouldBe emptyList()
                utbetalingsperioder shouldBe emptyList()
                aarsakIngenUtbetaling shouldBe listOf("Vi er ikke siste ordning: ALLE")
                betingetTjenestepensjonErInkludert shouldBe false
                erSisteOrdning shouldBe false
            }
        }

        should("bevare annen årsak til ingen utbetaling når ordninger og utbetalinger er null") {
            val response = StorebrandSimulerTjenestepensjonResponse(
                inkludertOrdningListe = null,
                utbetalingListe = null,
                aarsakIngenUtbetaling = listOf(
                    AarsakIngenUtbetaling(
                        statusKode = "IKKE_STOETTET",
                        statusBeskrivelse = "Ikke stoettet",
                        ytelseType = "SAERALDERSPAASLAG"
                    )
                )
            )

            val result = StorebrandMapper.fromResponseDto(response)

            with(result) {
                ordningsListe shouldBe emptyList()
                utbetalingsperioder shouldBe emptyList()
                aarsakIngenUtbetaling shouldBe listOf("Ikke stoettet: SAERALDERSPAASLAG")
                betingetTjenestepensjonErInkludert shouldBe false
                erSisteOrdning shouldBe true
            }
        }
    }
}) {
    private companion object {

        fun spec(
            afpErForespurt: Boolean,
            fremtidigeInntekter: List<TjenestepensjonInntektSpec> = emptyList()
        ) =
            OffentligTjenestepensjonFra2025SimuleringSpec(
                pid = Pid("12345678901"),
                foedselsdato = LocalDate.of(1963, 2, 5),
                uttaksdato = LocalDate.of(2026, 3, 1),
                sisteInntekt = 500000,
                utlandAntallAar = 3,
                afpErForespurt = afpErForespurt,
                epsHarPensjon = true,
                epsHarInntektOver2G = true,
                fremtidigeInntekter = fremtidigeInntekter,
                gjelderApoteker = false
            )

        fun response() =
            StorebrandSimulerTjenestepensjonResponse(
                inkludertOrdningListe = listOf(InkludertOrdning(tpnr = "4081")),
                utbetalingListe = listOf(
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2026, 3, 1),
                        delytelseListe = listOf(
                            Delytelse(ytelseType = "BTP", maanedligBelop = 1000),
                            Delytelse(ytelseType = "PAASLAG", maanedligBelop = 2000)
                        )
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2030, 3, 1),
                        delytelseListe = listOf(
                            Delytelse(ytelseType = "OT6370", maanedligBelop = 3000)
                        )
                    )
                ),
                aarsakIngenUtbetaling = listOf(
                    AarsakIngenUtbetaling(
                        statusKode = "IKKE_STOETTET",
                        statusBeskrivelse = "Ikke stoettet",
                        ytelseType = "SAERALDERSPAASLAG"
                    )
                )
            )
    }
}
