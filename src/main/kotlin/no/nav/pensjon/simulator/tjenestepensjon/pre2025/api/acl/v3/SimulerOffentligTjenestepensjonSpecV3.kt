package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SimulerOffentligTjenestepensjonSpecV3(
    val simuleringEtter2011: SimuleringEtter2011SpecV3
)

