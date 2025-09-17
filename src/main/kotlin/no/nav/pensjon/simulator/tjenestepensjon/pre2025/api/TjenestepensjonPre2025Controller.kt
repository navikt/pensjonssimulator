package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonMapper.fromDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonSpecV1
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/nav")
@SecurityRequirement(name = "BearerAuthentication")
class TjenestepensjonPre2025Controller(
    private val traceAid: TraceAid,
    private val service: TjenestepensjonSimuleringPre2025Service,
): ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/simuler-oftp/pre-2025")
    @Operation(
        summary = "Simuler tjenestepensjon pre2025 V1",
        description = "Henter en prognose for utbetaling av tjenestepensjon fra SPK.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av tjenestepensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptabel input. Det kan være: " +
                        " (1) helt uttak ikke etter gradert uttak," +
                        " (2) inntekt ikke 1. i måneden," +
                        " (3) inntekter har lik startdato, " +
                        " (4) negativ inntekt."
            )
        ]
    )
    fun simulerV1(@RequestBody specV1: SimulerOffentligTjenestepensjonSpecV1) : ResponseEntity<SimulerOffentligTjenestepensjonResultV1> {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV1" }
        countCall(FUNCTION_ID)
        try {
            val res: SimulerOffentligTjenestepensjonResultV1 = service.simuler(fromDto(specV1))
            log.debug { "$FUNCTION_ID response: $res" }
            return ResponseEntity.ok(res)
        }
        finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    companion object {
        const val FUNCTION_ID = "nav-tps-pre-2025"
        const val ERROR_MESSAGE = "feil ved simulering av tjenestepensjon pre 2025"
    }
}