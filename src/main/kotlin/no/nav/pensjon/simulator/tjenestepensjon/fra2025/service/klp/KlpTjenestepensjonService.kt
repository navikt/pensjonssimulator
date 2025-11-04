package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.IkkeSisteOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TomSimuleringFraTpOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TpUtil.grupperMedDatoFra
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TpUtil.redact
import org.springframework.stereotype.Service

@Service
class KlpTjenestepensjonService(private val client: KlpTjenestepensjonClientFra2025) {

    private val log = KotlinLogging.logger {}

    fun simuler(
        spec: SimulerOffentligTjenestepensjonFra2025SpecV1,
        tpNummer: String
    ): Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> {
        val ordning = client.service().shortName

        return client.simuler(spec, tpNummer)
            .fold(
                onSuccess = {
                    when {
                        it.erSisteOrdning.not() -> Result.failure(IkkeSisteOrdningException(ordning))
                        it.utbetalingsperioder.isEmpty() -> Result.failure(TomSimuleringFraTpOrdningException(ordning))
                        else -> Result.success(filtrertTjenestepensjon(tpNummer, tjenestepensjon = it, spec))
                    }
                },
                onFailure = { Result.failure(it) }
            ).also {
                it.onSuccess {
                    log.info { "tjenestepensjonsrequest til $ordning: ${redact(it.serviceData.toString())}" }
                }
            }
    }

    private fun filtrertTjenestepensjon(
        tpNummer: String,
        tjenestepensjon: SimulertTjenestepensjon,
        spec: SimulerOffentligTjenestepensjonFra2025SpecV1
    ) =
        SimulertTjenestepensjonMedMaanedsUtbetalinger(
            tpLeverandoer = client.service().description,
            tpNummer = tpNummer,
            ordningsListe = tjenestepensjon.ordningsListe,
            utbetalingsperioder = grupperMedDatoFra(
                utbetalingsliste = tjenestepensjon.utbetalingsperioder.filterNot(::ekskludert),
                foedselsdato = spec.foedselsdato
            ),
            aarsakIngenUtbetaling = tjenestepensjon.aarsakIngenUtbetaling,
            betingetTjenestepensjonErInkludert = false,
            serviceData = tjenestepensjon.serviceData
        )

    private companion object {

        private val ekskluderteYtelseTyper: List<String> =
            TjenestepensjonYtelseType.entries.filter { it.ekskludert }.map { it.kode }

        private fun ekskludert(periode: Utbetalingsperiode): Boolean =
            periode.ytelseType in ekskluderteYtelseTyper
    }
}
