package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.AvtalelandCti
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import java.io.Serializable
import java.util.*

/**
 * Klassen beskriver et poengår opptjent i utlandet. Settes av saksbehandler.
 */
class PoengarManuell(
    /**
     * Poengåret fra og med dato.
     */
    var fom: Date? = null,

    /**
     * Poengåret til og med dato.
     */
    var tom: Date? = null,

    /**
     * Angir om poengåret skal brukes i pro rata beregning.
     */
    var ikkeProrata: Boolean = false,

    /**
     * Angir om poengåret skal brukes i alternativ pro rata beregning.
     */
    var ikkeAlternativProrata: Boolean = false,

    /**
     * Avtaleland som poengår ble opptjent i.
     */
    var avtaleland: AvtalelandCti? = null
) : Comparable<PoengarManuell>, Serializable {

    constructor(poengarManuell: PoengarManuell) : this() {
        if (poengarManuell.fom != null) {
            fom = poengarManuell.fom!!.clone() as Date
        }
        if (poengarManuell.tom != null) {
            tom = poengarManuell.tom!!.clone() as Date
        }
        this.ikkeProrata = poengarManuell.ikkeProrata
        this.ikkeAlternativProrata = poengarManuell.ikkeAlternativProrata
        avtaleland = poengarManuell.avtaleland
    }

    constructor(fom: Date, tom: Date, ikkeProrata: Boolean, ikkeAlternativProrata: Boolean) : this() {
        this.fom = fom
        this.tom = tom
        this.ikkeProrata = ikkeProrata
        this.ikkeAlternativProrata = ikkeAlternativProrata
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    override fun compareTo(other: PoengarManuell): Int {
        return DateCompareUtil.compareTo(fom, other.fom)
    }
}
