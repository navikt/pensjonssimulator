package no.nav.pensjon.simulator.tech.security.ingress.scope

import mu.KotlinLogging
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success
import org.springframework.security.oauth2.jwt.Jwt

class JwtScopeValidator(val scopes: List<String>) : OAuth2TokenValidator<Jwt> {

    private val log = KotlinLogging.logger {}

    override fun validate(token: Jwt): OAuth2TokenValidatorResult =
        validate(tokenScope = token.getClaimAsString(CLAIM_NAME) ?: "")

    private fun validate(tokenScope: String): OAuth2TokenValidatorResult =
        if (scopes.contains(tokenScope))
            success()
        else
            "Invalid $CLAIM_NAME: $tokenScope".let {
                log.warn { it }
                failure(OAuth2Error(it))
            }

    private companion object {
        private const val CLAIM_NAME = "scope"
    }
}
