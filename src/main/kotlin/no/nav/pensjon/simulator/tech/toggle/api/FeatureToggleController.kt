package no.nav.pensjon.simulator.tech.toggle.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class FeatureToggleController(
    val service: FeatureToggleService,
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {

    private val log = KotlinLogging.logger {}

    @GetMapping("feature/{name}")
    @Operation(
        summary = "Hvorvidt en gitt funksjonsbryter er skrudd på",
        description = "Hent status for en gitt funksjonsbryter (hvorvidt funksjonen er skrudd på)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Sjekking av funksjonsbryter-status"
            ),
            ApiResponse(
                responseCode = "503",
                description = "Sjekking av funksjonsbryter-status kunne ikke utføres av tekniske årsaker",
                content = [Content(examples = [ExampleObject(value = SERVICE_UNAVAILABLE_EXAMPLE)])]
            ),
        ]
    )
    fun isEnabled(@PathVariable(value = "name") featureName: String): EnablementDto {
        traceAid.begin()
        log.debug { "Request for status for funksjonsbryter '$featureName'" }

        return try {
            EnablementDto(timed(service::isEnabled, featureName, "is feature enabled"))
                .also { log.debug { "Funksjonsbryter-status respons: $it" } }
        }
         finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved sjekking av funksjonsbryter-status"
    }
}
