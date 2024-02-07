package no.nav.pensjon.simulator.tech.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfiguration {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.GET,
                        "/internal/**",
                        "/api/v1/status",
                        "/error").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
            .build()
}
