package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.util.*

// Checked 2025-02-28
class PersonOpptjeningsgrunnlag {
    /**
     * Brukes ikke av pensjon-regler
     */
    var fnr: String? = null

    /**
     * Opptjeningen det skal beregnes pensjonspoeng for.
     * Feltene ar, opptjeningType er påkrevd. Dersom opptjeningType er lik PPI så er pi også påkrevd.
     */
    var opptjening: Opptjeningsgrunnlag? = null

    /**
     * Brukers Fødselsdato
     */
    var fodselsdato: Date? = null

    constructor()

    constructor(source: PersonOpptjeningsgrunnlag) : this() {
        fnr = source.fnr
        source.opptjening?.let { opptjening = Opptjeningsgrunnlag(it) }
        fodselsdato = source.fodselsdato?.clone() as? Date
    }
}
