package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum

data class Pre2025OffentligAfpSpec(
    val afpOrdning: AFPtypeEnum, // Hvilken AFP-ordning bruker er tilknyttet
    val inntektMaanedenFoerAfpUttakBeloep: Int, // Brukers inntekt måneden før uttak av AFP
    val inntektUnderAfpUttakBeloep: Int
)
