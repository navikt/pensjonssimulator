package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.MinimumIfuTypeCti
import java.util.*

/**
 * Angir inntekt før uførhet (IFU) og hvorvidt inntekten er minimumsgrense eller ikke.
 * Inneholder ulike varianter av inntekt før uførhet som resulterer fra ulike måter å beregne denne inntekten,
 * i tillegg til den endelige inntekten som brukes.
 */
class InntektForUforhet : AbstraktBeregningsvilkar {
    /**
     * Angir om bruker kvalifiserer til Minste-IFU sats som ung ufør, enslig eller gift.
     */
    var minimumIfuType: MinimumIfuTypeCti? = null

    /**
     * Den endelige inntekten før uførhet som brukes.
     */
    var inntekt = 0

    /**
     * Angir om minimums IFU er benyttet eller ikke.
     */
    var erMinimumsIfu = false

    /**
     * Dato for den kroneverdi inntekt er oppgitt i.
     */
    var ifuDato: Date? = null

    /**
     * Den inntekt før uførhet som er angitt av saksbehandler. Ikke justert for minimumsIFU.
     */
    var angittInntekt = 0

    constructor() : super()

    constructor(inntektForUforhet: InntektForUforhet) : super(inntektForUforhet) {
        this.inntekt = inntektForUforhet.inntekt
        this.erMinimumsIfu = inntektForUforhet.erMinimumsIfu
        this.angittInntekt = inntektForUforhet.angittInntekt
        if (inntektForUforhet.minimumIfuType != null) {
            this.minimumIfuType = MinimumIfuTypeCti(inntektForUforhet.minimumIfuType)
        }
        if (inntektForUforhet.ifuDato != null) {
            this.ifuDato = inntektForUforhet.ifuDato!!.clone() as Date
        }
    }

    constructor(
        merknadListe: MutableList<Merknad> = mutableListOf(),

        /** Interne felt */
        minimumIfuType: MinimumIfuTypeCti? = null,
        inntekt: Int = 0,
        erMinimumsIfu: Boolean = false,
        ifuDato: Date? = null,
        angittInntekt: Int = 0
    ) : super(merknadListe) {
        this.minimumIfuType = minimumIfuType
        this.inntekt = inntekt
        this.erMinimumsIfu = erMinimumsIfu
        this.ifuDato = ifuDato
        this.angittInntekt = angittInntekt
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var ifu: InntektForUforhet? = null
        if (abstraktBeregningsvilkar.javaClass == InntektForUforhet::class.java) {
            ifu = InntektForUforhet(abstraktBeregningsvilkar as InntektForUforhet)
        }
        return ifu
    }
}
