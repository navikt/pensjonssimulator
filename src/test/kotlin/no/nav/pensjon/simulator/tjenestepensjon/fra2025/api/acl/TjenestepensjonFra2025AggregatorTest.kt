package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.ResultatTypeDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025ResultV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimuleringsResultatDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimuleringsResultatStatusDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.UtbetalingPerAlder
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2.RelevantOrdningV2
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2.ResultatTypeDtoV2
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2.SimulerOffentligTjenestepensjonFra2025ResultV2
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2.SimuleringsResultatDtoV2
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2.SimuleringsResultatStatusDtoV2
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2.UtbetalingPerAlderV2
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Maanedsutbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tpregisteret.TpForhold
import java.time.LocalDate

class TjenestepensjonFra2025AggregatorTest : FunSpec({

    context("aggregering av utbetalingsperioder i V1 og V2") {
        test("returnerer tom liste når det ikke finnes utbetalinger") {
            TjenestepensjonFra2025Aggregator.aggregerTilAarligePerioder(emptyList()) shouldBe emptyList()
            TjenestepensjonFra2025Aggregator.aggregerTilAarligePerioderV2(emptyList()) shouldBe emptyList()
        }

        test("lar en enkelt periode løpe uten sluttalder") {
            val utbetalinger = listOf(maanedsutbetaling(Alder(62, 3), 5000))

            TjenestepensjonFra2025Aggregator.aggregerTilAarligePerioder(utbetalinger) shouldBe listOf(
                UtbetalingPerAlder(Alder(62, 3), null, 5000)
            )
            TjenestepensjonFra2025Aggregator.aggregerTilAarligePerioderV2(utbetalinger) shouldBe listOf(
                UtbetalingPerAlderV2(Alder(62, 3), null, 5000)
            )
        }

        test("setter sluttalder til måneden før neste periode, også ved årsskifte") {
            val utbetalinger = listOf(
                maanedsutbetaling(Alder(62, 0), 5000),
                maanedsutbetaling(Alder(65, 0), 6000),
                maanedsutbetaling(Alder(65, 1), 0),
                maanedsutbetaling(Alder(67, 6), 7000)
            )

            TjenestepensjonFra2025Aggregator.aggregerTilAarligePerioder(utbetalinger) shouldBe listOf(
                UtbetalingPerAlder(Alder(62, 0), Alder(64, 11), 5000),
                UtbetalingPerAlder(Alder(65, 0), Alder(65, 0), 6000),
                UtbetalingPerAlder(Alder(65, 1), Alder(67, 5), 0),
                UtbetalingPerAlder(Alder(67, 6), null, 7000)
            )
            TjenestepensjonFra2025Aggregator.aggregerTilAarligePerioderV2(utbetalinger) shouldBe listOf(
                UtbetalingPerAlderV2(Alder(62, 0), Alder(64, 11), 5000),
                UtbetalingPerAlderV2(Alder(65, 0), Alder(65, 0), 6000),
                UtbetalingPerAlderV2(Alder(65, 1), Alder(67, 5), 0),
                UtbetalingPerAlderV2(Alder(67, 6), null, 7000)
            )
        }
    }

    context("aggregering av vellykket respons") {
        listOf(true, false).forEach { betingetTjenestepensjon ->
            val simulering = SimulertTjenestepensjonMedMaanedsUtbetalinger(
                tpLeverandoer = EgressService.SPK,
                tpNummer = "3010",
                ordningsListe = listOf(Ordning("3010")),
                utbetalingsperioder = listOf(
                    maanedsutbetaling(Alder(62, 0), 5000),
                    maanedsutbetaling(Alder(67, 0), 6000)
                ),
                betingetTjenestepensjonErInkludert = betingetTjenestepensjon,
                serviceData = listOf("request", "response")
            )

            test("mapper alle resultatfelter i V1 med betingetTjenestepensjon=$betingetTjenestepensjon") {
                TjenestepensjonFra2025Aggregator.aggregerVellykketRespons(
                    simulering, listOf("Statens Pensjonskasse", "Storebrand")
                ) shouldBe SimulerOffentligTjenestepensjonFra2025ResultV1(
                    simuleringsResultatStatus = SimuleringsResultatStatusDto(ResultatTypeDto.SUCCESS),
                    simuleringsResultat = SimuleringsResultatDto(
                        tpLeverandoer = "Statens Pensjonskasse",
                        tpNummer = "3010",
                        utbetalingsperioder = listOf(
                            UtbetalingPerAlder(Alder(62, 0), Alder(66, 11), 5000),
                            UtbetalingPerAlder(Alder(67, 0), null, 6000)
                        ),
                        betingetTjenestepensjonErInkludert = betingetTjenestepensjon
                    ),
                    relevanteTpOrdninger = listOf("Statens Pensjonskasse", "Storebrand"),
                    serviceData = listOf("request", "response")
                )
            }

            test("mapper alle resultatfelter i V2 med betingetTjenestepensjon=$betingetTjenestepensjon") {
                val ordninger = listOf(
                    RelevantOrdningV2("Statens Pensjonskasse", "3010"),
                    RelevantOrdningV2("Storebrand", "4081")
                )

                TjenestepensjonFra2025Aggregator.aggregerVellykketResponsV2(
                    simulering, ordninger
                ) shouldBe SimulerOffentligTjenestepensjonFra2025ResultV2(
                    simuleringsResultatStatus = SimuleringsResultatStatusDtoV2(ResultatTypeDtoV2.SUCCESS),
                    simuleringsResultat = SimuleringsResultatDtoV2(
                        tpLeverandoer = "Statens Pensjonskasse",
                        tpNummer = "3010",
                        utbetalingsperioder = listOf(
                            UtbetalingPerAlderV2(Alder(62, 0), Alder(66, 11), 5000),
                            UtbetalingPerAlderV2(Alder(67, 0), null, 6000)
                        ),
                        betingetTjenestepensjonErInkludert = betingetTjenestepensjon
                    ),
                    relevanteTpOrdninger = ordninger,
                    serviceData = listOf("request", "response")
                )
            }
        }
    }

    context("mapToRelevantTpOrdning") {
        test("returnerer tom liste når det ikke finnes ordninger") {
            TjenestepensjonFra2025Aggregator.mapToRelevantTpOrdning(emptyList()) shouldBe emptyList()
        }

        test("bevarer navn, nummer og rekkefølge uavhengig av siste opptjeningsdato") {
            val ordninger = listOf(
                TpForhold("4081", "Storebrand", null),
                TpForhold("3010", "Statens Pensjonskasse", LocalDate.of(2025, 1, 1))
            )

            TjenestepensjonFra2025Aggregator.mapToRelevantTpOrdning(ordninger) shouldBe listOf(
                RelevantOrdningV2("Storebrand", "4081"),
                RelevantOrdningV2("Statens Pensjonskasse", "3010")
            )
        }
    }
}) {
    private companion object {
        fun maanedsutbetaling(alder: Alder, beloep: Int) = Maanedsutbetaling(
            fraOgMedDato = LocalDate.of(1963, 3, 1).plusYears(alder.aar.toLong()).plusMonths(alder.maaneder.toLong()),
            fraOgMedAlder = alder,
            maanedsBeloep = beloep
        )
    }
}
