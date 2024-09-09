package no.nav.pensjon.simulator.core.afp.privat

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat

// no.nav.service.pensjon.simulering.abstractsimulerapfra2011.BeregnAfpPrivatResponse
data class PrivatAfpResult(
    val afpPrivatBeregningsresultatListe: MutableList<BeregningsResultatAfpPrivat>,
    val gjeldendeBeregningsresultatAfpPrivat: BeregningsResultatAfpPrivat?
)
