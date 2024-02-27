package no.nav.pensjon.simulator.uttak.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.uttak.UttakService
import no.nav.pensjon.simulator.uttak.api.acl.UttakSpecMapperV1.fromSpecV1
import no.nav.pensjon.simulator.uttak.api.acl.TidligstMuligUttakResultV1
import no.nav.pensjon.simulator.uttak.api.acl.TidligstMuligUttakSpecV1
import no.nav.pensjon.simulator.uttak.api.acl.UttakResultMapperV1.resultV1
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class UttakController(
    private val service: UttakService,
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/tidligst-mulig-uttak")
    @Operation(
        summary = "Tidligst mulig uttak",
        description = "Finner den tidligst mulige dato for uttak av alderspensjon",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Utledning av uttaksdato utført"
            )
        ]
    )
    fun tidligstMuligUttak(@RequestBody spec: TidligstMuligUttakSpecV1): TidligstMuligUttakResultV1 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $spec" }

        return try {
            resultV1(timed(service::finnTidligstMuligUttak, fromSpecV1(spec), FUNCTION_ID))
                .also { log.debug { "$FUNCTION_ID response: $it" } }
        } catch (e: EgressException) {
            handle(e)!!
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved henting av pensjonsavtaler"
        private const val FUNCTION_ID = "v1/tidligst-mulig-uttak"
    }
}
