package no.nav.pensjon.simulator.tech.env

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class DevelopmentEnvironmentController {

    @GetMapping("devenv/entra")
    @Hidden
    fun entraEnvironment(): String = environmentVariable("AZURE_APP_CLIENT_SECRET")

    @GetMapping("devenv/maskinporten/client-jwk")
    @Hidden
    fun maskinportenEnvironment(): String = environmentVariable("MASKINPORTEN_CLIENT_JWK") //MASKINPORTEN_CLIENT_ID

    @GetMapping("devenv/maskinporten/client-id")
    @Hidden
    fun maskinportenClientIdEnvironment(): String = environmentVariable("MASKINPORTEN_CLIENT_ID")

    private companion object {
        private fun environmentVariable(name: String) =
            if (System.getenv("NAIS_CLUSTER_NAME") == "dev-gcp")
                System.getenv(name)
            else "forbidden"
    }
}
