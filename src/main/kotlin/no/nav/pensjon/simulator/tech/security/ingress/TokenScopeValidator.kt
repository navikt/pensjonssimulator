package no.nav.pensjon.simulator.tech.security.ingress

import mu.KotlinLogging
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success
import org.springframework.security.oauth2.jwt.Jwt

class TokenScopeValidator(val scope: String) : OAuth2TokenValidator<Jwt> {

    private val log = KotlinLogging.logger {}

    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult = validate(jwt.getClaimAsString(CLAIM_NAME))

    private fun validate(tokenScope: String): OAuth2TokenValidatorResult =
        if (tokenScope == scope)
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
