package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import java.io.Serializable

/**
 * Kopi av PEN 28/8/2009 - ruller tilbake til PREG domenemodell
 *
 * @author Ørnulf Moen
 */

class Basispensjon : Serializable {

    /**
     * Utgjør summen av basisgrunnpensjon, basistilleggspensjon og i basispensjonstillegg.
     */
    var totalbelop: Double = 0.0

    /**
     * Basisgrunnpensjon slik det er definert i nytt regelverk på gammel opptjening (kapittel 19).
     */
    var gp: BasisGrunnpensjon? = null

    /**
     * Basistilleggspensjon slik det er definert i nytt regelverk på gammel opptjening (kapittel 19).
     */
    var tp: BasisTilleggspensjon? = null

    /**
     * Basispensjonstillegg slik det er definert i nytt regelverk på gammel opptjening (kapittel 19).
     */
    var pt: BasisPensjonstillegg? = null

    /**
     * Formelkode kun for bruk for restpensjon
     */
    var formelKode: FormelKodeCti? = null

    init {
        formelKode = FormelKodeCti("ResPx")
    }

    constructor()

    constructor(bp: Basispensjon) : this() {
        totalbelop = bp.totalbelop
        if (bp.gp != null) {
            gp = BasisGrunnpensjon(bp.gp!!)
        }
        if (bp.tp != null) {
            tp = BasisTilleggspensjon(bp.tp!!)
        }
        if (bp.pt != null) {
            pt = BasisPensjonstillegg(bp.pt!!)
        }
        if (bp.formelKode != null) {
            formelKode = FormelKodeCti(bp.formelKode!!.kode)
        }
    }

    constructor(
            totalbelop: Double = 0.0,
            gp: BasisGrunnpensjon? = null,
            tp: BasisTilleggspensjon? = null,
            pt: BasisPensjonstillegg? = null,
            formelKode: FormelKodeCti? = FormelKodeCti("ResPx")
    ) {
        this.totalbelop = totalbelop
        this.gp = gp
        this.tp = tp
        this.pt = pt
        this.formelKode = formelKode
    }
}
