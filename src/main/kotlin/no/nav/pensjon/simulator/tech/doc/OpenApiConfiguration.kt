package no.nav.pensjon.simulator.tech.doc

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
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
                    .version("v0.3.0")
            ).components(
                Components()
                    .addSecuritySchemes(
                        "Bearer Authentication",
                        SecurityScheme()
                            .name("Bearer Authentication")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("For å anskaffe et token kan du bruke https://pensjon-maskinporten-test.intern.dev.nav.no/?scopes=nav:pensjonssimulator:simulering")
                    )
            )
}
