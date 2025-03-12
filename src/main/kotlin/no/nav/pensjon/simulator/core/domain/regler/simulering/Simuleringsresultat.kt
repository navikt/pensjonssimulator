package no.nav.pensjon.simulator.core.domain.regler.simulering

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import java.util.*

class Simuleringsresultat(
    var statusEnum: VedtakResultatEnum? = null,
    var beregning: Beregning? = null,
    var virk: Date? = null,
    var merknadListe: MutableList<Merknad> = mutableListOf()
)
