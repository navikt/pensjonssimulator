package no.nav.pensjon.simulator.core.domain.regler.to

import java.util.*

class HentGrunnbelopListeRequest : ServiceRequest() {
    var fom: Date? = null
    var tom: Date? = null
}
