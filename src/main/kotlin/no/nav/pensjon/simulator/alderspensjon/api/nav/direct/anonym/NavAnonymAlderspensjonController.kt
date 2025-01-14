package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.result.AnonymSimuleringResultMapperV1.mapSimuleringResult
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.result.AnonymSimuleringResultV1
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.spec.AnonymSimuleringSpecMapperV1.fromAnonymSimuleringSpecV1
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.spec.AnonymSimuleringSpecV1
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/anonym")
@SecurityRequirement(name = "BearerAuthentication")
class NavAnonymAlderspensjonController(
    private val service: SimulatorCore,
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon anonymt",
        description = "Lager en prognose for utbetaling av alderspensjon, basert på anonym input fra bruker.",
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
    fun simulerAlderspensjon(@RequestBody specV1: AnonymSimuleringSpecV1): AnonymSimuleringResultV1 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV1" }
        countCall(FUNCTION_ID)

        return try {
            val spec = fromAnonymSimuleringSpecV1(specV1)
            val result = service.simuler(spec)
            mapSimuleringResult(result)
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
        private const val ERROR_MESSAGE = "feil ved anonym simulering av alderspensjon"
        private const val FUNCTION_ID = "ap"
    }
}
