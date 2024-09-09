package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import java.util.*

class BeregningsgrunnlagOrdiner : AbstraktBeregningsgrunnlag {
    var opptjeningUTListe: MutableList<OpptjeningUT> = mutableListOf()
    var nasjonaltSnitt: Int = 0
    /**
     * Angir det sluttpoengtall som ordinært beregningsgrunnlag er omregnet fra.
     * Angår kun beregning av avdøde i sammenheng med nytt UT_GJT.
     */
    var sluttpoengtall: Double = 0.0

    constructor() : super() {
        opptjeningUTListe = ArrayList()
    }

    constructor(beregningsgrunnlagOrdinaer: BeregningsgrunnlagOrdiner) : super(beregningsgrunnlagOrdinaer) {
        opptjeningUTListe = ArrayList()
        nasjonaltSnitt = beregningsgrunnlagOrdinaer.nasjonaltSnitt
        this.sluttpoengtall = beregningsgrunnlagOrdinaer.sluttpoengtall
        for (opptjeningUT in beregningsgrunnlagOrdinaer.opptjeningUTListe) {
            opptjeningUTListe.add(OpptjeningUT(opptjeningUT))
        }
    }

    constructor(
        opptjeningUTListe: MutableList<OpptjeningUT> = mutableListOf(),
        nasjonaltSnitt: Int = 0,
        sluttpoengtall: Double = 0.0,
        /** super AbstraktBeregningsgrunnlag */
            formelKode: FormelKodeCti? = null,
        arsbelop: Int = 0,
        antattInntektFaktorKap19: Double = 0.0,
        antattInntektFaktorKap20: Double = 0.0
    ) : super(
            formelKode = formelKode,
            arsbelop = arsbelop,
            antattInntektFaktorKap19 = antattInntektFaktorKap19,
            antattInntektFaktorKap20 = antattInntektFaktorKap20
    ) {
        this.opptjeningUTListe = opptjeningUTListe
        this.nasjonaltSnitt = nasjonaltSnitt
        this.sluttpoengtall = sluttpoengtall
    }

    /**
     * Returnerer array med OpptjeningUT sortert med største først etter:
     * 1. avkortetBelop
     * 2. år
     * Original liste blir ikke endret.
     */
    fun sortertOpptjeningUTListe(): List<OpptjeningUT> {
        val sortertOpptjeningUTListe = ArrayList(opptjeningUTListe)
        sortertOpptjeningUTListe.sort()
        return sortertOpptjeningUTListe
    }
}
