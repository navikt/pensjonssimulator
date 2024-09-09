package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import java.util.*

class SatsResponse(
    var satsResultater: MutableList<SatsResultat> = Vector()
) : ServiceResponse() {
    override fun virkFom(): Date? {
        return satsResultater.lastOrNull()?.fom
    }

    override fun persons(): String {
        return ""
    }
}
