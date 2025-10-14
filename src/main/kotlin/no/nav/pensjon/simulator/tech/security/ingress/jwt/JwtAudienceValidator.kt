package no.nav.pensjon.simulator.tech.security.ingress.jwt

import mu.KotlinLogging
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class JwtAudienceValidator(val audience: String) : OAuth2TokenValidator<Jwt> {

    private val log = KotlinLogging.logger {}

    override fun validate(token: Jwt): OAuth2TokenValidatorResult =
        validate(token.audience.orEmpty())

    private fun validate(audiences: List<String>): OAuth2TokenValidatorResult =
        if (audiences.contains(audience))
            OAuth2TokenValidatorResult.success()
        else
            "Invalid audience claim: ${audiences.joinToString()}".let {
                log.warn { it }
                OAuth2TokenValidatorResult.failure(OAuth2Error(it))
            }
}