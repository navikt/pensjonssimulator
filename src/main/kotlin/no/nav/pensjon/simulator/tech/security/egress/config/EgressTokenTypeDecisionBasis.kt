package no.nav.pensjon.simulator.tech.security.egress.config

import no.nav.pensjon.simulator.tech.representasjon.RepresentertRolle

/**
 * Informasjon som bestemmer hvilken type av token (JWT) som skal brukes for egress-kall (kall mot andre apper).
 */
data class EgressTokenTypeDecisionBasis(
    val rolle: RepresentertRolle,
    val ingressToken: String? = null
)