package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat

data class FppSimuleringResult(
    val afpOrdning: AFPtypeEnum?,
    val simuleringsresultat: Simuleringsresultat
)
