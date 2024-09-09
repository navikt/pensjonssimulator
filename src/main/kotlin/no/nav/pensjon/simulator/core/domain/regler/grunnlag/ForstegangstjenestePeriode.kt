package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.ForstegangstjenesteperiodeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import java.io.Serializable
import java.util.*

class ForstegangstjenestePeriode : Comparable<ForstegangstjenestePeriode>, Serializable {

    var fomDato: Date? = null
    var tomDato: Date? = null
    var periodeType: ForstegangstjenesteperiodeTypeCti? = null

    constructor(p: ForstegangstjenestePeriode) {
        if (p.fomDato != null) {
            this.fomDato = p.fomDato!!.clone() as Date
        }
        if (p.tomDato != null) {
            this.tomDato = p.tomDato!!.clone() as Date
        }
        if (p.periodeType != null) {
            this.periodeType = ForstegangstjenesteperiodeTypeCti(p.periodeType)
        }
    }

    constructor(fomDato: Date? = null, tomDato: Date? = null, periodeType: ForstegangstjenesteperiodeTypeCti? = null) {
        this.fomDato = fomDato
        this.tomDato = tomDato
        this.periodeType = periodeType
    }

    constructor()

    override fun compareTo(other: ForstegangstjenestePeriode): Int {
        return DateCompareUtil.compareTo(fomDato, other.fomDato)
    }

    //SIMDOM-ADD
    fun ar(): Int? = fomDato?.let(::getYear)
}
