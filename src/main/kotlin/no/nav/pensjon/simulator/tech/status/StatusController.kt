package no.nav.pensjon.simulator.tech.status

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.Operation
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class StatusController(private val generelleDataHolder: GenerelleDataHolder) {

    private val log = mu.KotlinLogging.logger {}

    @GetMapping("v1/status")
    @Operation(
        summary = "Status",
        description = "Sjekker status for applikasjonens helsetilstand, og (hvis OK) viser siste gyldige opptjeningsår som brukes av simulatoren"
    )
    fun status(): StatusV1 =
        try {
            StatusV1(
                status = "OK",
                sisteGyldigeOpptjeningsaar = generelleDataHolder.getSisteGyldigeOpptjeningsaar()
            )
        } catch (e: Exception) {
            log.error(e) { "Feil ved henting av siste gyldige opptjeningsår" }
            StatusV1(status = "FEIL")
        }
}

@JsonInclude(NON_NULL)
data class StatusV1(
    val status: String,
    val sisteGyldigeOpptjeningsaar: Int? = null
)
