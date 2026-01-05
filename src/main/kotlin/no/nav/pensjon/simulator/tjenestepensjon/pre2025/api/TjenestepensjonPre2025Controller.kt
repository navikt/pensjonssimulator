package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.PEN249KunTilltatMedEnTpiVerdiException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.TjenestepensjonSimuleringPre2025ForPensjonskalkulatorService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.TjenestepensjonSimuleringPre2025SpecBeregningService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.TjenestepensjonSimuleringPre2025Service
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.SimulerOffentligTjenestepensjonMapperV2
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.SimulerOffentligTjenestepensjonMapperV3
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonMapperV1.fromDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonSpecV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2.SimulerOffentligTjenestepensjonResultMapperV2.toDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.SimulerOffentligTjenestepensjonResultMapperV3
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2.SimulerOffentligTjenestepensjonSpecV2
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.SimulerOffentligTjenestepensjonResultV3
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.SimulerOffentligTjenestepensjonSpecV3
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
    private val serviceForPensjonskalkulator: TjenestepensjonSimuleringPre2025ForPensjonskalkulatorService,
    private val beregningService: TjenestepensjonSimuleringPre2025SpecBeregningService,
    private val simulerOffentligTjenestepensjonMapperV2: SimulerOffentligTjenestepensjonMapperV2,
    private val simulerOffentligTjenestepensjonMapperV3: SimulerOffentligTjenestepensjonMapperV3,
    statistikk: StatistikkService
) : ControllerBase(traceAid = traceAid, statistikk = statistikk) {
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
    fun simulerV1(@RequestBody specV1: SimulerOffentligTjenestepensjonSpecV1): ResponseEntity<SimulerOffentligTjenestepensjonResultV1> {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV1" }
        countCall(FUNCTION_ID)

        try {
            val result: SimulerOffentligTjenestepensjonResultV1 = service.simuler(fromDto(specV1))
            log.debug { "$FUNCTION_ID response: $result" }
            return ResponseEntity.ok(result)
        } finally {
            traceAid.end()
        }
    }

    @PostMapping("v2/simuler-oftp/pre-2025")
    @Operation(
        summary = "Simuler tjenestepensjon pre2025 V2",
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
    fun simulerV2(@RequestBody specV2: SimulerOffentligTjenestepensjonSpecV2): ResponseEntity<Any> {
        traceAid.begin()
        log.debug { "$FUNCTION_ID_V2 request: $specV2" }
        countCall(FUNCTION_ID_V2)

        try {
            val simuleringSpec = simulerOffentligTjenestepensjonMapperV2.fromDto(specV2)
            registrerHendelse(simuleringstype = simuleringSpec.type)

            val spec = beregningService.kompletterMedAlderspensjonsberegning(
                simuleringSpec,
                stillingsprosentSpec = simulerOffentligTjenestepensjonMapperV2.stillingsprosentFromDto(specV2)
            )

            val result = toDto(service.simuler(spec))
            log.debug { "$FUNCTION_ID_V2 response: $result" }
            return result.let { ResponseEntity.ok(it) }
        }
        catch (e: PEN249KunTilltatMedEnTpiVerdiException) {
            log.error(e) { "Er ikke tillat med mer enn en TPI verdi" };
            return ResponseEntity.internalServerError().body(e.message);
        }
        finally {
            traceAid.end()
        }
    }

    @PostMapping("v3/simuler-oftp/pre-2025")
    @Operation(
        summary = "Simuler tjenestepensjon pre2025 V2",
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
    fun simulerV3(@RequestBody specV3: SimulerOffentligTjenestepensjonSpecV3): SimulerOffentligTjenestepensjonResultV3 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID_V3 request: $specV3" }
        countCall(FUNCTION_ID_V3)

        try {
            val simuleringSpec = simulerOffentligTjenestepensjonMapperV3.fromDto(specV3)
            registrerHendelse(simuleringstype = simuleringSpec.type)

            val spec = beregningService.kompletterMedAlderspensjonsberegning(
                simuleringSpec,
                stillingsprosentSpec = simulerOffentligTjenestepensjonMapperV3.stillingsprosentFromDto(specV3)
            )

            val result = SimulerOffentligTjenestepensjonResultMapperV3.toDto(serviceForPensjonskalkulator.simuler(spec))
            log.debug { "$FUNCTION_ID_V3 response: $result" }
            return result
        }
        finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    companion object {
        const val FUNCTION_ID = "nav-tps-pre-2025"
        const val FUNCTION_ID_V2 = "nav-tps-pre-2025-v2"
        const val FUNCTION_ID_V3 = "nav-tps-pre-2025-v3"
        const val ERROR_MESSAGE = "feil ved simulering av tjenestepensjon pre 2025"
    }
}