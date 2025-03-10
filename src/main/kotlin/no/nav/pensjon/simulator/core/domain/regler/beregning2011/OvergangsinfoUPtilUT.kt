package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.MinstepensjonstypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.ResultatKildeEnum
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid

// Checked 2025-02-28
class OvergangsinfoUPtilUT {
    /**
     * Konvertert beregningsgrunnlag for Ordinår beregning
     */
    var konvertertBeregningsgrunnlagOrdiner: BeregningsgrunnlagKonvertert? = null

    /**
     * Konvertert beregningsgrunnlag for Yrkesskade beregning
     */
    var konvertertBeregningsgrunnlagYrkesskade: BeregningsgrunnlagKonvertert? = null

    /**
     * uføretrygd ektefelletilleg
     */
    var ektefelletilleggUT: EktefelletilleggUT? = null

    /**
     * Inntektsgrense for Friintektsdato
     */
    var inntektsgrenseorFriinntektsdato = 0

    /**
     * Konvertert beregningsgrunnlag for gjenlevendetillegg hvis fastsatt
     */
    var konvertertBeregningsgrunnlagGJT: BeregningsgrunnlagKonvertert? = null

    /**
     * Anvendt trygdetid fra konvertert uførepensjon
     */
    var anvendtTrygdetidUP: AnvendtTrygdetid? = null

    /**
     * Anvendt trygdetid fra hjemmeberegningen til konvertert uførepensjon
     */
    var anvendtTrygdetidUPHjemme: AnvendtTrygdetid? = null

    /**
     * Egen anvendt trygdetid fra UP
     */
    var anvendtTrygdetidUP_egen: AnvendtTrygdetid? = null

    /**
     * Hvorvidt utbetalt uførepensjonen per 31.12.2014 ble definert som minstepensjon.
     */
    var minstepensjontypeEnum: MinstepensjonstypeEnum? = null

    /**
     * Hvorvidt utbetalt uførepensjonen per 31.12.2014 ble manuelt overstyrt eller ikke.
     */
    var resultatKildeEnum: ResultatKildeEnum? = null

    /**
     * Netto særtillegg i utbetalt uførepensjonen per 31.12.2014.
     */
    var sertilleggNetto = 0

    constructor()

    constructor(source: OvergangsinfoUPtilUT) : this() {
        ektefelletilleggUT = source.ektefelletilleggUT?.let(::EktefelletilleggUT)
        inntektsgrenseorFriinntektsdato = source.inntektsgrenseorFriinntektsdato
        konvertertBeregningsgrunnlagOrdiner =
            source.konvertertBeregningsgrunnlagOrdiner?.let(::BeregningsgrunnlagKonvertert)
        konvertertBeregningsgrunnlagYrkesskade =
            source.konvertertBeregningsgrunnlagYrkesskade?.let(::BeregningsgrunnlagKonvertert)
        konvertertBeregningsgrunnlagGJT = source.konvertertBeregningsgrunnlagGJT?.let(::BeregningsgrunnlagKonvertert)
        anvendtTrygdetidUP = source.anvendtTrygdetidUP?.let(::AnvendtTrygdetid)
        anvendtTrygdetidUPHjemme = source.anvendtTrygdetidUPHjemme?.let(::AnvendtTrygdetid)
        anvendtTrygdetidUP_egen = source.anvendtTrygdetidUP_egen?.let(::AnvendtTrygdetid)
        minstepensjontypeEnum = source.minstepensjontypeEnum
        resultatKildeEnum = source.resultatKildeEnum
        sertilleggNetto = source.sertilleggNetto
    }
}
