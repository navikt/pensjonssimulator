package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.util.*

class PersonOpptjeningsgrunnlag(
        /**
         * Brukes ikke av PREG
         */
        var fnr: String? = null,
        /**
         * Opptjeningen det skal beregnes pensjonspoeng for.
         * Feltene ar, opptjeningType er påkrevd. Dersom opptjeningType er lik PPI så er pi også påkrevd.
         */
        var opptjening: Opptjeningsgrunnlag? = null,
        /**
         * Brukers fødselsdato
         */
        var fodselsdato: Date? = null
) {

    constructor(personOpptjeningsgrunnlag: PersonOpptjeningsgrunnlag) : this() {
        this.fnr = personOpptjeningsgrunnlag.fnr
        if (personOpptjeningsgrunnlag.opptjening != null) {
            this.opptjening = Opptjeningsgrunnlag(personOpptjeningsgrunnlag.opptjening!!)
        }
        if (personOpptjeningsgrunnlag.fodselsdato != null) {
            this.fodselsdato = personOpptjeningsgrunnlag.fodselsdato!!.clone() as Date
        }
    }
}
