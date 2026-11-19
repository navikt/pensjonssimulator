package no.nav.pensjon.simulator.tech.security.egress.token

import no.nav.pensjon.simulator.person.Pid

data class CacheKey(
    val scope: String,
    val pid: Pid
)