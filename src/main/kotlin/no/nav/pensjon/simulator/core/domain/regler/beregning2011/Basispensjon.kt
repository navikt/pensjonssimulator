package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum

// 2025-03-19
class Basispensjon {
    /**
     * Utgjør summen av basisgrunnpensjon, basistilleggspensjon og i basispensjonstillegg.
     */
    var totalbelop = 0.0

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
    var formelKodeEnum: FormelKodeEnum = FormelKodeEnum.ResPx
}
