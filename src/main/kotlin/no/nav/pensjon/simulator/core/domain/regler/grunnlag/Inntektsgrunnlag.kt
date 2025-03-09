package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektTypeCti
import no.nav.pensjon.simulator.core.util.toNorwegianNoon
import java.util.*

// Checked 2025-02-28
class Inntektsgrunnlag {

    /**
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
    var inntektType: InntektTypeCti? = null
    var inntektTypeEnum: InntekttypeEnum? = null

    /**
     * fra-og-med dato for gyldigheten av inntektsgrunnlaget.
     */
    var fom: Date? = null
        set(value) { // SIMDOM-ADD
            rawFom = value
            field = value?.toNorwegianNoon()
        }

    /**
     * til-og-med dato for gyldigheten av inntektsgrunnlaget.
     */
    var tom: Date? = null
        set(value) { // SIMDOM-ADD
            rawTom = value
            field = value?.toNorwegianNoon()
        }

    /**
     * Angir om inntektsgrunnlaget brukes som grunnlag på kravet.
     */
    var bruk: Boolean? = null // SIMDOM-EDIT true -> null, since nullable in Inntektsgrunnlag in PEN

    /**
     * Kilden til inntektsgrunnlaget.
     */
    var grunnlagKilde: GrunnlagKildeCti? = null
    var grunnlagKildeEnum: GrunnlagkildeEnum? = null

    @JsonIgnore
    var rawFom: Date? = null // SIMDOM-ADD

    @JsonIgnore
    var rawTom: Date? = null // SIMDOM-ADD

    constructor()

    constructor(source: Inntektsgrunnlag) {
        inntektsgrunnlagId = source.inntektsgrunnlagId
        belop = source.belop
        inntektType = source.inntektType
        inntektTypeEnum = source.inntektTypeEnum
        fom = source.fom?.clone() as? Date
        tom = source.tom?.clone() as? Date
        bruk = source.bruk
        grunnlagKilde = source.grunnlagKilde
        grunnlagKildeEnum = source.grunnlagKildeEnum
    }
}
