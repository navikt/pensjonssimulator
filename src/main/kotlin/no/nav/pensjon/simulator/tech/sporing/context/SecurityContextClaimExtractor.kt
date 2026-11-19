package no.nav.pensjon.simulator.tech.sporing.context

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

object SecurityContextClaimExtractor {

    fun claim(key: String): Any? = jwt()?.claims?.get(key)

    fun claimAsMap(key: String): MutableMap<String, Any>? = jwt()?.getClaimAsMap(key)

    private fun jwt(): Jwt? = SecurityContextHolder.getContext().authentication?.credentials as? Jwt
}