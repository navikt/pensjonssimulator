package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import java.io.Serializable
import java.util.*

/**
 * Objektet inneholder informasjon om den månedlige faste utgiften en bruker har hatt i forbindelse med
 * opphold på en institusjon. Det inneholder også tidsrommet brukeren var innlagt.
 */
class InstOpphFasteUtgifterperiode(
        //	instOpphFasteUtgifterperiodeId	long	Unik identifikasjon av objektet.
        //	fomDato	Date	Dato bruker ble innlagt
        //	tomDato	Date	Dato bruker ble skrevet ut.
        //	fasteUtgifter	int	Månedlig fast utgift bruker hadde på

        /**
         * Unik identifikasjon av objektet
         */
        var instOpphFasteUtgifterperiodeId: Long? = null,
        /**
         * Dato bruker ble innlagt
         */
        var fom: Date? = null,
        /**
         * Dato bruker ble skrevet ut
         */
        var tom: Date? = null,
        /**
         * Månedlig fast utgift bruker hadde på institusjonen
         */
        var fasteUtgifter: Int = 0
) : Comparable<InstOpphFasteUtgifterperiode>, Serializable {

    constructor(instOpphFasteUtgifterperiode: InstOpphFasteUtgifterperiode) : this() {
        this.instOpphFasteUtgifterperiodeId = instOpphFasteUtgifterperiode.instOpphFasteUtgifterperiodeId
        if (instOpphFasteUtgifterperiode.fom != null) {
            this.fom = instOpphFasteUtgifterperiode.fom!!.clone() as Date
        }
        if (instOpphFasteUtgifterperiode.tom != null) {
            this.tom = instOpphFasteUtgifterperiode.tom!!.clone() as Date
        }
        this.fasteUtgifter = instOpphFasteUtgifterperiode.fasteUtgifter
    }

    constructor(instOpphFasteUtgifterperiodeId: Long, fom: Date, tom: Date, fasteUtgifter: Int) : this() {
        this.instOpphFasteUtgifterperiodeId = instOpphFasteUtgifterperiodeId
        this.fom = fom
        this.tom = tom
        this.fasteUtgifter = fasteUtgifter
    }

    override fun compareTo(other: InstOpphFasteUtgifterperiode): Int {
        return DateCompareUtil.compareTo(fom, other.fom)
    }

    override fun toString(): String {
        val TAB = "    "
        return ("InstOpphFasteUtgifterperiode ( " + super.toString() + TAB + "instOpphFasteUtgifterperiodeId = " + instOpphFasteUtgifterperiodeId + TAB + "fom = " + fom + TAB
                + "tom = " + tom + TAB + "fasteUtgifter = " + fasteUtgifter + TAB + " )")
    }
}
