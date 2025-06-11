package no.nav.pensjon.simulator.generelt

import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat

data class GenerelleData(
    val person: Person,
    val privatAfpSatser: PrivatAfpSatser,
    val satsResultatListe: List<VeietSatsResultat>
)
