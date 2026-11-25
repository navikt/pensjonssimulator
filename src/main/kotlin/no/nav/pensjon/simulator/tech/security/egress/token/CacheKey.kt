package no.nav.pensjon.simulator.tech.security.egress.token

data class CacheKey(
    val scope: String,
    val pid: String
)