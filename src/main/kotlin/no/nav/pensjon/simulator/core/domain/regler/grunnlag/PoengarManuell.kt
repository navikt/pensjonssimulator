package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleLandEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.AvtalelandCti
import java.util.*

/**
 * Klassen beskriver et poengår opptjent i utlandet. Settes av saksbehandler.
 */
// Checked 2025-02-28
class PoengarManuell {
    /**
     * Poengåret fra og med dato.
     */
    var fom: Date? = null

    /**
     * Poengåret til og med dato.
     */
    var tom: Date? = null

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
    var avtaleland: AvtalelandCti? = null
    var avtalelandEnum: AvtaleLandEnum? = null

    constructor()

    constructor(source: PoengarManuell) : this() {
        fom = source.fom?.clone() as? Date
        tom = source.tom?.clone() as? Date
        ikkeProrata = source.ikkeProrata
        ikkeAlternativProrata = source.ikkeAlternativProrata
        avtaleland = source.avtaleland?.let(::AvtalelandCti)
        avtalelandEnum = source.avtalelandEnum
    }
}
