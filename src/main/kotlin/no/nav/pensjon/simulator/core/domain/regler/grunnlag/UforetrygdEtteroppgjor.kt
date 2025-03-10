package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.util.*

// 2025-03-10
class UforetrygdEtteroppgjor {
    /**
     * Angir om det har vært arbeidsforsøk i etteroppgjørsåret.
     */
    var arbeidsforsok = false

    /**
     * Angir start av arbeidsforsøk.
     */
    var arbeidsforsokFom: Date? = null

    /**
     * Angir slutt av arbeidsforsøk.
     */
    var arbeidsforsokTom: Date? = null

    var detaljer: List<UforetrygdEtteroppgjorDetalj> = mutableListOf()

    /**
     * Angir start av uføretrygd i etteroppgjørsåret.
     */
    var periodeFom: Date? = null

    /**
     * Angir slutt av uføretrygd i etteroppgjørsåret.
     */
    var periodeTom: Date? = null

    constructor()

    constructor(source: UforetrygdEtteroppgjor) : this() {
        arbeidsforsok = source.arbeidsforsok
        arbeidsforsokFom = source.arbeidsforsokFom?.time?.let(::Date)
        arbeidsforsokTom = source.arbeidsforsokTom?.time?.let(::Date)
        detaljer = source.detaljer.map(::UforetrygdEtteroppgjorDetalj)
        periodeFom = source.periodeFom?.time?.let(::Date)
        periodeTom = source.periodeTom?.time?.let(::Date)
    }
}
