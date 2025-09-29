package no.nav.pensjon.simulator.core.domain.regler.to

import Delingstall

data class HentDelingstallResponse(
    val arskull: Int? = null,
    val delingstall: List<Delingstall> = emptyList()
)