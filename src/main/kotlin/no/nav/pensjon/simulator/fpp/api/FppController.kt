package no.nav.pensjon.simulator.fpp.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.fpp.FppSimuleringFacade
import no.nav.pensjon.simulator.fpp.FppSimuleringResult
import no.nav.pensjon.simulator.fpp.FppSimuleringSpec
import no.nav.pensjon.simulator.fpp.api.acl.v1.SimuleringTypeV1
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.json.JsonMapper

/**
 * REST-controller for simulering for FPP (framtidige pensjonspoeng).
 */
@RestController
@RequestMapping("api/nav")
@SecurityRequirement(name = "BearerAuthentication")
class FppController(
    private val service: FppSimuleringFacade,
    private val traceAid: TraceAid,
    private val jsonMapper: JsonMapper,
    statistikk: StatistikkService,
) : ControllerBase(traceAid = traceAid, statistikk = statistikk) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/simuler-for-fpp")
    @Operation(
        summary = "Simuler for FPP (V1)",
        description = "Lager en pensjonsprognose for beregning av framtidige pensjonspoeng (FPP)" +
                " (versjon 1 av tjenesten)."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering for FPP utført, eller personen har utilstrekkelig opptjening/trygdetid"
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
    fun simulerForFppV1(
        @RequestParam("simuleringstype") simuleringType: SimuleringTypeV1,
        @RequestBody spec: FppSimuleringSpec
    ): ResponseEntity<FppSimuleringResult> {
        traceAid.begin()
        countCall(functionName = FUNCTION_ID)
        log.info { "spec ${jsonMapper.writeValueAsRedactedString(spec)}" }

        return try {
            registrerHendelse(simuleringstype = simuleringType.internalValue)
            val result = service.simulerPensjon(simuleringType.internalValue, spec)
            ResponseEntity.status(HttpStatus.OK).body(result)
        } catch (e: Exception) {
            log.error(e) { "$FUNCTION_ID intern feil for spec ${jsonMapper.writeValueAsRedactedString(spec)}" }
            throw e
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    @ExceptionHandler(value = [Exception::class])
    private fun internalError(e: Exception): ResponseEntity<FppSimuleringResult> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem(e))

    private companion object {
        private const val TJENESTE = "simulering for FPP"
        private const val ERROR_MESSAGE = "feil ved $TJENESTE"
        private const val FUNCTION_ID = "fpp"

        private fun problem(e: Exception) =
            FppSimuleringResult(
                afpOrdning = null,
                beregnetAfp = null,
                problem = Problem(
                    type = ProblemType.ANNEN_KLIENTFEIL,
                    beskrivelse = e.message ?: e.javaClass.simpleName
                )
            )
    }
}

private val FNR_REGEX = """[0-9]{2}([0-9]{4})[0-9]{5}""".toRegex()

fun JsonMapper.writeValueAsRedactedString(value: Any) =
    FNR_REGEX.replace(this.writeValueAsString(value), "**$1*****")

