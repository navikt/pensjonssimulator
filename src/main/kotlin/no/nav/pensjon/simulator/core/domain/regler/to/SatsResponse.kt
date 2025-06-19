package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.SatsResultat
import java.util.Vector

// 2025-06-13
class SatsResponse : ServiceResponse() {
    var satsResultater: List<SatsResultat> = Vector()
}
