package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektTypeCti
import no.nav.pensjon.simulator.core.domain.regler.util.Copyable
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import java.io.Serializable
import java.util.*

class Inntektsgrunnlag : Comparable<Inntektsgrunnlag>, Copyable<Inntektsgrunnlag>, Serializable {
    /**
     * Unik identifikator for Inntektsgrunnlag. Endres ikke av regelmotoren,
     * men Inntektsgrunnlag med null id kan opprettes av batchtjenestene,
     * derfor settes den til typen wrapperobjekt Long i stedet for primitiven long.
     */
    var inntektsgrunnlagId: Long? = null

    /**
     * Inntektens størrelse, i hele kroner.
     */
    var belop: Int = 0

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
     * IMFU Inntekt Mnd Før Uttak
     * PENF Pensjonsinntekt fra folketrygden
     * ARBLTO Arbeidsinntekt (Lønn og trekk)
     * ARBLIGN Arbeidsinntekt (Ligning)
     * PENSKD Pensjonsinntekt (ikke folketrygd)
     * KAP Kapitalinntekt
     */
    var inntektType: InntektTypeCti? = null

    /**
     * fra-og-med dato for gyldigheten av inntektsgrunnlaget.
     */
    var fom: Date? = null
        set(value) {
            rawFom = value
            field = value?.noon()
        }

    @JsonIgnore
    var rawFom: Date? = null // SIMDOM-ADD

    /**
     * til-og-med dato for gyldigheten av inntektsgrunnlaget.
     */
    var tom: Date? = null
        set(value) {
            rawTom = value
            field = value?.noon()
        }

    @JsonIgnore
    var rawTom: Date? = null // SIMDOM-ADD

    /**
     * Angir om inntektsgrunnlaget brukes som grunnlag på kravet.
     */
    var bruk: Boolean = false

    /**
     * Kilden til inntektsgrunnlaget.
     */
    var grunnlagKilde: GrunnlagKildeCti? = null

    val isInntektsgrunnlagIdNull: Boolean
        get() = inntektsgrunnlagId == null

    constructor()

    constructor(inntektsgrunnlag: Inntektsgrunnlag) {
        if (inntektsgrunnlag.inntektsgrunnlagId != null) {
            this.inntektsgrunnlagId = inntektsgrunnlag.inntektsgrunnlagId
        }
        this.belop = inntektsgrunnlag.belop
        if (inntektsgrunnlag.inntektType != null) {
            this.inntektType = InntektTypeCti(inntektsgrunnlag.inntektType)
        }
        if (inntektsgrunnlag.fom != null) {
            this.fom = inntektsgrunnlag.fom!!.clone() as Date
        }
        if (inntektsgrunnlag.tom != null) {
            this.tom = inntektsgrunnlag.tom!!.clone() as Date
        }
        this.bruk = inntektsgrunnlag.bruk
        if (inntektsgrunnlag.grunnlagKilde != null) {
            this.grunnlagKilde = GrunnlagKildeCti(inntektsgrunnlag.grunnlagKilde)
        }
    }

    /**
     * Copy constructor
     * Brukes i spesialtilfeller der vi vil opprette et inntektsgrunnlag fra Blaze hvor ID skal være null.
     * Dette brukes i enkelte batchtjenester, og kan ikke gjøres direkte i Blaze fordi Long blir mappet til integer.
     */
    constructor(inntektsgrunnlag: Inntektsgrunnlag, inntektsgrunnlagIdNull: Boolean) : this(inntektsgrunnlag) {
        if (inntektsgrunnlagIdNull) {
            inntektsgrunnlagId = null
        }
    }

    constructor(
        inntektsgrunnlagId: Long? = null,
        belop: Int = 0,
        inntektType: InntektTypeCti? = null,
        fom: Date? = null,
        tom: Date? = null,
        bruk: Boolean = true,
        grunnlagKilde: GrunnlagKildeCti? = null
    ) {
        this.inntektsgrunnlagId = inntektsgrunnlagId
        this.belop = belop
        this.inntektType = inntektType
        this.fom = fom
        this.tom = tom
        this.bruk = bruk
        this.grunnlagKilde = grunnlagKilde
    }

    override fun compareTo(other: Inntektsgrunnlag): Int {
        return DateCompareUtil.compareTo(fom, other.fom)
    }

    override fun deepCopy(): Inntektsgrunnlag {
        return Inntektsgrunnlag(this)
    }
}
