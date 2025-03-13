package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.UtfallEnum
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

// 2025-03-10
class Minsteytelse {
    var formelKodeEnum: FormelKodeEnum? = null
    var merknadListe: List<Merknad> = mutableListOf()
    var satsMinsteytelse: SatsMinsteytelse? = null
    var arsbelop = 0
    var eksportforbud = false

    /**
     * Trygdetid som er brukt ved beregning av minsteytelsen.
     */
    var anvendtTrygdetid: AnvendtTrygdetid? = null
    var anvendtFlyktningEnum: UtfallEnum? = null

    constructor() : super() {
        merknadListe = mutableListOf()
    }

    constructor(source: Minsteytelse) : super() {
        formelKodeEnum = source.formelKodeEnum
        merknadListe = source.merknadListe.map { it.copy() }
        satsMinsteytelse = source.satsMinsteytelse?.let(::SatsMinsteytelse)
        arsbelop = source.arsbelop
        eksportforbud = source.eksportforbud
        anvendtTrygdetid = source.anvendtTrygdetid?.let(::AnvendtTrygdetid)
        anvendtFlyktningEnum = source.anvendtFlyktningEnum
    }
}

