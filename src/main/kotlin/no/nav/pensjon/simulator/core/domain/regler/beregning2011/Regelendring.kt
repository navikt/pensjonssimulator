package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.RegelendringTypeEnum
import java.util.*

// 2025-03-10
class Regelendring {
    /**
     * Datoen en regel- eller satsendring har virkningsdato.
     */
    var endringsdato: Date? = null

    /**
     * Tekst som beskriver typen endring, ref. kodeverk.
     */
    var endringstypeEnum: RegelendringTypeEnum? = null

    constructor() : super()

    constructor(source: Regelendring) : super() {
        endringsdato = source.endringsdato?.clone() as? Date
        endringstypeEnum = source.endringstypeEnum
    }
}
