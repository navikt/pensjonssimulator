package no.nav.pensjon.simulator.tech.security.ingress.claim

import org.springframework.security.core.AuthenticationException

/**
 * Utility for extracting and validating token claims pertaining to consumer identification.
 * The identification format is assumed to be in accordance with ISO/IEC 6523.
 * See e.g. en.wikipedia.org/wiki/ISO/IEC_6523 for background information.
 */
object ConsumerClaimUtil {
    private const val CONSUMER_AUTHORITY_KEY = "authority"
    private const val CONSUMER_ID_KEY = "ID"

    /**
     * ISO/IEC 6523 identifier scheme.
     * The term "upis" presumably originates from "universal participant identifier scheme".
     */
    private const val CONSUMER_AUTHORITY = "iso6523-actorid-upis"

    /**
     * ICD = International Code Designator.
     * "0192" implies "Organisasjonsnummer" (to 'identify entities registered in the Central Coordinating Register
     * for Legal Entities in Norway', ref. docs.peppol.eu/poacc/billing/3.0/codelist/ICD)
     */
    private const val ICD_CODE = "0192" //

    fun organisasjonsnummer(consumerClaim: Map<String, Any>): String =
        try {
            validateAuthority(consumerClaim)
            consumerIdWithoutPrefix(consumerClaim)
        } catch (e: RuntimeException) {
            throw object : AuthenticationException(e.message, e) {}
        }

    private fun consumerIdWithoutPrefix(claim: Map<String, Any>): String =
        with(claim[CONSUMER_ID_KEY]) {
            requireNotNull(this) { "Claim is missing '$CONSUMER_ID_KEY' key" }
            require(this is String) { "Unexpected $CONSUMER_ID_KEY value type '${this::class.java}'" }
            require(this.startsWith("$ICD_CODE:")) { "Unknown ICD code in $CONSUMER_ID_KEY '$this'" }
            this.substringAfter("$ICD_CODE:")
        }

    private fun validateAuthority(claim: Map<String, Any>) {
        with(claim[CONSUMER_AUTHORITY_KEY]) {
            requireNotNull(this) { "Claim is missing '$CONSUMER_AUTHORITY_KEY' key" }
            require(this is String) { "Unexpected $CONSUMER_AUTHORITY_KEY value type: '${this::class.java}'" }
            require(this == CONSUMER_AUTHORITY) { "Unknown $CONSUMER_AUTHORITY_KEY: '$this'" }
        }
    }
}
