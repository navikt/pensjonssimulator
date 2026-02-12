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
open class OpenApiConfiguration {

    @Bean
    open fun openApi() =
        OpenAPI()
            .info(
                Info()
                    .title("Pensjonssimulator API")
                    .description("Tjenester for simulering av pensjon (prognose for pensjonsutbetaling).\\\n" +
                            "Klienter må autentisere seg via Maskinporten – se hver enkelt tjeneste for hvilket *scope* som skal brukes.")
                    .version("v1.2.0")
            ).components(
                Components()
                    .addSecuritySchemes(
                        "BearerAuthentication",
                        SecurityScheme()
                            .name("BearerAuthentication")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Nav-intern informasjon:\\\nFor å anskaffe et token i dev kan du bruke [pensjon-maskinporten-test](https://pensjon-maskinporten-test.intern.dev.nav.no/)")
                    )
            )
}
