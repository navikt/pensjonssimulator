package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1

object SimulerOffentligTjenestepensjonResultMapperV2 {

    fun toDto(resultV1: SimulerOffentligTjenestepensjonResultV1): SimulerOffentligTjenestepensjonResultV2 {

        if (resultV1.brukerErIkkeMedlemAvTPOrdning || resultV1.brukerErMedlemAvTPOrdningSomIkkeStoettes) {
            return SimulerOffentligTjenestepensjonResultV2()
        }

        val simulertPensjon = SimulertPensjonResultV2(
            tpnr = resultV1.tpnr,
            navnOrdning = resultV1.navnOrdning,
            inkluderteOrdninger = resultV1.inkluderteOrdningerListe,
            leverandorUrl = resultV1.leverandorUrl,
            inkluderteTpnr = null,
            utelatteTpnr = null,
            status = null,
            feilkode = null,
            feilbeskrivelse = null,
            utbetalingsperioder = resultV1.utbetalingsperiodeListe.map { periodeV1 ->
                UtbetalingsperiodeResultV2(
                    grad = periodeV1?.uttaksgrad,
                    arligUtbetaling = periodeV1?.arligUtbetaling,
                    datoFom = periodeV1?.datoFom?.toNorwegianDate(),
                    datoTom = periodeV1?.datoTom?.toNorwegianDate(),
                    ytelsekode = periodeV1?.ytelsekode?.toString()
                )
            }
        )

        return SimulerOffentligTjenestepensjonResultV2(
            listOf(simulertPensjon),
            feilrespons = resultV1.errorResponse?.let {
                SimulerOFTPErrorResponseV2(
                    errorCode = it.errorCode,
                    errorMessage = it.errorMessage
                )
            }
        )
    }
}