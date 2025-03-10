package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.enum.MinimumIfuTypeEnum
import java.util.*

// 2025-03-10
/**
 * Angir inntekt før uførhet (IFU) og hvorvidt inntekten er minimumsgrense eller ikke.
 * Inneholder ulike varianter av inntekt før uførhet som resulterer fra ulike måter å beregne denne inntekten,
 * i tillegg til den endelige inntekten som brukes.
 */
class InntektForUforhet : AbstraktBeregningsvilkar {
    /**
     * Angir om bruker kvalifiserer til Minste-IFU sats som ung ufør, enslig eller gift.
     */
    var minimumIfuTypeEnum: MinimumIfuTypeEnum? = null

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

    constructor(source: InntektForUforhet) : super(source) {
        inntekt = source.inntekt
        erMinimumsIfu = source.erMinimumsIfu
        angittInntekt = source.angittInntekt
        minimumIfuTypeEnum = source.minimumIfuTypeEnum
        ifuDato = source.ifuDato?.clone() as? Date
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var ifu: InntektForUforhet? = null
        if (abstraktBeregningsvilkar.javaClass == InntektForUforhet::class.java) {
            ifu = InntektForUforhet(abstraktBeregningsvilkar as InntektForUforhet)
        }
        return ifu
    }
}
