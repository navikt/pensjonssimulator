package no.nav.pensjon.simulator.core.domain.regler

class BatchStatus(
    var merknadListe: MutableList<Merknad> = mutableListOf(),
    var statusOK: Boolean = false
) {
    constructor(statusOK: Boolean) : this() {
        this.statusOK = statusOK
    }
}
