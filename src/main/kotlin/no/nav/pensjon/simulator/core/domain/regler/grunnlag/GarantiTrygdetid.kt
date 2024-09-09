package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import java.util.*

class GarantiTrygdetid(
    var trygdetid_garanti: Int = 0,
    var fomDato: Date? = null,
    var tomDato: Date? = null
) : Comparable<GarantiTrygdetid> {

    constructor(garantiTrygdetid: GarantiTrygdetid) : this() {
        fomDato = garantiTrygdetid.fomDato
        tomDato = garantiTrygdetid.tomDato
        trygdetid_garanti = garantiTrygdetid.trygdetid_garanti
    }

    override fun compareTo(other: GarantiTrygdetid): Int {
        return DateCompareUtil.compareTo(fomDato, other.fomDato)
    }
}
