package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.JustertPeriodeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.JustertPeriodeCti
import java.util.*

/**
 * Objektet inneholder informasjon om perioder der person har institusjonsopphold som kan medføre reduksjon av pensjon.
 */
// Checked 2025-02-28
class InstOpphReduksjonsperiode {
    /**
     * Unik identifikasjon av objektet.
     */
    var instOpphReduksjonsperiodeId: Long = 0

    /**
     * Fra og med dato
     */
    var fom: Date? = null

    /**
     * Til og med dato
     */
    var tom: Date? = null

    /**
     * Angir om reduksjon er grunnet varighet.
     */
    var reduksjonGrunnetVarighet = false

    /**
     * Angir om institusjonsoppholdsperioden medfører en økning eller reduksjon av pensjonsytelsen.
     */
    var justertPeriodeType: JustertPeriodeCti? = null
    var justertPeriodeTypeEnum: JustertPeriodeEnum? = null

    /**
     * Angir om bruker har forsørgeransvar ved institusjonsopphold
     */
    var forsorgeransvar = false

    constructor()

    constructor(source: InstOpphReduksjonsperiode) : this() {
        instOpphReduksjonsperiodeId = source.instOpphReduksjonsperiodeId
        fom = source.fom?.clone() as? Date
        tom = source.tom?.clone() as? Date
        reduksjonGrunnetVarighet = source.reduksjonGrunnetVarighet
        justertPeriodeType =source.justertPeriodeType?.let(::JustertPeriodeCti)
        justertPeriodeTypeEnum =source.justertPeriodeTypeEnum
        forsorgeransvar = source.forsorgeransvar
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
