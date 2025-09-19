package no.nav.pensjon.simulator.tech.security.egress.oauth2.maskinporten

import com.nimbusds.jose.jwk.RSAKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MaskinportenCredentials(
    @param:Value("\${maskinporten.client-id}") val clientId: String,
    @param:Value("\${maskinporten.client-jwk}") private val clientJwk: String,
    @param:Value("\${maskinporten.issuer}") val issuer: String,
){
    private val rsaKey = RSAKey.parse(clientJwk)
    val keyId = rsaKey.keyID
    val privateKey = rsaKey.toRSAPrivateKey()
}