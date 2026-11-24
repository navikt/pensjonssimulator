package no.nav.pensjon.simulator.tjenestepensjon

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.TjenestepensjonSimuleringsresultatVedFeil

class TjenestepensjonException : RuntimeException {
    val resultat: TjenestepensjonSimuleringsresultatVedFeil

    constructor(message: String, resultat: TjenestepensjonSimuleringsresultatVedFeil) : super(message) {
        this.resultat = resultat
    }
}