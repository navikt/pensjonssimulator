package no.nav.pensjon.client

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import no.nav.pensjon.MaskinportenConfig
import no.nav.pensjon.loadMaskinportenConfig
import java.util.*

object MaskinportenToken {
    private const val REQUEST_TOKEN_TO_EXPIRE_AFTER_SECONDS: Int = 30
    private val maskinportenConfig: MaskinportenConfig = loadMaskinportenConfig()
    val log = KotlinLogging.logger {}

    suspend fun hentToken(): String {
        log.info("Bruker følgende conf: $maskinportenConfig")

        val rsaKey = RSAKey.parse(maskinportenConfig.clientJwk)
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKey.keyID)
                .type(JOSEObjectType.JWT)
                .build(),
            JWTClaimsSet.Builder()
                .audience(maskinportenConfig.tokenEndpointUrl)
                .issuer(maskinportenConfig.clientId)
                .claim("scope", maskinportenConfig.scope)
                .issueTime(Date())
                .expirationTime(getExpireAfter())
                .build()
        )
        signedJWT.sign(RSASSASigner(rsaKey.toRSAPrivateKey()))

        val httpResponse = ClientProvider.client.post(maskinportenConfig.tokenEndpointUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            accept(ContentType.Companion.Any)
            setBody(
                Parameters.Companion.build {
                    append("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                    append("assertion", signedJWT.serialize())
                }.formUrlEncode()
            )
        }

        if (!httpResponse.status.isSuccess()) {
            val errorText = httpResponse.bodyAsText()
            log.warn("Error from Maskinporten: ${httpResponse.status} - $errorText")
            throw RuntimeException("Failed to get token: HTTP ${httpResponse.status}")
        }

        val response: MaskinportenTokenResponse = httpResponse.body()
        log.info("Hentet token fra maskinporten med scope(s): ${maskinportenConfig.scope}")
        return response.access_token
    }

    fun getExpireAfter(): Date {
        val calendar = Calendar.getInstance()
        calendar.time = Date();
        calendar.add(Calendar.SECOND, REQUEST_TOKEN_TO_EXPIRE_AFTER_SECONDS)
        return calendar.time
    }

    @Serializable
    data class MaskinportenTokenResponse(
        val access_token: String,
        val token_type: String,
        val expires_in: Int,
        val scope: String,
    )
}