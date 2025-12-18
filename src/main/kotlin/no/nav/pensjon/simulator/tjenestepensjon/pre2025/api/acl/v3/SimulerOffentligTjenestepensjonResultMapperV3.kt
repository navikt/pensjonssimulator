package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.Feilkode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1

object SimulerOffentligTjenestepensjonResultMapperV3 {

    fun toDto(resultV1: SimulerOffentligTjenestepensjonResultV1): SimulerOffentligTjenestepensjonResultV3 {

        if (resultV1.brukerErIkkeMedlemAvTPOrdning) {
            return SimulerOffentligTjenestepensjonResultV3(emptyList(), Feilkode.BRUKER_IKKE_MEDLEM_AV_TP_ORDNING)
        }

        if (resultV1.brukerErMedlemAvTPOrdningSomIkkeStoettes) {
            return SimulerOffentligTjenestepensjonResultV3(
                simulertPensjonListe = null,
                feilkode = Feilkode.TP_ORDNING_STOETTES_IKKE,
                relevanteTpOrdninger = resultV1.relevanteTpOrdninger
            )
        }

        val simulertPensjon = SimulertPensjonResultV3(
            tpnr = resultV1.tpnr,
            navnOrdning = resultV1.navnOrdning,
            inkluderteOrdninger = resultV1.inkluderteOrdningerListe,
            leverandorUrl = resultV1.leverandorUrl,
            utbetalingsperioder = resultV1.utbetalingsperiodeListe.map { periodeV1 ->
                UtbetalingsperiodeResultV3(
                    grad = periodeV1?.uttaksgrad,
                    arligUtbetaling = periodeV1?.arligUtbetaling,
                    datoFom = periodeV1?.datoFom?.toNorwegianDate(),
                    datoTom = periodeV1?.datoTom?.toNorwegianDate(),
                    ytelsekode = periodeV1?.ytelsekode?.toString()
                )
            }
        )

        return SimulerOffentligTjenestepensjonResultV3(
            listOf(simulertPensjon),
            feilkode = resultV1.feilkode,
            relevanteTpOrdninger = resultV1.relevanteTpOrdninger
        )
    }
}