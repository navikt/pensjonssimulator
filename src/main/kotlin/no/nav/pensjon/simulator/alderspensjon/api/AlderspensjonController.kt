package no.nav.pensjon.simulator.alderspensjon.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonService
import no.nav.pensjon.simulator.alderspensjon.api.acl.AlderspensjonResultMapperV4.resultV4
import no.nav.pensjon.simulator.alderspensjon.api.acl.AlderspensjonResultV4
import no.nav.pensjon.simulator.alderspensjon.api.acl.AlderspensjonSpecMapperV4.fromSpecV4
import no.nav.pensjon.simulator.alderspensjon.api.acl.AlderspensjonSpecV4
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
    private val service: AlderspensjonService,
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v4/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon",
        description = "Lager en prognose for utbetaling av alderspensjon.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon utført"
            )
        ]
    )
    fun simulerAlderspensjon(
        @RequestBody specV4: AlderspensjonSpecV4,
        request: HttpServletRequest
    ): AlderspensjonResultV4 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV4" }

        return try {
            val spec = fromSpecV4(specV4)
            request.setAttribute("pid", spec.pid)

            resultV4(timed(service::simulerAlderspensjon, spec, FUNCTION_ID))
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
