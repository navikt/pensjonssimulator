package no.nav.pensjon.simulator.tech.security

import jakarta.servlet.http.HttpServletRequest
import no.nav.pensjon.simulator.tech.security.egress.SecurityContextEnricher
import no.nav.pensjon.simulator.tech.security.ingress.AuthenticationEnricherFilter
import no.nav.pensjon.simulator.tech.security.ingress.TokenScopeValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
    open fun tokenAuthenticationManagerResolver(
        @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") issuerUri: String,
        @Value("\${ps.maskinporten.scope}") scope: String
    ): AuthenticationManagerResolver<HttpServletRequest> =
        AuthenticationManagerResolver { ProviderManager(JwtAuthenticationProvider(jwtDecoder(issuerUri, scope))) }

    private companion object {
        private fun jwtDecoder(issuerUri: String, scope: String): JwtDecoder {
            val decoder = JwtDecoders.fromIssuerLocation(issuerUri) as NimbusJwtDecoder
            val issuerValidator: OAuth2TokenValidator<Jwt> = JwtValidators.createDefaultWithIssuer(issuerUri)
            decoder.setJwtValidator(DelegatingOAuth2TokenValidator(issuerValidator, TokenScopeValidator(scope)))
            return decoder
        }
    }
}
