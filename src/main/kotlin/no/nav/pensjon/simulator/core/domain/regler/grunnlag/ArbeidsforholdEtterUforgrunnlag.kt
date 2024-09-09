package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import java.io.Serializable
import java.util.*

class ArbeidsforholdEtterUforgrunnlag(
    /**
     * Fom date for arbeidsforholdet.
     */
    var fomDato: Date? = null,
    /**
     *  stillingsprosent the work load to set
     */
    var stillingsprosent: Int = 0,
    /**
     *  varigTilrettelagtArbeid if it is lasting facilitated work
     */
    var varigTilrettelagtArbeid: Boolean = false
) : Comparable<ArbeidsforholdEtterUforgrunnlag>, Serializable {

    constructor(arbeidsforholdEtterUforgrunnlag: ArbeidsforholdEtterUforgrunnlag) : this() {
        if (arbeidsforholdEtterUforgrunnlag.fomDato != null) {
            this.fomDato = arbeidsforholdEtterUforgrunnlag.fomDato
        }
        this.stillingsprosent = arbeidsforholdEtterUforgrunnlag.stillingsprosent
        this.varigTilrettelagtArbeid = arbeidsforholdEtterUforgrunnlag.varigTilrettelagtArbeid
    }

    override fun compareTo(other: ArbeidsforholdEtterUforgrunnlag): Int {
        return DateCompareUtil.compareTo(fomDato, other.fomDato)
    }
}
