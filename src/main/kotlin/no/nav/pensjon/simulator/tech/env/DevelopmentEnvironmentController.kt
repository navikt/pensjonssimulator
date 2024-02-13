package no.nav.pensjon.simulator.tech.env

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("internal/env")
class DevelopmentEnvironmentController {

    @GetMapping("entra")
    fun entraEnvironment(): String = environmentVariable("AZURE_APP_CLIENT_SECRET")

    @GetMapping("entra2")
    fun entraEnvironment2(): String = environmentVariable("AZURE_APP_CLIENT_ID")

    private companion object {
        private fun environmentVariable(name: String) =
            if (System.getenv("NAIS_CLUSTER_NAME") == "dev-gcp")
                System.getenv(name)
            else "forbidden"
    }
}
