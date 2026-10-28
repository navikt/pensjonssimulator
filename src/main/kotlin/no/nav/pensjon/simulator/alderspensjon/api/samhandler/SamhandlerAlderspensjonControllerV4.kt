package no.nav.pensjon.simulator.alderspensjon.api.samhandler

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonResult
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonService
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v4.*
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v4.AlderspensjonResultMapperV4.resultV4
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.json.writeValueAsRedactedString
import no.nav.pensjon.simulator.tech.sporing.web.SporingInterceptor
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import no.nav.pensjon.simulator.validity.IngressErrorHandler.extractUnsafeMessages
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.json.JsonMapper

/**
 * REST-controller for simulering av alderspensjon for personer født 1963 eller senere.
 * Tjenestene er ment å brukes av samhandlere (tjenestepensjonsordninger).
 * Samhandlere gjør kall til pensjonssimulator via API-gateway.
 */
@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class SamhandlerAlderspensjonControllerV4(
    private val service: AlderspensjonService,
    private val traceAid: TraceAid,
    private val jsonMapper: JsonMapper,
    statistikk: StatistikkService,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) : ControllerBase(traceAid, statistikk, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v4/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon for personer født i 1963 eller senere.",
        description = "Lager en prognose for utbetaling av alderspensjon for personer født i 1963 eller senere." +
                "\\\n\\\n*Scope*:" +
                "\\\n– Uten delegering: **nav:pensjonssimulator:simulering**" +
                "\\\n– Med delegering: **nav:pensjon/simulering.read**"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptable inndata. Det kan være:" +
                        "\\\n(1) helt uttak ikke etter gradert uttak," +
                        "\\\n(2) inntekt ikke 1. i måneden," +
                        "\\\n(3) inntekter har lik startdato, " +
                        "\\\n(4) negativ inntekt."
            )
        ]
    )
    fun simulerAlderspensjon(
        @RequestBody specV4: AlderspensjonSpecV4,
        request: HttpServletRequest
    ): ResponseEntity<AlderspensjonResultV4> {
        traceAid.begin(request)
        countCall(FUNCTION_ID_V4)

        return try {
            val spec: AlderspensjonSpec = AlderspensjonSpecMapperV4.fromDto(specV4)
            registrerHendelse(simuleringstype = SimuleringTypeEnum.ALDER)
            request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, spec.pid)
            verifiserAtBrukerTilknyttetTpLeverandoer(spec.pid)
            val result: AlderspensjonResult = service.simulerAlderspensjon(spec)
            result.problem?.let(::problemEntity) ?: okEntity(result)
        } catch (e: Exception) {
            log.error(e) { "$$FUNCTION_ID_V4 intern feil for spec ${jsonMapper.writeValueAsRedactedString(specV4)}" }
            throw e
        } finally {
            traceAid.end()
        }
    }

    @ExceptionHandler(value = [HttpMessageNotReadableException::class])
    private fun handleMalformedRequest(e: Exception): ResponseEntity<AlderspensjonResultV4> =
        badRequestEntity(e)
            .also {
                log.warn { "$FUNCTION_ID_V4 - request not readable - ${extractUnsafeMessages(e)}" }
            }

    @ExceptionHandler(value = [Exception::class])
    private fun handleInternalServerError(e: Exception): ResponseEntity<AlderspensjonResultV4> =
        internalErrorEntity(e)

    override fun errorMessage() = ERROR_MESSAGE

    private fun okEntity(result: AlderspensjonResult): ResponseEntity<AlderspensjonResultV4> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(resultV4(result))

    private fun problemEntity(problem: Problem): ResponseEntity<AlderspensjonResultV4> =
        ResponseEntity
            .status(problem.type.let(::httpStatus))
            .body(AlderspensjonResultV4.forFeil(problem, callId = traceAid.callId()))

    private fun badRequestEntity(e: Exception): ResponseEntity<AlderspensjonResultV4> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(AlderspensjonResultV4.forFeil(e, callId = traceAid.callId()))

    private fun internalErrorEntity(e: Exception): ResponseEntity<AlderspensjonResultV4> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(AlderspensjonResultV4.forFeil(e, callId = traceAid.callId()))

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon V4"
        private const val FUNCTION_ID_V4 = "apv4"

        /**
         * NB: Dette endepunktet bruker HTTP-status 'OK' for de fleste unntakstilfeller.
         * Problemårsaken formidles gjennom feltet 'aarsakListeIkkeSuksess' i responsen.
         */
        private fun httpStatus(type: ProblemType) =
            when (type) {
                ProblemType.ANNEN_KLIENTFEIL -> HttpStatus.BAD_REQUEST
                ProblemType.IMPLEMENTASJONSFEIL -> HttpStatus.INTERNAL_SERVER_ERROR
                ProblemType.INTERN_DATA_INKONSISTENS -> HttpStatus.INTERNAL_SERVER_ERROR
                ProblemType.TREDJEPARTSFEIL -> HttpStatus.INTERNAL_SERVER_ERROR
                ProblemType.ANNEN_SERVERFEIL -> HttpStatus.INTERNAL_SERVER_ERROR
                else -> HttpStatus.OK
            }
    }
}
