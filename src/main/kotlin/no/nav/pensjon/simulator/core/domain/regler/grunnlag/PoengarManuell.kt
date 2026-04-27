package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleLandEnum
import java.time.LocalDate

// 2026-04-23
/**
 * Klassen beskriver et poengår opptjent i utlandet. Settes av saksbehandler.
 */
class PoengarManuell {
    /**
     * Poengåret fra og med dato.
     */
    var fomLd: LocalDate? = null

    /**
     * Poengåret til og med dato.
     */
    var tomLd: LocalDate? = null

    /**
     * Angir om poengåret skal brukes i pro rata beregning.
     */
    var ikkeProrata = false

    /**
     * Angir om poengåret skal brukes i alternativ pro rata beregning.
     */
    var ikkeAlternativProrata = false

    /**
     * Avtaleland som poengår ble opptjent i.
     */
    var avtalelandEnum: AvtaleLandEnum? = null

    constructor()

    constructor(source: PoengarManuell) : this() {
        fomLd = source.fomLd
        tomLd = source.tomLd
        ikkeProrata = source.ikkeProrata
        ikkeAlternativProrata = source.ikkeAlternativProrata
        avtalelandEnum = source.avtalelandEnum
    }
}
