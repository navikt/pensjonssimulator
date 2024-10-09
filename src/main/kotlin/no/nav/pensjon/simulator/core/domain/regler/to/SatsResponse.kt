package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import java.util.Vector

class SatsResponse : ServiceResponse() {

    var satsResultater: List<SatsResultat> = Vector()
}
