package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService.Companion.PEN_715_SIMULER_SPK
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.IkkeSisteOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TomSimuleringFraTpOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Client
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TpUtil.grupperMedDatoFra
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SpkTjenestepensjonService(
    @param:Qualifier("spk") private val client: TjenestepensjonFra2025Client,
    private val featureToggleService: FeatureToggleService
) {
    private val log = KotlinLogging.logger {}

    fun simuler(
        spec: OffentligTjenestepensjonFra2025SimuleringSpec,
        tpNummer: String
    ): Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> =
        when {
            featureToggleService.isEnabled(PEN_715_SIMULER_SPK) ->
                client.simuler(spec, tpNummer).fold(
                    onSuccess = { result(pensjon = it, spec.foedselsdato, tpNummer) },
                    onFailure = { Result.failure(exception = it) }
                )

            else -> failure()
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
                .also { logAfp(utbetalingsliste = pensjon.utbetalingsperioder) }
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
            betingetTjenestepensjonErInkludert = pensjon.betingetTjenestepensjonErInkludert,
            serviceData = pensjon.serviceData
        )

    private fun failure(): Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> =
        "Simulering av tjenestepensjon hos ${client.service.shortName} er slått av".let {
            log.warn { it }
            Result.failure(TjenestepensjonSimuleringException(it, client.service.shortName))
        }

    private fun logAfp(utbetalingsliste: List<Utbetalingsperiode>) {
        val afp = utbetalingsliste.filter { it.ytelseType == TjenestepensjonYtelseType.OFFENTLIG_AFP.kode }

        if (afp.isNotEmpty()) {
            log.info { "AFP fra ${client.service.shortName}: $afp" }
        }
    }

    private companion object {

        private val ekskluderteYtelseTyper: List<String> =
            TjenestepensjonYtelseType.entries.filter { it.erEkskludertForSpk }.map { it.kode }

        private fun ekskludert(periode: Utbetalingsperiode): Boolean =
            periode.ytelseType in ekskluderteYtelseTyper
    }
}