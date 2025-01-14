package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1.TpoSimuleringResultMapperV1
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1.TpoSimuleringResultV1
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1.TpoSimuleringSpecMapperV1
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1.TpoSimuleringSpecV1
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2.TpoSimuleringResultMapperV2
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2.TpoSimuleringResultV2
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2.TpoSimuleringSpecMapperV2
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2.TpoSimuleringSpecV2
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3.TpoSimuleringResultMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3.TpoSimuleringResultV3
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3.TpoSimuleringSpecMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3.TpoSimuleringSpecV3
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST-controller for simulering av alderspensjon.
 * Tjenestene er ment å brukes av tjenestepensjonsordninger (TPO).
 * TPO gjør ikke kall til pensjonssimulator direkte, men må gå via pensjon-pen.
 */
@RestController
@RequestMapping("api/tpo")
@SecurityRequirement(name = "BearerAuthentication")
class TpoViaPenAlderspensjonController(
    private val simulatorCore: SimulatorCore,
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon for tjenestepensjonsordning (V1)",
        description = "Lager en prognose for utbetaling av alderspensjon (versjon 1 av tjenesten).",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "100",
                description = "Simulering av alderspensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptabel input. Det kan være: " +
                        " (1) helt uttak ikke etter gradert uttak," +
                        " (1) inntekt ikke 1. i måneden," +
                        " (3) inntekter har lik startdato, " +
                        " (4) negativ inntekt."
            )
        ]
    )
    fun simulerAlderspensjonV1(@RequestBody specV1: TpoSimuleringSpecV1): TpoSimuleringResultV1 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV1" }
        countCall(FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = TpoSimuleringSpecMapperV1.fromDto(specV1)
            val result: SimulatorOutput = simulatorCore.simuler(spec)
            TpoSimuleringResultMapperV1.toDto(result)
        } catch (e: EgressException) {
            handle(e)!!
        } catch (e: BadRequestException) {
            badRequest(e)!!
        } catch (e: InvalidEnumValueException) {
            badRequest(e)!!
        } finally {
            traceAid.end()
        }
    }

    @PostMapping("v2/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon for tjenestepensjonsordning (V2)",
        description = "Lager en prognose for utbetaling av alderspensjon (versjon 2 av tjenesten).",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon utført."
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
    fun simulerAlderspensjonV2(@RequestBody specV2: TpoSimuleringSpecV2): TpoSimuleringResultV2 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV2" }
        countCall(FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = TpoSimuleringSpecMapperV2.fromDto(specV2)
            val result: SimulatorOutput = simulatorCore.simuler(spec)
            TpoSimuleringResultMapperV2.toDto(result)
        } catch (e: EgressException) {
            handle(e)!!
        } catch (e: BadRequestException) {
            badRequest(e)!!
        } catch (e: InvalidEnumValueException) {
            badRequest(e)!!
        } finally {
            traceAid.end()
        }
    }

    @PostMapping("v3/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon for tjenestepensjonsordning (V3)",
        description = "Lager en prognose for utbetaling av alderspensjon (versjon 3 av tjenesten).",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon utført."
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
    fun simulerAlderspensjonV3(@RequestBody specV3: TpoSimuleringSpecV3): TpoSimuleringResultV3 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV3" }
        countCall(FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = TpoSimuleringSpecMapperV3.fromDto(specV3)
            val result: SimulatorOutput = simulatorCore.simuler(spec)
            TpoSimuleringResultMapperV3.toDto(result)
        } catch (e: EgressException) {
            handle(e)!!
        } catch (e: BadRequestException) {
            badRequest(e)!!
        } catch (e: InvalidEnumValueException) {
            badRequest(e)!!
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon for TPO via PEN"
        private const val FUNCTION_ID = "ap-tpo-pen"
    }
}
