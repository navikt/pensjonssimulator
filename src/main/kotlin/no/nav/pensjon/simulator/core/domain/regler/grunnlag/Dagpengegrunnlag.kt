package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.DagpengetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.DagpengeTypeCti

// Checked 2025-02-28
class Dagpengegrunnlag {
    var ar = 0
    var dagpengeType: DagpengeTypeCti? = null
    var dagpengetypeEnum: DagpengetypeEnum? = null
    var uavkortetDagpengegrunnlag = 0
    var utbetalteDagpenger = 0
    var ferietillegg = 0
    var barnetillegg = 0

    constructor()

    constructor(source: Dagpengegrunnlag) : this() {
        ar = source.ar
        source.dagpengeType?.let { dagpengeType = DagpengeTypeCti(it) }
        dagpengetypeEnum = source.dagpengetypeEnum
        uavkortetDagpengegrunnlag = source.uavkortetDagpengegrunnlag
        utbetalteDagpenger = source.utbetalteDagpenger
        ferietillegg = source.ferietillegg
        barnetillegg = source.barnetillegg
    }
}
