package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.YtelseVedDodCti
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid
import java.util.*

/**
 * @author Steinar Hjellvik (Decisive) - PK-11391
 */
class GjenlevendetilleggInformasjon {

    /**
     * Avdødes beregningsgrunnlag.
     */

    var beregningsgrunnlagAvdodOrdiner: AbstraktBeregningsgrunnlag? = null

    /**
     * Avdødes anvendte trygdetid.
     */

    var anvendtTrygdetid: AnvendtTrygdetid? = null

    /**
     * Dødsdato.
     */

    var dodstidspunkt: Date? = null

    /**
     * Hvis ufør ved død er dette gjeldende uføretidspunkt.
     */

    var uforetidspunkt: Date? = null

    /**
     * Hvilken ytelse avdød mottok ved sin død.
     */

    var ytelseVedDod: YtelseVedDodCti? = null

    /**
     * Angir om avdøde ga opphav til gjenlevendepensjon.
     */

    var gjenlevendepensjon: Boolean = false

    /**
     * Angir om ung ufør ble anvendt for avdøde.
     */

    var minsteYtelseBenyttetUngUfor: Boolean = false

    /**
     * Minsteytelsen med avdødes egen trygdetid.
     */

    var minsteYtelseArsbelop: Int = 0

    /**
     * Avdødes minsteytelsesats basert på gjenlevendes sivilstand.
     */

    var minsteYtelseSats: Double = 0.0

    /**
     * Yrkesskade beregnet for avdød.
     */

    var yrkesskade: Boolean = false

    /**
     * Avdødes beregningsgrunnlag for yrkesskade.
     */

    var beregningsgrunnlagAvdodYrkesskade: AbstraktBeregningsgrunnlag? = null

    /**
     * Skadetidspunkt ved yrkesskade.
     */

    var skadetidspunkt: Date? = null

    /**
     * Avdødes yrkesskadegrad.
     */

    var yrkesskadegrad: Int = 0

    constructor() {}

    constructor(gjenlevendetilleggInformasjon: GjenlevendetilleggInformasjon) {
        if (gjenlevendetilleggInformasjon.anvendtTrygdetid != null) {
            this.anvendtTrygdetid = AnvendtTrygdetid(gjenlevendetilleggInformasjon.anvendtTrygdetid!!)
        }
        if (gjenlevendetilleggInformasjon.dodstidspunkt != null) {
            this.dodstidspunkt = gjenlevendetilleggInformasjon.dodstidspunkt!!.clone() as Date
        }
        if (gjenlevendetilleggInformasjon.uforetidspunkt != null) {
            this.uforetidspunkt = gjenlevendetilleggInformasjon.uforetidspunkt!!.clone() as Date
        }
        if (gjenlevendetilleggInformasjon.ytelseVedDod != null) {
            this.ytelseVedDod = YtelseVedDodCti(gjenlevendetilleggInformasjon.ytelseVedDod)
        }
        this.gjenlevendepensjon = gjenlevendetilleggInformasjon.gjenlevendepensjon
        this.minsteYtelseBenyttetUngUfor = gjenlevendetilleggInformasjon.minsteYtelseBenyttetUngUfor
        this.minsteYtelseArsbelop = gjenlevendetilleggInformasjon.minsteYtelseArsbelop
        this.minsteYtelseSats = gjenlevendetilleggInformasjon.minsteYtelseSats
        this.yrkesskade = gjenlevendetilleggInformasjon.yrkesskade
        if (gjenlevendetilleggInformasjon.skadetidspunkt != null) {
            this.skadetidspunkt = gjenlevendetilleggInformasjon.skadetidspunkt!!.clone() as Date
        }
        this.yrkesskadegrad = gjenlevendetilleggInformasjon.yrkesskadegrad
        if (gjenlevendetilleggInformasjon.beregningsgrunnlagAvdodOrdiner != null) {
            if (gjenlevendetilleggInformasjon.beregningsgrunnlagAvdodOrdiner is BeregningsgrunnlagOrdiner) {
                this.beregningsgrunnlagAvdodOrdiner = BeregningsgrunnlagOrdiner((gjenlevendetilleggInformasjon.beregningsgrunnlagAvdodOrdiner as BeregningsgrunnlagOrdiner?)!!)
            } else if (gjenlevendetilleggInformasjon.beregningsgrunnlagAvdodOrdiner is BeregningsgrunnlagKonvertert) {
                this.beregningsgrunnlagAvdodOrdiner = BeregningsgrunnlagKonvertert((gjenlevendetilleggInformasjon.beregningsgrunnlagAvdodOrdiner as BeregningsgrunnlagKonvertert?)!!)
            }
        }
        if (gjenlevendetilleggInformasjon.beregningsgrunnlagAvdodYrkesskade != null) {
            if (gjenlevendetilleggInformasjon.beregningsgrunnlagAvdodYrkesskade is BeregningsgrunnlagYrkesskade) {
                this.beregningsgrunnlagAvdodYrkesskade = BeregningsgrunnlagYrkesskade((gjenlevendetilleggInformasjon.beregningsgrunnlagAvdodYrkesskade as BeregningsgrunnlagYrkesskade?)!!)
            } else if (gjenlevendetilleggInformasjon.beregningsgrunnlagAvdodYrkesskade is BeregningsgrunnlagKonvertert) {
                this.beregningsgrunnlagAvdodYrkesskade = BeregningsgrunnlagKonvertert((gjenlevendetilleggInformasjon.beregningsgrunnlagAvdodYrkesskade as BeregningsgrunnlagKonvertert?)!!)
            }
        }
    }

    constructor(
            beregningsgrunnlagAvdodOrdiner: AbstraktBeregningsgrunnlag? = null,
            anvendtTrygdetid: AnvendtTrygdetid? = null,
            dodstidspunkt: Date? = null,
            uforetidspunkt: Date? = null,
            ytelseVedDod: YtelseVedDodCti? = null,
            gjenlevendepensjon: Boolean = false,
            minsteYtelseBenyttetUngUfor: Boolean = false,
            minsteYtelseArsbelop: Int = 0,
            minsteYtelseSats: Double = 0.0,
            yrkesskade: Boolean = false,
            beregningsgrunnlagAvdodYrkesskade: AbstraktBeregningsgrunnlag? = null,
            skadetidspunkt: Date? = null,
            yrkesskadegrad: Int = 0
    ) {
        this.beregningsgrunnlagAvdodOrdiner = beregningsgrunnlagAvdodOrdiner
        this.anvendtTrygdetid = anvendtTrygdetid
        this.dodstidspunkt = dodstidspunkt
        this.uforetidspunkt = uforetidspunkt
        this.ytelseVedDod = ytelseVedDod
        this.gjenlevendepensjon = gjenlevendepensjon
        this.minsteYtelseBenyttetUngUfor = minsteYtelseBenyttetUngUfor
        this.minsteYtelseArsbelop = minsteYtelseArsbelop
        this.minsteYtelseSats = minsteYtelseSats
        this.yrkesskade = yrkesskade
        this.beregningsgrunnlagAvdodYrkesskade = beregningsgrunnlagAvdodYrkesskade
        this.skadetidspunkt = skadetidspunkt
        this.yrkesskadegrad = yrkesskadegrad
    }
}
