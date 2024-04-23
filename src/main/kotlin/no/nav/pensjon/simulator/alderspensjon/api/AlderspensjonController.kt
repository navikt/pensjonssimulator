package no.nav.pensjon.simulator.alderspensjon.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.api.acl.AlderspensjonMapperV1.resultV1
import no.nav.pensjon.simulator.alderspensjon.api.acl.AlderspensjonResultV1
import no.nav.pensjon.simulator.alderspensjon.api.acl.AlderspensjonSpecV1
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class AlderspensjonController(
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v4/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon",
        description = "Lager en prognose for utbetaling av alderspensjon. NB: Foreløpig er responsen hardkodet.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon utført"
            )
        ]
    )
    fun simulerFolketrygdbeholdning(@RequestBody spec: AlderspensjonSpecV1): AlderspensjonResultV1 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $spec" }

        return try {
            resultV1(success = true)
                .also { log.debug { "$FUNCTION_ID response: $it" } }
        } catch (e: EgressException) {
            handle(e)!!
        } catch (e: BadRequestException) {
            badRequest(e)!!
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon"
        private const val FUNCTION_ID = "v4/simuler-alderspensjon"
    }
}
