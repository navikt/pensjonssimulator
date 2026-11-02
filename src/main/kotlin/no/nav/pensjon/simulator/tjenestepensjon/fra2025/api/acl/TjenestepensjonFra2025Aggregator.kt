package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsforhold
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
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger

object TjenestepensjonFra2025Aggregator {

    fun aggregerVellykketRespons(
        simulertTjenestepensjon: SimulertTjenestepensjonMedMaanedsUtbetalinger,
        tpOrdninger: List<String>
    ) =
        SimulerOffentligTjenestepensjonFra2025ResultV1(
            relevanteTpOrdninger = tpOrdninger,
            simuleringsResultatStatus = SimuleringsResultatStatusDto(ResultatTypeDto.SUCCESS),
            simuleringsResultat = SimuleringsResultatDto(
                tpLeverandoer = simulertTjenestepensjon.tpLeverandoer.description,
                tpNummer = simulertTjenestepensjon.tpNummer,
                utbetalingsperioder = aggregerTilAarligePerioder(simulertTjenestepensjon.utbetalingsperioder),
                betingetTjenestepensjonErInkludert = simulertTjenestepensjon.betingetTjenestepensjonErInkludert
            ),
            serviceData = simulertTjenestepensjon.serviceData
        )

    fun aggregerTilAarligePerioder(maanedsutbetalinger: List<Maanedsutbetaling>): List<UtbetalingPerAlder> {
        val aarligeUtbetalinger = mutableListOf<UtbetalingPerAlder>()
        for (index in maanedsutbetalinger.indices) {
            val sluttAlder = maanedsutbetalinger
                .getOrNull(index + 1)
                ?.fraOgMedAlder
                ?.let(::minusEnMaaned)
            aarligeUtbetalinger.add(
                UtbetalingPerAlder(
                    startAlder = maanedsutbetalinger[index].fraOgMedAlder,
                    sluttAlder = sluttAlder,
                    maanedligBeloep = maanedsutbetalinger[index].maanedsBeloep,
                )
            )
        }
        return aarligeUtbetalinger
    }

    fun aggregerVellykketResponsV2(
        simulertTjenestepensjon: SimulertTjenestepensjonMedMaanedsUtbetalinger,
        tpOrdninger: List<RelevantOrdningV2>
    ) =
        SimulerOffentligTjenestepensjonFra2025ResultV2(
            relevanteTpOrdninger = tpOrdninger,
            simuleringsResultatStatus = SimuleringsResultatStatusDtoV2(ResultatTypeDtoV2.SUCCESS),
            simuleringsResultat = SimuleringsResultatDtoV2(
                tpLeverandoer = simulertTjenestepensjon.tpLeverandoer.description,
                tpNummer = simulertTjenestepensjon.tpNummer,
                utbetalingsperioder = aggregerTilAarligePerioderV2(simulertTjenestepensjon.utbetalingsperioder),
                betingetTjenestepensjonErInkludert = simulertTjenestepensjon.betingetTjenestepensjonErInkludert
            ),
            serviceData = simulertTjenestepensjon.serviceData
        )

    fun mapToRelevantTpOrdning(tpOrdninger: List<Tjenestepensjonsforhold>) =
        tpOrdninger.map { RelevantOrdningV2(it.navn, tpNummer = it.tpNr) }

    fun aggregerTilAarligePerioderV2(maanedsutbetalinger: List<Maanedsutbetaling>): List<UtbetalingPerAlderV2> {
        val aarligeUtbetalinger = mutableListOf<UtbetalingPerAlderV2>()
        for (index in maanedsutbetalinger.indices) {
            val sluttAlder = maanedsutbetalinger
                .getOrNull(index + 1)
                ?.fraOgMedAlder
                ?.let(::minusEnMaaned)
            aarligeUtbetalinger.add(
                UtbetalingPerAlderV2(
                    startAlder = maanedsutbetalinger[index].fraOgMedAlder,
                    sluttAlder = sluttAlder,
                    maanedligBeloep = maanedsutbetalinger[index].maanedsBeloep,
                )
            )
        }
        return aarligeUtbetalinger
    }

    private fun minusEnMaaned(alder: Alder): Alder =
        if (alder.maaneder == 0)
            Alder(alder.aar - 1, MAANEDER_PER_AAR - 1)
        else
            Alder(alder.aar, alder.maaneder - 1)
}