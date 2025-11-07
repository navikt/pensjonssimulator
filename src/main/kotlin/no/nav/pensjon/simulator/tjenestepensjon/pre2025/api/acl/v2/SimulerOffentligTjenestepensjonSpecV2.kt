package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SimulerOffentligTjenestepensjonSpecV2(
    val simuleringEtter2011: SimuleringEtter2011SpecV2
)

