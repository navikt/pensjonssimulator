package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.TjenestepensjonSimuleringPre2025Facade
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.SimulerOffentligTjenestepensjonSpecMapperV3
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.SimulerOffentligTjenestepensjonResultMapperV3
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.SimulerOffentligTjenestepensjonResultV3
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.SimulerOffentligTjenestepensjonSpecV3
import org.springframework.http.HttpStatus
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
    private val mapper: SimulerOffentligTjenestepensjonSpecMapperV3,
    private val service: TjenestepensjonSimuleringPre2025Facade,
    statistikk: StatistikkService
) : ControllerBase(traceAid = traceAid, statistikk = statistikk) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v3/simuler-oftp/pre-2025")
    @Operation(
        summary = "Simuler tjenestepensjon pre2025 V3",
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
    fun simulerV3(
        @RequestBody specV3: SimulerOffentligTjenestepensjonSpecV3
    ): ResponseEntity<SimulerOffentligTjenestepensjonResultV3> {
        traceAid.begin()
        log.debug { "$FUNCTION_ID_V3 request: $specV3" }
        countCall(FUNCTION_ID_V3)

        try {
            val simuleringSpec = mapper.fromDto(specV3)
            registrerHendelse(simuleringstype = simuleringSpec.type)

            val result = service.simuler(
                simuleringSpec,
                stillingsprosentSpec = mapper.stillingsprosentFromDto(specV3)
            )

            return ResponseEntity.status(result.problem?.let { HttpStatus.BAD_REQUEST } ?: HttpStatus.OK)
                .body(SimulerOffentligTjenestepensjonResultMapperV3.toDto(result))
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    companion object {
        const val FUNCTION_ID_V3 = "nav-tps-pre-2025-v3"
        const val ERROR_MESSAGE = "feil ved simulering av tjenestepensjon pre 2025"
    }
}