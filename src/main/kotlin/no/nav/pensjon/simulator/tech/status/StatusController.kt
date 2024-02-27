package no.nav.pensjon.simulator.tech.status

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class StatusController {

    @GetMapping("v1/status")
    @Operation(summary = "Status", description = "Sjekker status for applikasjonens helsetilstand")
    fun status() = StatusV1(status = "OK")
}

data class StatusV1(val status: String)
