package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.IkkeSisteOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TomSimuleringFraTpOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Client
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TpUtil.grupperMedDatoFra
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TpUtil.redact
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class KlpTjenestepensjonService(
    @param:Qualifier("klp") private val client: TjenestepensjonFra2025Client
) {
    private val log = KotlinLogging.logger {}

    fun simuler(
        spec: OffentligTjenestepensjonFra2025SimuleringSpec,
        tpNummer: String
    ): Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> =
        client.simuler(spec, tpNummer)
            .fold(
                onSuccess = { result(pensjon = it, spec.foedselsdato, tpNummer) },
                onFailure = { Result.failure(exception = it) }
            ).also {
                logSuccess(result = it)
            }

    private fun result(
        pensjon: SimulertTjenestepensjon,
        foedselsdato: LocalDate,
        tpNummer: String
    ): Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> =
        when {
            pensjon.erSisteOrdning.not() ->
                Result.failure(exception = IkkeSisteOrdningException(tpOrdning = client.service.shortName))

            pensjon.utbetalingsperioder.isEmpty() ->
                Result.failure(exception = TomSimuleringFraTpOrdningException(tpOrdning = client.service.shortName))

            else -> Result.success(value = filtrertTjenestepensjon(tpNummer, pensjon, foedselsdato))
        }

    private fun filtrertTjenestepensjon(
        tpNummer: String,
        pensjon: SimulertTjenestepensjon,
        foedselsdato: LocalDate
    ) =
        SimulertTjenestepensjonMedMaanedsUtbetalinger(
            tpLeverandoer = client.service,
            tpNummer = tpNummer,
            ordningsListe = pensjon.ordningsListe,
            utbetalingsperioder = grupperMedDatoFra(
                utbetalingsliste = pensjon.utbetalingsperioder.filterNot(::ekskludert),
                foedselsdato
            ),
            aarsakIngenUtbetaling = pensjon.aarsakIngenUtbetaling,
            betingetTjenestepensjonErInkludert = false,
            serviceData = pensjon.serviceData
        )

    private fun logSuccess(result: Result<SimulertTjenestepensjonMedMaanedsUtbetalinger>) {
        result.onSuccess {
            log.info {
                "tjenestepensjonsrequest til ${client.service.shortName}: ${redact(it.serviceData.toString())}"
            }
        }
    }

    private companion object {

        private val ekskluderteYtelseTyper: List<String> =
            TjenestepensjonYtelseType.entries.filter { it.erEkskludertForKlp }.map { it.kode }

        private fun ekskludert(periode: Utbetalingsperiode): Boolean =
            periode.ytelseType in ekskluderteYtelseTyper
    }
}
