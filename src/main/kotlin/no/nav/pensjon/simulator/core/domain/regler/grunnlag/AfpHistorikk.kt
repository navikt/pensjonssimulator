package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import no.nav.pensjon.simulator.core.domain.regler.kode.AfpOrdningTypeCti
import java.io.Serializable
import java.util.*

class AfpHistorikk(
    /**
     * Fremtidig pensjonspoeng
     */
    var afpFpp: Double = 0.0,

    var virkFom: Date? = null,

    var virkTom: Date? = null,

    var afpPensjonsgrad: Int = 0,

    var afpOrdning: AfpOrdningTypeCti? = null
) : Comparable<AfpHistorikk>, Serializable {

    constructor(afpHistorikk: AfpHistorikk) : this() {
        this.afpFpp = afpHistorikk.afpFpp
        if (afpHistorikk.virkFom != null) {
            this.virkFom = afpHistorikk.virkFom!!.clone() as Date
        }
        if (afpHistorikk.virkTom != null) {
            this.virkTom = afpHistorikk.virkTom!!.clone() as Date
        }
        this.afpPensjonsgrad = afpHistorikk.afpPensjonsgrad
        if (afpHistorikk.afpOrdning != null) {
            this.afpOrdning = AfpOrdningTypeCti(afpHistorikk.afpOrdning)
        }
    }

    override fun compareTo(other: AfpHistorikk): Int {
        return DateCompareUtil.compareTo(virkFom, other.virkFom)
    }
}
