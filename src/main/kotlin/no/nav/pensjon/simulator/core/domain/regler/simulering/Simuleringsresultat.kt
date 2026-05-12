package no.nav.pensjon.simulator.core.domain.regler.simulering

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import java.time.LocalDate

// 2026-04-07
class Simuleringsresultat {
    /**
     * Status på vedtaket
     */
    var statusEnum: VedtakResultatEnum? = null

    /**
     * Beregningen
     */
    var beregning: Beregning? = null

    /**
     * Virkningstidspunkt
     */
    var virkLd: LocalDate? = null

    /**
     * Liste av merknader
     */
    var merknadListe: MutableList<Merknad> = mutableListOf()
}
