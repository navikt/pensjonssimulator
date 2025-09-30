package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService.Companion.SIMULER_KLP
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.dto.request.SimulerTjenestepensjonRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.IkkeSisteOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TomSimuleringFraTpOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TpOrdningStoettesIkkeException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TpUtil
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TpUtil.redact
import org.springframework.stereotype.Service

@Service
class KLPTjenestepensjonService(private val client: KLPTjenestepensjonClientFra2025, private val featureToggleService: FeatureToggleService) {
    private val log = KotlinLogging.logger {}

    fun simuler(request: SimulerTjenestepensjonRequestDto, tpNummer: String): Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> {
        if (!featureToggleService.isEnabled(SIMULER_KLP)) {
            return loggOgReturn()
        }

        return client.simuler(request, tpNummer)
            .fold(
                onSuccess = {
                    if (!it.erSisteOrdning){
                        Result.failure(IkkeSisteOrdningException(client.service().shortName))
                    }
                    else if (it.utbetalingsperioder.isEmpty())
                        Result.failure(TomSimuleringFraTpOrdningException(client.service().shortName))
                    else
                        Result.success(
                            SimulertTjenestepensjonMedMaanedsUtbetalinger(
                                tpLeverandoer = KLPMapper.PROVIDER_FULLT_NAVN,
                                tpNummer = tpNummer,
                                ordningsListe = it.ordningsListe,
                                utbetalingsperioder = TpUtil.grupperMedDatoFra(
                                    eksluderYtelser(it.utbetalingsperioder),
                                    request.foedselsdato
                                ),
                                aarsakIngenUtbetaling = it.aarsakIngenUtbetaling,
                                betingetTjenestepensjonErInkludert = false,
                                serviceData = it.serviceData
                            ))
                },
                onFailure = { Result.failure(it) }
            ).also { it.onSuccess {
                log.info { "tjenestepensjonsrequest til ${client.service().shortName}: ${redact(it.serviceData.toString())}" } }
            }

    }

    private fun eksluderYtelser(utbetalingsperiode: List<Utbetalingsperiode>): List<Utbetalingsperiode> {
        return utbetalingsperiode.filter { it.ytelseType !in setOf("OAFP", "BTP") }
    }

    private fun loggOgReturn(): Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> {
        val message = "Simulering av tjenestepensjon hos ${client.service().shortName} er slått av"
        log.warn { message }
        return Result.failure(TpOrdningStoettesIkkeException(client.service().shortName))
    }

}