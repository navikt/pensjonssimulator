package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Utbetalingsperiode
import no.nav.pensjon.simulator.validity.Problem

object SimulerOffentligTjenestepensjonResultMapperV3 {

    fun toDto(result: SimulerOffentligTjenestepensjonResult): SimulerOffentligTjenestepensjonResultV3 =
        when {
            result.brukerErIkkeMedlemAvTPOrdning ->
                SimulerOffentligTjenestepensjonResultV3(
                    simulertPensjonListe = emptyList(),
                    feilkode = FeilkodeV3.BRUKER_IKKE_MEDLEM_AV_TP_ORDNING
                )

            result.brukerErMedlemAvTPOrdningSomIkkeStoettes ->
                SimulerOffentligTjenestepensjonResultV3(
                    simulertPensjonListe = null,
                    feilkode = FeilkodeV3.TP_ORDNING_STOETTES_IKKE,
                    relevanteTpOrdninger = result.relevanteTpOrdninger
                )

            else -> SimulerOffentligTjenestepensjonResultV3(
                simulertPensjonListe = listOf(pensjon(result)),
                feilkode = result.feilkode?.let(FeilkodeV3::externalValue),
                relevanteTpOrdninger = result.relevanteTpOrdninger,
                problem = result.problem?.let(::problem)
            )
        }

    private fun pensjon(result: SimulerOffentligTjenestepensjonResult) =
        SimulertPensjonResultV3(
            tpnr = result.tpnr,
            navnOrdning = result.navnOrdning,
            inkluderteOrdninger = result.inkluderteOrdningerListe,
            leverandorUrl = result.leverandorUrl,
            utbetalingsperioder = result.utbetalingsperiodeListe.map(::utbetalingsperiode)
        )

    private fun utbetalingsperiode(source: Utbetalingsperiode) =
        UtbetalingsperiodeResultV3(
            grad = source.uttaksgrad,
            arligUtbetaling = source.arligUtbetaling,
            datoFom = source.datoFom.toNorwegianDate(), //TODO use LocalDate
            datoTom = source.datoTom?.toNorwegianDate(),
            ytelsekode = source.ytelsekode?.toString() //TODO use enum
        )

    private fun problem(source: Problem) =
        Pre2025TpV3Problem(
            kode = Pre2025TpV3ProblemType.from(internalValue = source.type),
            beskrivelse = source.beskrivelse
        )
}