package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid

// 2025-03-10
class EgenopptjentUforetrygd {
    var faktor = 0.66
    var formelKodeEnum: FormelKodeEnum? = null
    var arsbelop = 0
    var beregningsgrunnlagOrdiner: AbstraktBeregningsgrunnlag? = null
        set(aBeregningsgrunnlag) {
            if (aBeregningsgrunnlag is BeregningsgrunnlagOrdiner
                || aBeregningsgrunnlag is BeregningsgrunnlagKonvertert
            ) {
                field = aBeregningsgrunnlag
            }
        }
    var beregningsgrunnlagYrkesskade: AbstraktBeregningsgrunnlag? = null
        set(aBeregningsgrunnlag) {
            if (aBeregningsgrunnlag is BeregningsgrunnlagYrkesskade
                || aBeregningsgrunnlag is BeregningsgrunnlagKonvertert
            ) {
                field = aBeregningsgrunnlag
            }
        }
    var beregningsgrunnlagYrkesskadeBest = false

    /**
     * Prosentsats brukt for Påslag.
     */
    var konverteringsPaslagForRedGP = 0

    /**
     * Påslag pga økt redusert grunnpensjon sats til egenopptjent uføretrygd som angår konvertert Uføretidpunkt.
     */
    var konverteringsPaslagForRedGPSats = 0

    /**
     * Trygdetid som er brukt ved beregning av egenopptjent uføretrygd.
     */
    var anvendtTrygdetid: AnvendtTrygdetid? = null

    constructor()

    constructor(source: EgenopptjentUforetrygd) : this() {
        faktor = source.faktor
        arsbelop = source.arsbelop
        beregningsgrunnlagYrkesskadeBest = source.beregningsgrunnlagYrkesskadeBest
        konverteringsPaslagForRedGP = source.konverteringsPaslagForRedGP
        konverteringsPaslagForRedGPSats = source.konverteringsPaslagForRedGPSats
        formelKodeEnum = source.formelKodeEnum

        (source.beregningsgrunnlagOrdiner as? BeregningsgrunnlagOrdiner)?.let {
            beregningsgrunnlagOrdiner = BeregningsgrunnlagOrdiner(it)
        }

        (source.beregningsgrunnlagOrdiner as? BeregningsgrunnlagKonvertert)?.let {
            beregningsgrunnlagOrdiner = BeregningsgrunnlagKonvertert(it)
        }

        (source.beregningsgrunnlagYrkesskade as? BeregningsgrunnlagYrkesskade)?.let {
            beregningsgrunnlagYrkesskade = BeregningsgrunnlagYrkesskade(it)
        }

        (source.beregningsgrunnlagYrkesskade as? BeregningsgrunnlagKonvertert)?.let {
            beregningsgrunnlagYrkesskade = BeregningsgrunnlagKonvertert(it)
        }

        anvendtTrygdetid = source.anvendtTrygdetid?.let(::AnvendtTrygdetid)
    }
}
