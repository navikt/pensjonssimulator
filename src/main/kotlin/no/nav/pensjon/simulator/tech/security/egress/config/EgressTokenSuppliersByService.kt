package no.nav.pensjon.simulator.tech.security.egress.config

import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import java.util.function.Supplier

/**
 * Egress access tokens are used for authentication when calling external services.
 * This class holds a collection of suppliers of such access tokens.
 * Each supplier is mapped to a service (the service for which the token is to be used).
 * By holding suppliers instead of actual tokens, the tokens are obtained "lazily" (when needed).
 */
data class EgressTokenSuppliersByService(val value: Map<EgressService, Supplier<RawJwt>>)
