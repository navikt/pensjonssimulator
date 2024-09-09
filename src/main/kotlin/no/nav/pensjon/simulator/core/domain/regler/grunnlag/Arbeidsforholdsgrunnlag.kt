package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import java.io.Serializable
import java.util.*

/**
 * Angir informasjon om stillingsprosent
 */
class Arbeidsforholdsgrunnlag(

        /**
         * Fom dato for arbeidsforholdet.
         */
        var fomDato: Date? = null,
        /**
         *  tomDato the tomDato to set
         */
        var tomDato: Date? = null,

        /**
         * Stillingsandel i prosent.
         */
        var stillingsprosent: Int = 0,

        /**
         * Navn på arbeidsgiver.
         */
        var arbeidsgiver: String? = null,

        /**
         * Arbeidsgivers juridiske organisasjonsnummer.
         */
        var orgNummer: String? = null
) : Comparable<Arbeidsforholdsgrunnlag>, Serializable {

    constructor(ag: Arbeidsforholdsgrunnlag) : this() {
        if (ag.fomDato != null) {
            this.fomDato = ag.fomDato!!.clone() as Date
        }
        if (ag.tomDato != null) {
            this.tomDato = ag.tomDato!!.clone() as Date
        }
        this.stillingsprosent = ag.stillingsprosent
        this.arbeidsgiver = ag.arbeidsgiver
        this.orgNummer = ag.orgNummer
    }

    override fun compareTo(other: Arbeidsforholdsgrunnlag): Int {
        return DateCompareUtil.compareTo(fomDato, other.fomDato)
    }
}
