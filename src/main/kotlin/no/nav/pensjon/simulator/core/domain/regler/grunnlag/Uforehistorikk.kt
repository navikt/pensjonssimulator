package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.util.*

// Checked 2025-02-28
class Uforehistorikk {
    /**
     * Liste av Uføreperioder.
     */
    var uforeperiodeListe: MutableList<Uforeperiode> = mutableListOf()

    /**
     * Uføregraden pensjonen er blitt fryst fra.
     */
    var garantigrad = 0

    /**
     * Yrkesskadegraden pensjonen er blitt fryst fra.
     */
    var garantigradYrke = 0

    /**
     * Dato for sist innmeldt i Folketrygden- for fremtidig trygdetid.
     * Lagt inn ifm PENPORT-2222
     */
    var sistMedlITrygden: Date? = null

    constructor()

    constructor(source: Uforehistorikk) : this() {
        source.uforeperiodeListe.forEach { uforeperiodeListe.add(Uforeperiode(it)) }
        garantigrad = source.garantigrad
        garantigradYrke = source.garantigradYrke
        sistMedlITrygden = source.sistMedlITrygden?.clone() as? Date
    }

    /**
     * Constructs a `String` with all attributes
     * in name = value format.
     */
    override fun toString(): String {
        val TAB = "    "
        val retValue = StringBuilder()
        retValue.append("Uforehistorikk ( ").append(super.toString()).append(TAB).append("uforeperiodeListe = ")
            .append(uforeperiodeListe).append(TAB).append(" )")
        return retValue.toString()
    }

    //SIMDOM-ADD:
    fun realUforePeriodeList(): List<Uforeperiode> = uforeperiodeListe.filter { it.isRealUforeperiode() }

    fun containsActualUforeperiode(): Boolean = uforeperiodeListe.any { it.isRealUforeperiode() }
    // end SIMDOM-ADD
}
