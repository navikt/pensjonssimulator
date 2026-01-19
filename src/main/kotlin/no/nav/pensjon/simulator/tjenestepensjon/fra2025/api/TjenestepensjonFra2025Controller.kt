package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.tech.metric.Metrics
import no.nav.pensjon.simulator.tech.selftest.SelfTest.Companion.APPLICATION_NAME
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.OffentligTjenestepensjonFra2025SimuleringSpecMapperV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.ResultatTypeDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025ResultV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.TjenestepensjonFra2025Aggregator.aggregerVellykketRespons
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.*
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TPSimuleringResultatFra2025
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Service
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@RestController
@RequestMapping("api/nav")
@SecurityRequirement(name = "BearerAuthentication")
class TjenestepensjonFra2025Controller(
    private val traceAid: TraceAid,
    private val tjenestepensjonFra2025Service: TjenestepensjonFra2025Service
): ControllerBase(traceAid)  {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/simuler-oftp/fra-2025")
    @Operation(
        summary = "Simuler tjenestepensjon fra 2025 V1",
        description = "Henter simulering av utbetaling av tjenestepensjon fra TP-levernadør.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av tjenestepensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptabel input."
            )
        ]
    )
    fun simuler(@RequestBody specV1: SimulerOffentligTjenestepensjonFra2025SpecV1): SimulerOffentligTjenestepensjonFra2025ResultV1 {
        log.debug { "Simulerer tjenestepensjon fra 2025 for spec: $specV1" }
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV1" }
        countCall(FUNCTION_ID)
        try {
            validateSpec(specV1)
            val simuleringsresultat: Pair<List<String>, Result<SimulertTjenestepensjonMedMaanedsUtbetalinger>> = tjenestepensjonFra2025Service.simuler(
                OffentligTjenestepensjonFra2025SimuleringSpecMapperV1.fromDto(specV1))
            val relevanteTpOrdninger = simuleringsresultat.first
            return simuleringsresultat.second.fold(
                onSuccess = {
                    val aggregerVellykketRespons: SimulerOffentligTjenestepensjonFra2025ResultV1 = aggregerVellykketRespons(it, relevanteTpOrdninger)
                        .also { _ -> Metrics.countTjenestepensjonSimuleringFra2025(TPSimuleringResultatFra2025.OK, it.tpLeverandoer.shortName) }
                    log.debug { "Simulering vellykket: $aggregerVellykketRespons" }
                    aggregerVellykketRespons
                },
                onFailure = { e ->
                    countMetric(e)
                    when (e) {
                        is BrukerErIkkeMedlemException -> SimulerOffentligTjenestepensjonFra2025ResultV1(ResultatTypeDto.BRUKER_ER_IKKE_MEDLEM_HOS_TP_ORDNING, e.message, relevanteTpOrdninger)
                        is TpOrdningStoettesIkkeException -> SimulerOffentligTjenestepensjonFra2025ResultV1(ResultatTypeDto.TP_ORDNING_ER_IKKE_STOTTET, e.message, relevanteTpOrdninger)
                        is TjenestepensjonSimuleringException -> SimulerOffentligTjenestepensjonFra2025ResultV1(ResultatTypeDto.TEKNISK_FEIL_FRA_TP_ORDNING, e.message, relevanteTpOrdninger)
                        is TomSimuleringFraTpOrdningException -> SimulerOffentligTjenestepensjonFra2025ResultV1(ResultatTypeDto.INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING, "Simulering fra ${e.tpOrdning} inneholder ingen utbetalingsperioder", relevanteTpOrdninger)
                        is IkkeSisteOrdningException -> SimulerOffentligTjenestepensjonFra2025ResultV1(ResultatTypeDto.INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING, "Simulering fra ${e.tpOrdning} inneholder ingen utbetalingsperioder", relevanteTpOrdninger)
                        is TpregisteretException -> { log.error(e) { "Simulering feilet pga feil fra tpregisteret: ${e.message}" }; throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message) }
                        else -> { log.error(e) { "Simulering feilet: ${e.message}" }; throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message) }
                    }
                })
        } catch (e: UgyldigSpecException) {
            countMetric(e)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    private fun countMetric(e: Throwable) {
        if (e is MetricAware) {
            Metrics.countTjenestepensjonSimuleringFra2025(e.metricResult, e.metricSource)
        } else {
            Metrics.countTjenestepensjonSimuleringFra2025(TPSimuleringResultatFra2025.TEKNISK_FEIL_I_NAV, APPLICATION_NAME)
        }
    }

    private fun validateSpec(spec: SimulerOffentligTjenestepensjonFra2025SpecV1) {
        if (spec.erApoteker) {
            throw UgyldigSpecException("Apoteker støttes ikke")
        }
        if (spec.foedselsdato.isBefore(MINSTE_FOEDSELSDATO)) {
            throw UgyldigSpecException("Fødselsdato før $MINSTE_FOEDSELSDATO støttes ikke")
        }
    }

    companion object {
        private val MINSTE_FOEDSELSDATO = LocalDate.of(1963, 1, 1)
        const val FUNCTION_ID = "nav-tps-fra-2025"
        const val ERROR_MESSAGE = "feil ved simulering av tjenestepensjon fra 2025"
    }
}