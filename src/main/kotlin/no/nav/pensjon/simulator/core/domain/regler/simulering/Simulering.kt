package no.nav.pensjon.simulator.core.domain.regler.simulering

import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

// 2025-03-09
class Simulering {

    /**
     * Type simulering
     */
    var simuleringTypeEnum: SimuleringTypeEnum? = null

    /**
     * Type AFP ordning
     */
    var afpOrdningEnum: AFPtypeEnum? = null

    /**
     * Dato for når bruker ønsker å simulere uttak av pensjon fra.
     */
    var uttaksdato: Date? = null

    /**
     * Liste av tilknyttede personer.
     */
    var persongrunnlagListe: List<Persongrunnlag> = mutableListOf()
    var vilkarsvedtakliste: MutableList<VilkarsVedtak> = mutableListOf()
}
