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
                    .description("For å kunne bruke tjenestene må scope i autentiseringen mot Maskinporten settes til nav:pensjonssimulator:simulering. Dette er simuleringstjenester for tjenestepensjonsordninger i offentlig sektor, og benyttes for å kunne simulere alderspensjon fra folketrygden for brukere med tjenestepensjonsforhold.")
                    .version("v0.3.0")
            ).components(
                Components()
                    .addSecuritySchemes(
                        "BearerAuthentication",
                        SecurityScheme()
                            .name("BearerAuthentication")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("NAV-intern informasjon: For å anskaffe et token i dev kan du bruke https://pensjon-maskinporten-test.intern.dev.nav.no/")
                    )
            )
}
