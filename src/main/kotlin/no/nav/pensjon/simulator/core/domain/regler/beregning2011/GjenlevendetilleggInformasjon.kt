package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseVedDodEnum
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid
import java.util.*

// 2025-03-10
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
    var ytelseVedDodEnum: YtelseVedDodEnum? = null

    /**
     * Angir om avdøde ga opphav til gjenlevendepensjon.
     */
    var gjenlevendepensjon = false

    /**
     * Angir om ung ufør ble anvendt for avdøde.
     */
    var minsteYtelseBenyttetUngUfor = false

    /**
     * Minsteytelsen med avdødes egen trygdetid.
     */
    var minsteYtelseArsbelop = 0

    /**
     * Avdødes minsteytelsesats basert på gjenlevendes sivilstand.
     */
    var minsteYtelseSats = 0.0

    /**
     * Yrkesskade beregnet for avdød.
     */
    var yrkesskade = false

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
    var yrkesskadegrad = 0

    constructor() {}

    constructor(source: GjenlevendetilleggInformasjon) {
        anvendtTrygdetid = source.anvendtTrygdetid?.let(::AnvendtTrygdetid)
        dodstidspunkt = source.dodstidspunkt?.clone() as? Date
        uforetidspunkt = source.uforetidspunkt?.clone() as? Date
        ytelseVedDodEnum = source.ytelseVedDodEnum
        gjenlevendepensjon = source.gjenlevendepensjon
        minsteYtelseBenyttetUngUfor = source.minsteYtelseBenyttetUngUfor
        minsteYtelseArsbelop = source.minsteYtelseArsbelop
        minsteYtelseSats = source.minsteYtelseSats
        yrkesskade = source.yrkesskade
        skadetidspunkt = source.skadetidspunkt?.clone() as? Date
        yrkesskadegrad = source.yrkesskadegrad

        (source.beregningsgrunnlagAvdodOrdiner as? BeregningsgrunnlagOrdiner)?.let {
            beregningsgrunnlagAvdodOrdiner = BeregningsgrunnlagOrdiner(it)
        }

        (source.beregningsgrunnlagAvdodOrdiner as? BeregningsgrunnlagKonvertert)?.let {
            beregningsgrunnlagAvdodOrdiner = BeregningsgrunnlagKonvertert(it)
        }

        (source.beregningsgrunnlagAvdodYrkesskade as? BeregningsgrunnlagYrkesskade)?.let {
            beregningsgrunnlagAvdodYrkesskade = BeregningsgrunnlagYrkesskade(it)
        }

        (source.beregningsgrunnlagAvdodYrkesskade as? BeregningsgrunnlagKonvertert)?.let {
            beregningsgrunnlagAvdodYrkesskade = BeregningsgrunnlagKonvertert(it)
        }
    }
}
