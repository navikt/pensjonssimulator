package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Delingstall

data class HentDelingstallResponse(val arskull:Int, val delingstall: List<Delingstall>)
