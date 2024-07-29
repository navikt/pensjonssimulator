package no.nav.pensjon.simulator.tech.doc

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration of OpenAPI (formerly known as Swagger).
 */
@Configuration
class OpenApiConfiguration {

    @Bean
    fun openApi() =
        OpenAPI()
            .info(
                Info()
                    .title("Pensjonssimulator API")
                    .description("Tjenester for simulering av alderspensjon")
                    .version("v0.2.0")
            )
}
