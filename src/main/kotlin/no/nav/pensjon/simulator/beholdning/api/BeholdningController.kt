package no.nav.pensjon.simulator.beholdning.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import mu.KotlinLogging
import no.nav.pensjon.simulator.beholdning.api.acl.FolketrygdbeholdningResultMapperV1.resultV1
import no.nav.pensjon.simulator.beholdning.api.acl.SimulerFolketrygdbeholdningResultV1
import no.nav.pensjon.simulator.beholdning.api.acl.SimulerFolketrygdbeholdningSpecV1
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class BeholdningController(
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/simuler-folketrygdbeholdning")
    @Operation(
        summary = "Simuler folketrygdbeholdning",
        description = "Lager en prognose for pensjonsbeholdning i folketrygden",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av folketrygdbeholdning utført"
            )
        ]
    )
    fun simulerFolketrygdbeholdning(@RequestBody spec: SimulerFolketrygdbeholdningSpecV1):
            SimulerFolketrygdbeholdningResultV1 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $spec" }

        return try {
            resultV1()
        } catch (e: EgressException) {
            handle(e)!!
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering folketrygdbeholdning"
        private const val FUNCTION_ID = "v1/simuler-folketrygdbeholdning"
    }
}
