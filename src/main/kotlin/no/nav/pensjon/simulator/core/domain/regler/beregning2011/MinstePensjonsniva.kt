package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.MinstePensjonsnivaSatsEnum
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

// 2025-03-10
class MinstePensjonsniva {
    var belop = 0.0
    var sats = 0.0
    var benyttetYug = 0
    var pro_rata_teller_mnd = 0
    var pro_rata_nevner_mnd = 0
    var pro_rata_brok = 0.0
    var formelKodeEnum: FormelKodeEnum = FormelKodeEnum.MPNx

    /**
     * Minstepensjonsnivå. Kan være lav, ordinær og forhøyet. Benytter tabellen
     */
    var satsTypeEnum: MinstePensjonsnivaSatsEnum? = null
    var merknadListe: MutableList<Merknad> = mutableListOf()
    var faktisk_tt_avtaleland_mnd = 0

    constructor()

    constructor(source: MinstePensjonsniva) : super() {
        belop = source.belop
        sats = source.sats
        benyttetYug = source.benyttetYug
        pro_rata_teller_mnd = source.pro_rata_teller_mnd
        pro_rata_nevner_mnd = source.pro_rata_nevner_mnd
        pro_rata_brok = source.pro_rata_brok
        satsTypeEnum = source.satsTypeEnum
        formelKodeEnum = source.formelKodeEnum
        merknadListe = source.merknadListe.map { it.copy() }.toMutableList()
        faktisk_tt_avtaleland_mnd = source.faktisk_tt_avtaleland_mnd
    }
}
