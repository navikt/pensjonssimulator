package no.nav.pensjon.simulator.core.domain.regler.grunnlag

// Checked 2025-02-28
class Forstegangstjeneste {
    var periodeListe: MutableList<ForstegangstjenestePeriode> = mutableListOf()

    constructor()

    constructor(source: Forstegangstjeneste) : this() {
        source.periodeListe.forEach { periodeListe.add(ForstegangstjenestePeriode(it)) }
    }
}
