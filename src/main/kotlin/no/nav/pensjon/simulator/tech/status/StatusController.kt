package no.nav.pensjon.simulator.tech.status

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class StatusController {

    @GetMapping("v1/status")
    fun status() = StatusV1(status = "OK")
}

data class StatusV1(val status: String)
