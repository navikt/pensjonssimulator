package no.nav.pensjon.simulator.core.domain.regler.simulering

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarsvedtakResultatCti
import java.util.*

class Simuleringsresultat(
    var status: VilkarsvedtakResultatCti? = null,
    var beregning: Beregning? = null,
    var virk: Date? = null,
    var merknadListe: MutableList<Merknad> = mutableListOf()
)
