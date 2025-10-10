package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.alder.Alder

data class HentDelingstallRequest(val arskull: Int, val alder: List<Alder>)