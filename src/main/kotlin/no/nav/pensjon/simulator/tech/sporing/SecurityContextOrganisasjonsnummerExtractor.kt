package no.nav.pensjon.simulator.tech.sporing

import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.tech.security.ingress.claim.ConsumerClaimUtil
import no.nav.pensjon.simulator.tech.sporing.context.SecurityContextClaimExtractor
import org.springframework.stereotype.Component

@Component
class SecurityContextOrganisasjonsnummerExtractor : OrganisasjonsnummerProvider {

    override fun provideOrganisasjonsnummer(): Organisasjonsnummer =
        organisasjonsnummerFromSecurityContext()?.let(::Organisasjonsnummer)
            ?: throw RuntimeException("Organisasjonsnummer not found")

    private companion object {
        private const val CLAIM_KEY = "consumer"

        private fun organisasjonsnummerFromSecurityContext(): String? =
            SecurityContextClaimExtractor.claimAsMap(CLAIM_KEY)?.let(ConsumerClaimUtil::organisasjonsnummer)
    }
}
