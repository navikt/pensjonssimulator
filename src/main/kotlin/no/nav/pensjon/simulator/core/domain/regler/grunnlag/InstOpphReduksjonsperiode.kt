package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.JustertPeriodeCti
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import java.io.Serializable
import java.util.*

/**
 * Objektet inneholder informasjon om perioder der person har institusjonsopphold som kan medføre reduksjon av pensjon.
 */
class InstOpphReduksjonsperiode(
    /**
     * Unik identifikasjon av objektet.
     */
    var instOpphReduksjonsperiodeId: Long? = null,
    /**
     * Fra og med dato
     */
    var fom: Date? = null,
    /**
     * Til og med dato
     */
    var tom: Date? = null,
    /**
     * Angir om reduksjon er grunnet varighet.
     */
    var reduksjonGrunnetVarighet: Boolean = false,
    /**
     * Angir om institusjonsoppholdsperioden medfører en økning eller reduksjon av pensjonsytelsen.
     */
    var justertPeriodeType: JustertPeriodeCti? = null,

    /**
     * Angir om bruker har forsørgeransvar ved institusjonsopphold
     */
    var forsorgeransvar: Boolean = false
) : Comparable<InstOpphReduksjonsperiode>, Serializable {

    constructor(instOpphReduksjonsperiode: InstOpphReduksjonsperiode) : this() {
        this.instOpphReduksjonsperiodeId = instOpphReduksjonsperiode.instOpphReduksjonsperiodeId
        if (instOpphReduksjonsperiode.fom != null) {
            this.fom = instOpphReduksjonsperiode.fom!!.clone() as Date
        }
        if (instOpphReduksjonsperiode.tom != null) {
            this.tom = instOpphReduksjonsperiode.tom!!.clone() as Date
        }
        this.reduksjonGrunnetVarighet = instOpphReduksjonsperiode.reduksjonGrunnetVarighet
        if (instOpphReduksjonsperiode.justertPeriodeType != null) {
            this.justertPeriodeType = JustertPeriodeCti(instOpphReduksjonsperiode.justertPeriodeType)
        }
        this.forsorgeransvar = instOpphReduksjonsperiode.forsorgeransvar
    }

    constructor(
        instOpphReduksjonsperiodeId: Long,
        fom: Date,
        tom: Date,
        reduksjonGrunnetVarighet: Boolean,
        justertPeriodeType: JustertPeriodeCti
    ) : this() {
        this.instOpphReduksjonsperiodeId = instOpphReduksjonsperiodeId
        this.fom = fom
        this.tom = tom
        this.reduksjonGrunnetVarighet = reduksjonGrunnetVarighet
        this.justertPeriodeType = justertPeriodeType
    }

    override fun compareTo(other: InstOpphReduksjonsperiode): Int {
        return DateCompareUtil.compareTo(fom, other.fom)
    }

    override fun toString(): String {
        val TAB = "    "

        var retValue =
            ("InstOpphReduksjonsperiode ( " + super.toString() + TAB + "instOpphReduksjonsperiodeId = " + instOpphReduksjonsperiodeId + TAB + "fom = " + fom + TAB + "tom = "
                    + tom + TAB + "reduksjonGrunnetVarighet = " + reduksjonGrunnetVarighet + TAB)
        if (justertPeriodeType != null) {
            retValue += "justertPeriodeType = " + justertPeriodeType!!.kode + TAB
        }
        retValue += " )"

        return retValue
    }
}
