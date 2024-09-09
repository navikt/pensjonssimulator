package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable
import java.util.*

class Uforehistorikk : Serializable {
    /**
     * Liste av uføreperioder.
     */
    var uforeperiodeListe: MutableList<Uforeperiode> = mutableListOf()

    /**
     * Uføregraden pensjonen er blitt fryst fra.
     */
    var garantigrad: Int = 0

    /**
     * Yrkesskadegraden pensjonen er blitt fryst fra.
     */
    var garantigradYrke: Int = 0

    /**
     * Dato for sist innmeldt i Folketrygden- for fremtidig trygdetid.
     * Lagt inn ifm PENPORT-2222
     */
    var sistMedlITrygden: Date? = null

    /**
     * Ved eksport og ung uføre vil denne listen inneholde uførehistorikk med ung uføre.
     * Intern PREG variabel
     */
    @JsonIgnore
    var originalUforeperiodeListe: MutableList<Uforeperiode> = mutableListOf()

    /**
     * For å hindre at gammel ung uføre utløses som en funksjon av alder og uføretidspunkt må det et eksplisitt flagg til for å hindre dette.
     */
    @JsonIgnore
    var eksportforbudUngUfor: Boolean = false

    constructor()

    constructor(uforehistorikk: Uforehistorikk) : this() {
        for (uforeperiode in uforehistorikk.uforeperiodeListe) {
            uforeperiodeListe.add(Uforeperiode(uforeperiode))
        }
        this.garantigrad = uforehistorikk.garantigrad
        this.garantigradYrke = uforehistorikk.garantigradYrke
        this.sistMedlITrygden = uforehistorikk.sistMedlITrygden
        for (uforeperiode in uforehistorikk.originalUforeperiodeListe) {
            this.originalUforeperiodeListe.add(Uforeperiode(uforeperiode))
        }
    }

    constructor(
        uforeperiodeListe: MutableList<Uforeperiode> = mutableListOf(),
        garantigrad: Int = 0,
        garantigradYrke: Int = 0,
        sistMedlITrygden: Date? = null,
        originalUforeperiodeListe: MutableList<Uforeperiode> = mutableListOf()
    ) {
        this.uforeperiodeListe = uforeperiodeListe
        this.garantigrad = garantigrad
        this.garantigradYrke = garantigradYrke
        this.sistMedlITrygden = sistMedlITrygden
        this.originalUforeperiodeListe = originalUforeperiodeListe
    }

    fun getSortedUforeperiodeListe(reverse: Boolean): MutableList<Uforeperiode> {
        return run {
            val sortedOg = mutableListOf(*uforeperiodeListe.toTypedArray())
            if (reverse) {
                Collections.sort(sortedOg, Collections.reverseOrder())
            } else {
                sortedOg.sort()
            }
            sortedOg
        }
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
