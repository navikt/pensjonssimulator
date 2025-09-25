package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.pensjon.simulator.alder.Alder

@JsonIgnoreProperties(ignoreUnknown = true)
data class Delingstall(
    var alder: Alder,
    var delingstall: Double,
)
