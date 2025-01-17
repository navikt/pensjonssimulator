package no.nav.pensjon.simulator.core.exception

import no.nav.pensjon.simulator.core.domain.regler.Merknad

// PEN: PEN166BeregningsmotorValidereException
class RegelmotorValideringException(
    message: String,
    val merknadListe: List<Merknad> = mutableListOf()
) : RuntimeException(message)
