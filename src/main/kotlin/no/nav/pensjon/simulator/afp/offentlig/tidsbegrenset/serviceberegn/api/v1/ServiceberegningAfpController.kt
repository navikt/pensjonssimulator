package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetAfpSimuleringFacade
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl.ServiceberegningAfpResultDto
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl.ServiceberegningAfpResultMapper.transferable
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl.ServiceberegningAfpSpecDto
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl.ServiceberegningAfpSpecMapper.fromDto
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl.ServiceberegningProblemDto
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl.ServiceberegningProblemtypeDto
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.json.writeValueAsRedactedString
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.validity.IngressErrorHandler.extractSafeMessage
import no.nav.pensjon.simulator.validity.IngressErrorHandler.extractUnsafeMessages
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.json.JsonMapper

/**
 * REST-controller for simulering av AFP for serviceberegning.
 */
@RestController
@RequestMapping("api/nav")
@SecurityRequirement(name = "BearerAuthentication")
class ServiceberegningAfpController(
    private val service: TidsbegrensetAfpSimuleringFacade,
    private val traceAid: TraceAid,
    private val jsonMapper: JsonMapper,
    statistikk: StatistikkService
) : ControllerBase(traceAid = traceAid, statistikk = statistikk) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/simuler-afp-serviceberegning")
    @Operation(
        summary = "Simuler AFP for serviceberegning (V1)",
        description = "Lager en pensjonsprognose for AFP for serviceberegning" +
                " (versjon 1 av tjenesten)."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering utført, eller personen har utilstrekkelig opptjening/trygdetid"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptable inndata."
            ),
            ApiResponse(
                responseCode = "401",
                description = "Simulering kunne ikke utføres pga. manglende/feilaktig autentisering."
            ),
            ApiResponse(
                responseCode = "403",
                description = "Simulering kunne ikke utføres pga. manglende tilgang til tjenesten."
            ),
            ApiResponse(
                responseCode = "404",
                description = "Simulering kunne ikke utføres fordi angitt person ikke finnes i systemet."
            ),
            ApiResponse(
                responseCode = "500",
                description = "Simulering kunne ikke utføres pga. feil i systemet."
            )
        ]
    )
    fun simulerAfpForServiceberegning(
        @RequestBody spec: ServiceberegningAfpSpecDto
    ): ResponseEntity<ServiceberegningAfpResultDto> {
        traceAid.begin()
        countCall(functionName = FUNCTION_ID)
        log.info { "spec ${jsonMapper.writeValueAsRedactedString(spec)}" }

        return try {
            registrerHendelse(simuleringstype = SimuleringTypeEnum.AFP)
            val result = service.simulerAfpForServiceberegning(fromDto(spec))
            ResponseEntity.status(HttpStatus.OK).body(transferable(result))
        } catch (e: Exception) {
            log.error(e) { "$FUNCTION_ID intern feil for spec ${jsonMapper.writeValueAsRedactedString(spec)}" }
            throw e
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    @ExceptionHandler(value = [HttpMessageNotReadableException::class])
    private fun handleMalformedRequest(e: Exception): ResponseEntity<ServiceberegningAfpResultDto> =
        with(ServiceberegningProblemtypeDto.ANNEN_KLIENTFEIL) {
            log.warn(e) { "$FUNCTION_ID - request not readable - ${extractUnsafeMessages(e)}" }
            ResponseEntity.status(this.httpStatus).body(problem(type = this, beskrivelse = extractSafeMessage(e)))
        }

    @ExceptionHandler(value = [Exception::class])
    private fun internalError(e: Exception): ResponseEntity<ServiceberegningAfpResultDto> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(problem(ServiceberegningProblemtypeDto.ANNEN_SERVERFEIL, e.javaClass.simpleName))

    private companion object {
        private const val TJENESTE = "simulering AFP for serviceberegning"
        private const val ERROR_MESSAGE = "feil ved $TJENESTE"
        private const val FUNCTION_ID = "afp-sb"

        private fun problem(type: ServiceberegningProblemtypeDto, beskrivelse: String) =
            ServiceberegningAfpResultDto(
                beregnetAfp = null,
                opptjeningListe = emptyList(),
                problem = ServiceberegningProblemDto(type, beskrivelse)
            )
    }
}