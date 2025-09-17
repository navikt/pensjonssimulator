package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class SamlToken(
        @param:JsonProperty("access_token")
        val accessToken: String,
        @param:JsonProperty("expires_in")
        val expiresIn: Long,
        @param:JsonProperty("token_type")
        val tokenType: String? = null,
        @param:JsonProperty("issued_token_type")
        val issuedTokenType: String? = null
) {

    private val issuedAt = LocalDateTime.now()

    val isExpired: Boolean
        get() = LocalDateTime.now().isAfter(issuedAt.plusSeconds(expiresIn))

    override fun toString(): String {
        return "SamlToken(accessToken='$accessToken', expiresIn=$expiresIn, tokenType=$tokenType, issuedTokenType=$issuedTokenType, issuedAt=$issuedAt, isExpired=$isExpired)"
    }


}