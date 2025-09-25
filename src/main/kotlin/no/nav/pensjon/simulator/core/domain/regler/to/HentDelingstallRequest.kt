package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.alder.Alder

class HentDelingstallRequest(var aarskull: Int? = null, var alder: List<Alder> = mutableListOf()) : ServiceRequest()