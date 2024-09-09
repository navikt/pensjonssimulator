package no.nav.pensjon.simulator.core.exception

import no.nav.pensjon.simulator.core.domain.regler.Merknad

class BeregningsmotorValidereException(
    message: String,
    val merknadListe: List<Merknad> = mutableListOf()
) : RuntimeException(message)
