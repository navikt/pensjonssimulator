package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.util.*

class UforetrygdEtteroppgjor(

    /**
     * Angir om det har vært arbeidsforsøk i etteroppgjørsåret.
     */
    var arbeidsforsok: Boolean = false,

    /**
     * Angir start av arbeidsforsøk.
     */
    var arbeidsforsokFom: Date? = null,

    /**
     * Angir slutt av arbeidsforsøk.
     */
    var arbeidsforsokTom: Date? = null,

    var detaljer: MutableList<UforetrygdEtteroppgjorDetalj> = mutableListOf(),

    /**
     * Angir start av uføretrygd i etteroppgjørsåret.
     */
    var periodeFom: Date? = null,

    /**
     * Angir slutt av uføretrygd i etteroppgjørsåret.
     */
    var periodeTom: Date? = null
) {

    constructor(uforetrygdEtteroppgjor: UforetrygdEtteroppgjor) : this() {
        this.arbeidsforsok = uforetrygdEtteroppgjor.arbeidsforsok
        this.detaljer = uforetrygdEtteroppgjor.detaljer.map { UforetrygdEtteroppgjorDetalj(it) }.toMutableList()

        if (uforetrygdEtteroppgjor.periodeFom != null) {
            this.periodeFom = Date(uforetrygdEtteroppgjor.periodeFom!!.time)
        }
        if (uforetrygdEtteroppgjor.periodeTom != null) {
            this.periodeTom = Date(uforetrygdEtteroppgjor.periodeTom!!.time)
        }
        if (uforetrygdEtteroppgjor.arbeidsforsokFom != null) {
            this.arbeidsforsokFom = Date(uforetrygdEtteroppgjor.arbeidsforsokFom!!.time)
        }
        if (uforetrygdEtteroppgjor.arbeidsforsokTom != null) {
            this.arbeidsforsokTom = Date(uforetrygdEtteroppgjor.arbeidsforsokTom!!.time)
        }
    }

    fun getUforetrygdEtteroppgjorDetalj(grunnlagsrolle: String): UforetrygdEtteroppgjorDetalj? {
        for (ued in detaljer) {
            if (ued.grunnlagsrolle!!.kode == grunnlagsrolle) {
                return ued
            }
        }
        return null
    }
}
