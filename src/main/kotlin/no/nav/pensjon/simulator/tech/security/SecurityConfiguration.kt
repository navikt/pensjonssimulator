package no.nav.pensjon.simulator.tech.security

import jakarta.servlet.http.HttpServletRequest
import no.nav.pensjon.simulator.tech.security.egress.SecurityContextEnricher
import no.nav.pensjon.simulator.tech.security.ingress.AuthenticationEnricherFilter
import no.nav.pensjon.simulator.tech.security.ingress.TokenAudienceValidator
import no.nav.pensjon.simulator.tech.security.ingress.TokenScopeValidator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
open class SecurityConfiguration {

    @Bean
    open fun filterChain(
        http: HttpSecurity,
        authResolver: AuthenticationManagerResolver<HttpServletRequest>,
        securityContextEnricher: SecurityContextEnricher,
    ): SecurityFilterChain =
        http
            .addFilterAfter(
                AuthenticationEnricherFilter(securityContextEnricher),
                BasicAuthenticationFilter::class.java
            )
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        HttpMethod.GET,
                        "/internal/**",
                        "/api/v1/status",
                        "/api/devenv",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/error"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.authenticationManagerResolver(authResolver)
            }
            .build()

    @Bean
    @Primary
    open fun authenticationManagerResolver(
        @Qualifier("entra-id-provider") entraProvider: ProviderManager,
        @Qualifier("maskinporten-provider") maskinportenProvider: ProviderManager
    ): AuthenticationManagerResolver<HttpServletRequest> =
        ApiAuthenticationManagerResolver(entraProvider, maskinportenProvider)

    @Bean("entra-id-provider")
    @Primary
    open fun entraIdProvider(
        @Value("\${azure.openid.config.issuer}") issuer: String,
        @Value("\${azure-app.client-id}") audience: String
    ): ProviderManager =
        ProviderManager(
            JwtAuthenticationProvider(
                jwtDecoder(issuer, tokenValidator = TokenAudienceValidator(audience))
            )
        )

    @Bean("maskinporten-provider")
    open fun maskinportenProvider(
        @Value("\${maskinporten.issuer}") issuer: String,
        @Value("\${ps.maskinporten.scope}") scope: String
    ): ProviderManager =
        ProviderManager(
            JwtAuthenticationProvider(
                jwtDecoder(issuer, tokenValidator = TokenScopeValidator(scope))
            )
        )


    private companion object {

        private fun jwtDecoder(issuer: String, tokenValidator: OAuth2TokenValidator<Jwt>): JwtDecoder =
            jwtDecoder(issuer).apply {
                setJwtValidator(
                    DelegatingOAuth2TokenValidator(
                        JwtValidators.createDefaultWithIssuer(issuer),
                        tokenValidator
                    )
                )
            }

        private fun jwtDecoder(issuer: String) =
            JwtDecoders.fromIssuerLocation(issuer) as NimbusJwtDecoder
    }
}
