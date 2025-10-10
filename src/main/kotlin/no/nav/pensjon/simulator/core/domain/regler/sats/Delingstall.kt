package no.nav.pensjon.simulator.core.domain.regler.sats

import no.nav.pensjon.simulator.alder.Alder

data class Delingstall(
    val alder: Alder,
    val delingstall: Double,
)