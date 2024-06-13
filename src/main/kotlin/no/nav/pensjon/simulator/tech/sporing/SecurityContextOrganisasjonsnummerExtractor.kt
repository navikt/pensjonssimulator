package no.nav.pensjon.simulator.tech.sporing

import no.nav.pensjon.simulator.tech.sporing.context.SecurityContextClaimExtractor
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Component

@Component
class SecurityContextOrganisasjonsnummerExtractor : OrganisasjonsnummerProvider {

    override fun provideOrganisasjonsnummer(): Organisasjonsnummer =
        organisasjonsnummerFromSecurityContext()?.let(::Organisasjonsnummer)
            ?: throw RuntimeException("Organisasjonsnummer not found")

    private companion object {
        private const val CLAIM_KEY = "consumer"
        private const val CONSUMER_AUTHORITY = "iso6523-actorid-upis"
        private const val ICD_CODE = "0192" // https://docs.peppol.eu/poacc/billing/3.0/codelist/ICD/

        private fun organisasjonsnummerFromSecurityContext(): String? {
            return try {
                SecurityContextClaimExtractor.claimAsMap(CLAIM_KEY)?.let {
                    with(it["authority"]) {
                        requireNotNull(this) { "Claim is missing `authority`" }
                        require(this is String) { "Unknown authority type `${this::class.java}`" }
                        require(this == CONSUMER_AUTHORITY) { "Unknown authority type=`${this}`" }
                    }

                    with(it["ID"]) {
                        requireNotNull(this) { "Claim is missing `ID`" }
                        require(this is String) { "Unknown ID type `${this::class.java}`" }
                        require(this.startsWith("$ICD_CODE:")) { "Unknown ICD code in ID `$this`" }
                        this.substringAfter("$ICD_CODE:")
                    }
                }
            } catch (e: RuntimeException) {
                throw object : AuthenticationException(e.message, e) {}
            }
        }
    }
}
