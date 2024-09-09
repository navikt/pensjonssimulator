package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid

/**
 * @author Steinar Hjellvik (Decisive) - PKPYTON-1746
 */
class EgenopptjentUforetrygd() {
    var faktor: Double = 0.66
    var formelKode: FormelKodeCti? = null
    var arsbelop: Int = 0

    var beregningsgrunnlagOrdiner: AbstraktBeregningsgrunnlag? = null

    var beregningsgrunnlagYrkesskade: AbstraktBeregningsgrunnlag? = null

    fun setBeregningsgrunnlagYrkesskadeCopy(aBeregningsgrunnlag: AbstraktBeregningsgrunnlag) {
        if (aBeregningsgrunnlag is BeregningsgrunnlagYrkesskade || aBeregningsgrunnlag is BeregningsgrunnlagKonvertert) {
            this.beregningsgrunnlagYrkesskade = aBeregningsgrunnlag
        }
    }

    fun setBeregningsgrunnlagOrdinerCopy(aBeregningsgrunnlag: AbstraktBeregningsgrunnlag) {
        if (aBeregningsgrunnlag is BeregningsgrunnlagOrdiner || aBeregningsgrunnlag is BeregningsgrunnlagKonvertert) {
            this.beregningsgrunnlagOrdiner = aBeregningsgrunnlag
        }
    }

    var beregningsgrunnlagYrkesskadeBest: Boolean = false

    /**
     * Prosentsats brukt for påslag.
     */
    var konverteringsPaslagForRedGP: Int = 0

    /**
     * Påslag pga økt redusert grunnpensjon sats til egenopptjent uføretrygd som angår konvertert uføretidpunkt.
     */
    var konverteringsPaslagForRedGPSats: Int = 0

    /**
     * Trygdetid som er brukt ved beregning av egenopptjent uføretrygd.
     */
    var anvendtTrygdetid: AnvendtTrygdetid? = null

    constructor(egenopptjentUforetrygd: EgenopptjentUforetrygd) : this() {
        faktor = egenopptjentUforetrygd.faktor
        arsbelop = egenopptjentUforetrygd.arsbelop
        beregningsgrunnlagYrkesskadeBest = egenopptjentUforetrygd.beregningsgrunnlagYrkesskadeBest
        konverteringsPaslagForRedGP = egenopptjentUforetrygd.konverteringsPaslagForRedGP
        konverteringsPaslagForRedGPSats = egenopptjentUforetrygd.konverteringsPaslagForRedGPSats
        if (egenopptjentUforetrygd.formelKode != null) {
            formelKode = FormelKodeCti(egenopptjentUforetrygd.formelKode!!)
        }
        if (egenopptjentUforetrygd.beregningsgrunnlagOrdiner != null) {
            if (egenopptjentUforetrygd.beregningsgrunnlagOrdiner is BeregningsgrunnlagOrdiner) {
                setBeregningsgrunnlagOrdinerCopy(BeregningsgrunnlagOrdiner((egenopptjentUforetrygd.beregningsgrunnlagOrdiner as BeregningsgrunnlagOrdiner?)!!))
            } else if (egenopptjentUforetrygd.beregningsgrunnlagOrdiner is BeregningsgrunnlagKonvertert) {
                setBeregningsgrunnlagOrdinerCopy(BeregningsgrunnlagKonvertert((egenopptjentUforetrygd.beregningsgrunnlagOrdiner as BeregningsgrunnlagKonvertert?)!!))
            }
        }
        if (egenopptjentUforetrygd.beregningsgrunnlagYrkesskade != null) {
            if (egenopptjentUforetrygd.beregningsgrunnlagYrkesskade is BeregningsgrunnlagYrkesskade) {
                setBeregningsgrunnlagYrkesskadeCopy(BeregningsgrunnlagYrkesskade((egenopptjentUforetrygd.beregningsgrunnlagYrkesskade as BeregningsgrunnlagYrkesskade?)!!))
            } else if (egenopptjentUforetrygd.beregningsgrunnlagYrkesskade is BeregningsgrunnlagKonvertert) {
                setBeregningsgrunnlagYrkesskadeCopy(BeregningsgrunnlagKonvertert((egenopptjentUforetrygd.beregningsgrunnlagYrkesskade as BeregningsgrunnlagKonvertert?)!!))
            }
        }
        if (egenopptjentUforetrygd.anvendtTrygdetid != null) {
            anvendtTrygdetid = AnvendtTrygdetid(egenopptjentUforetrygd.anvendtTrygdetid!!)
        }
    }

    constructor(
        faktor: Double = 0.66,
        formelKode: FormelKodeCti? = null,
        arsbelop: Int = 0,
        beregningsgrunnlagOrdiner: AbstraktBeregningsgrunnlag? = null,
        beregningsgrunnlagYrkesskade: AbstraktBeregningsgrunnlag? = null,
        beregningsgrunnlagYrkesskadeBest: Boolean = false,
        konverteringsPaslagForRedGP: Int = 0,
        konverteringsPaslagForRedGPSats: Int = 0,
        anvendtTrygdetid: AnvendtTrygdetid? = null
    ) : this() {
        this.faktor = faktor
        this.formelKode = formelKode
        this.arsbelop = arsbelop
        this.beregningsgrunnlagOrdiner = beregningsgrunnlagOrdiner
        this.beregningsgrunnlagYrkesskade = beregningsgrunnlagYrkesskade
        this.beregningsgrunnlagYrkesskadeBest = beregningsgrunnlagYrkesskadeBest
        this.konverteringsPaslagForRedGP = konverteringsPaslagForRedGP
        this.konverteringsPaslagForRedGPSats = konverteringsPaslagForRedGPSats
        this.anvendtTrygdetid = anvendtTrygdetid
    }
}
