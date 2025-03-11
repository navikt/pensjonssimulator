package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.JusteringsTypeEnum

// 2025-03-10
class JusteringsInformasjon {
    var totalJusteringsfaktor: Double = 0.0
    var justeringsTypeEnum: JusteringsTypeEnum? = null
    var elementer: MutableList<IJustering> = mutableListOf()

    constructor()
}
