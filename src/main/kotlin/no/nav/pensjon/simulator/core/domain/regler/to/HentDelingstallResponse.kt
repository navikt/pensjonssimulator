package no.nav.pensjon.simulator.core.domain.regler.to

import Delingstall

data class HentDelingstallResponse(
    val arskull: Int,
    val delingstall: List<Delingstall>
)