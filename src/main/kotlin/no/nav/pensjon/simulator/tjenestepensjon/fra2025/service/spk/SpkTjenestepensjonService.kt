package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService.Companion.PEN_715_SIMULER_SPK
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.IkkeSisteOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TomSimuleringFraTpOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TpUtil
import org.springframework.stereotype.Service

@Service
class SpkTjenestepensjonService(private val client: SpkTjenestepensjonClientFra2025, private val featureToggleService: FeatureToggleService) {
    private val log = KotlinLogging.logger {}

    fun simuler(request: SimulerOffentligTjenestepensjonFra2025SpecV1, tpNummer: String): Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> {
        if (!featureToggleService.isEnabled(PEN_715_SIMULER_SPK)) {
            return failure()
        }

        return client.simuler(request, tpNummer)
            .fold(
                onSuccess = {
                    if (!it.erSisteOrdning)
                        Result.failure(IkkeSisteOrdningException(client.service().shortName))
                    else if (it.utbetalingsperioder.isEmpty())
                        Result.failure(TomSimuleringFraTpOrdningException(client.service().shortName))
                    else
                        Result.success(
                            SimulertTjenestepensjonMedMaanedsUtbetalinger(
                                tpLeverandoer = SpkMapper.PROVIDER_FULLT_NAVN,
                                tpNummer = tpNummer,
                                ordningsListe = it.ordningsListe,
                                utbetalingsperioder = TpUtil.grupperMedDatoFra(fjernAfp(it.utbetalingsperioder), request.foedselsdato),
                                betingetTjenestepensjonErInkludert = it.betingetTjenestepensjonErInkludert,
                                serviceData = it.serviceData
                            )
                        )
                },
                onFailure = { Result.failure(it) }
            )
    }

    private fun failure(): Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> {
        val message = "Simulering av tjenestepensjon hos ${client.service().shortName} er slått av"
        log.warn { message }
        return Result.failure(TjenestepensjonSimuleringException(message))
    }

    fun fjernAfp(utbetalingsliste: List<Utbetalingsperiode>) : List<Utbetalingsperiode> {
        val afp = utbetalingsliste.filter { it.ytelseType == "OAFP" }
        if (afp.isNotEmpty()){
            log.info { "AFP fra ${client.service().shortName}: $afp" }
        }
        return utbetalingsliste.filter { it.ytelseType != "OAFP" }
    }
}