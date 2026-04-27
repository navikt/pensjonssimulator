package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import java.time.LocalDate

// 2026-04-23
class Inntektsgrunnlag {
    /*
     * Unik identifikator for Inntektsgrunnlag. Endres ikke av regelmotoren,
     * men Inntektsgrunnlag med null id kan opprettes av batchtjenestene,
     * derfor settes den til typen wrapperobjekt Long i stedet for primitiven long.
     */
    var inntektsgrunnlagId: Long? = null

    /**
     * Inntektens størrelse, i hele kroner.
     */
    var belop = 0

    /**
     * Kode som angir type inntekt. Se K_INNTEKT_T.
     * Pr september 2007 så tabellen slik ut:
     * FPI Forventet arbeidsinntekt
     * FKI Forventet kapitalinntekt
     * PENT Forventet tjenestepensjonsinntekt (ikke folketrygd)
     * FBI Forventet bidrag o.l
     * HYPF Hypotetisk forventet arbeidsinntekt
     * HYPF2G Hypotetisk forventet arbeidsinntekt 2G
     * PGI Foreløpig pensjonsgivende inntekt
     * IMFU Inntekt Mnd før Uttak
     * PENF Pensjonsinntekt fra folketrygden
     * ARBLTO Arbeidsinntekt (Lønn og trekk)
     * ARBLIGN Arbeidsinntekt (Ligning)
     * PENSKD Pensjonsinntekt (ikke folketrygd)
     * KAP Kapitalinntekt
     */
    var inntektTypeEnum: InntekttypeEnum? = null

    /**
     * fra-og-med dato for gyldigheten av inntektsgrunnlaget.
     */
    var fomLd: LocalDate? = null

    /**
     * til-og-med dato for gyldigheten av inntektsgrunnlaget.
     */
    var tomLd: LocalDate? = null

    /**
     * Angir om inntektsgrunnlaget brukes som grunnlag på kravet.
     */
    var bruk: Boolean = true

    /**
     * Kilden til inntektsgrunnlaget.
     */
    var grunnlagKildeEnum: GrunnlagkildeEnum? = null

    //@JsonIgnore
    //var rawFom: Date? = null // SIMDOM-ADD
//
    //@JsonIgnore
    //var rawTom: Date? = null // SIMDOM-ADD

    constructor()

    constructor(source: Inntektsgrunnlag) {
        inntektsgrunnlagId = source.inntektsgrunnlagId
        belop = source.belop
        inntektTypeEnum = source.inntektTypeEnum
        fomLd = source.fomLd
        tomLd = source.tomLd
        bruk = source.bruk
        grunnlagKildeEnum = source.grunnlagKildeEnum
    }
}
