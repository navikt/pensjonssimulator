package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseVedDodEnum
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import java.time.LocalDate

// 2026-04-23
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
     * dødsdato.
     */
    var dodstidspunktLd: LocalDate? = null

    /**
     * Hvis ufør ved død er dette gjeldende uføretidspunkt.
     */
    var uforetidspunktLd: LocalDate? = null

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
    var skadetidspunktLd: LocalDate? = null

    /**
     * Avdødes yrkesskadegrad.
     */
    var yrkesskadegrad = 0

    constructor() {}

    constructor(source: GjenlevendetilleggInformasjon) {
        anvendtTrygdetid = source.anvendtTrygdetid?.let(::AnvendtTrygdetid)
        dodstidspunktLd = source.dodstidspunktLd
        uforetidspunktLd = source.uforetidspunktLd
        ytelseVedDodEnum = source.ytelseVedDodEnum
        gjenlevendepensjon = source.gjenlevendepensjon
        minsteYtelseBenyttetUngUfor = source.minsteYtelseBenyttetUngUfor
        minsteYtelseArsbelop = source.minsteYtelseArsbelop
        minsteYtelseSats = source.minsteYtelseSats
        yrkesskade = source.yrkesskade
        skadetidspunktLd = source.skadetidspunktLd
        yrkesskadegrad = source.yrkesskadegrad

        (source.beregningsgrunnlagAvdodOrdiner as? BeregningsgrunnlagOrdiner)?.let {
            beregningsgrunnlagAvdodOrdiner = it.copy()
        }

        (source.beregningsgrunnlagAvdodOrdiner as? BeregningsgrunnlagKonvertert)?.let {
            beregningsgrunnlagAvdodOrdiner = it.copy()
        }

        (source.beregningsgrunnlagAvdodYrkesskade as? BeregningsgrunnlagYrkesskade)?.let {
            beregningsgrunnlagAvdodYrkesskade = it.copy()
        }

        (source.beregningsgrunnlagAvdodYrkesskade as? BeregningsgrunnlagKonvertert)?.let {
            beregningsgrunnlagAvdodYrkesskade = it.copy()
        }
    }
}
