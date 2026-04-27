package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.time.LocalDate

// 2026-04-23
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
    var sistMedlITrygdenLd: LocalDate? = null

    constructor()

    constructor(source: Uforehistorikk) : this() {
        source.uforeperiodeListe.forEach { uforeperiodeListe.add(Uforeperiode(it)) }
        garantigrad = source.garantigrad
        garantigradYrke = source.garantigradYrke
        sistMedlITrygdenLd = source.sistMedlITrygdenLd
    }

    // Extra:
    //fun realUforePeriodeList(): List<Uforeperiode> = uforeperiodeListe.filter { it.isRealUforeperiode() }

    fun containsActualUforeperiode(): Boolean = uforeperiodeListe.any { it.isRealUforeperiode() }
    // end extra
}
