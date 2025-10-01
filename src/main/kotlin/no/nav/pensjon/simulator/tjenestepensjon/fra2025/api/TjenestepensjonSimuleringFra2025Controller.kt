package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.BrukerErIkkeMedlemException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TomSimuleringFraTpOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TpOrdningStoettesIkkeException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.TjenestepensjonFra2025Aggregator.aggregerVellykketRespons
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerTjenestepensjonRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.ResultatTypeDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerTjenestepensjonResponseDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.IkkeSisteOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TpregisteretException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Service
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class TjenestepensjonSimuleringFra2025Controller(
    private val tjenestepensjonFra2025Service: TjenestepensjonFra2025Service
) {
    private val log = KotlinLogging.logger {}

    @PostMapping("/v2025/tjenestepensjon/v1/simulering")
    fun simuler(@RequestBody request: SimulerTjenestepensjonRequestDto): SimulerTjenestepensjonResponseDto {
        log.debug { "Simulerer tjenestepensjon for request: $request" }
        val simuleringsresultat = tjenestepensjonFra2025Service.simuler(request)
        val relevanteTpOrdninger = simuleringsresultat.first
        return simuleringsresultat.second.fold(
            onSuccess = {
                val aggregerVellykketRespons: SimulerTjenestepensjonResponseDto = aggregerVellykketRespons(it, relevanteTpOrdninger)
                log.debug { "Simulering vellykket: $aggregerVellykketRespons" }
                aggregerVellykketRespons
            },
            onFailure = { e ->
                when (e) {
                    is BrukerErIkkeMedlemException -> SimulerTjenestepensjonResponseDto(ResultatTypeDto.BRUKER_ER_IKKE_MEDLEM_HOS_TP_ORDNING, e.message, relevanteTpOrdninger)
                    is TpOrdningStoettesIkkeException -> SimulerTjenestepensjonResponseDto(ResultatTypeDto.TP_ORDNING_ER_IKKE_STOTTET, e.message, relevanteTpOrdninger)
                    is TjenestepensjonSimuleringException -> SimulerTjenestepensjonResponseDto(ResultatTypeDto.TEKNISK_FEIL_FRA_TP_ORDNING, e.message, relevanteTpOrdninger)
                    is TomSimuleringFraTpOrdningException -> SimulerTjenestepensjonResponseDto(ResultatTypeDto.INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING, "Simulering fra ${e.tpOrdning} inneholder ingen utbetalingsperioder", relevanteTpOrdninger)
                    is IkkeSisteOrdningException -> SimulerTjenestepensjonResponseDto(ResultatTypeDto.INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING, "Simulering fra ${e.tpOrdning} inneholder ingen utbetalingsperioder", relevanteTpOrdninger)
                    is TpregisteretException -> loggOgReturnerTekniskFeil(e)
                    else -> loggOgReturnerTekniskFeil(RuntimeException(e))
                }
            })
    }

    private fun loggOgReturnerTekniskFeil(e: RuntimeException): SimulerTjenestepensjonResponseDto {
        log.error(e) { "Simulering feilet: ${e.message}" }
        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
    }

}