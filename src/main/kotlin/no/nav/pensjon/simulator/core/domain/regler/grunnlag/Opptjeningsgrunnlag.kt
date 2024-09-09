package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.OpptjeningTypeCti
import java.io.Serializable

class Opptjeningsgrunnlag(

        var ar: Int = 0,
        var pi: Int = 0,

        /**
         * Anvendt pensjonsgivende inntekt.Redusert pi etter 1/3-regelen.
         * Brukes ved beregning av poengtallene.<br></br>
         * `0 =< pia <= 8.33G (som int)`
         */
        var pia: Int = 0,
        var pp: Double = 0.0,

        /**
         * Angir type opptjening. Se K_OPPTJN_T.
         * Pr september 2007 så tabellen slik ut:
         * OSFE Omsorg for syke/funksjonshemmede/eldre
         * OBO7H Omsorg for barn over 7 år med hjelpestønad sats 3 eller 4
         * OBU7 Omsorg for barn under 7 år
         * PPI Pensjonsgivende inntekt
         */
        var opptjeningType: OpptjeningTypeCti? = null,

        /**
         * Maks uføregrad for dette året
         */
        var maksUforegrad: Int = 0,
        /**
         * Angir om opptjeningsgrunnlaget brukes somm grunnlag på kravet.
         */
        var bruk: Boolean = false,
        /**
         * Kilden til opptjeningsgrunnlaget.
         */
        var grunnlagKilde: GrunnlagKildeCti? = null
) : Comparable<Opptjeningsgrunnlag>, Serializable {
    /**
     * Inneholder alle inntektstyper for dette året
     */
    var opptjeningTypeListe: MutableList<OpptjeningTypeMapping> = mutableListOf()

    constructor(opptjeningsgrunnlag: Opptjeningsgrunnlag) : this() {
        this.ar = opptjeningsgrunnlag.ar
        this.pi = opptjeningsgrunnlag.pi
        this.pia = opptjeningsgrunnlag.pia
        this.pp = opptjeningsgrunnlag.pp
        if (opptjeningsgrunnlag.opptjeningType != null) {
            this.opptjeningType = OpptjeningTypeCti(opptjeningsgrunnlag.opptjeningType)
        }
        this.maksUforegrad = opptjeningsgrunnlag.maksUforegrad
        this.bruk = opptjeningsgrunnlag.bruk
        if (opptjeningsgrunnlag.grunnlagKilde != null) {
            this.grunnlagKilde = GrunnlagKildeCti(opptjeningsgrunnlag.grunnlagKilde)
        }
        for (o in opptjeningsgrunnlag.opptjeningTypeListe) {
            this.opptjeningTypeListe.add(OpptjeningTypeMapping(o))
        }
    }

    constructor(ar: Int, pi: Int, pia: Int, pp: Double, opptjeningType: OpptjeningTypeCti, maksUforegrad: Int, isBruk: Boolean, grunnlagKilde: GrunnlagKildeCti,
                opptjeningTypeListe: MutableList<OpptjeningTypeMapping>) : this() {
        this.ar = ar
        this.pi = pi
        this.pia = pia
        this.pp = pp
        this.opptjeningType = opptjeningType
        this.maksUforegrad = maksUforegrad
        this.bruk = isBruk
        this.grunnlagKilde = grunnlagKilde
        this.opptjeningTypeListe = opptjeningTypeListe
    }

    constructor(ar: Int, pi: Int, pia: Int, pp: Double, opptjeningType: OpptjeningTypeCti, maksUforegrad: Int, grunnlagKilde: GrunnlagKildeCti,
                opptjeningTypeListe: MutableList<OpptjeningTypeMapping>) : this() {
        this.ar = ar
        this.pi = pi
        this.pia = pia
        this.pp = pp
        this.opptjeningType = opptjeningType
        this.maksUforegrad = maksUforegrad
        this.bruk = true
        this.grunnlagKilde = grunnlagKilde
        this.opptjeningTypeListe = opptjeningTypeListe
    }

    override fun compareTo(other: Opptjeningsgrunnlag): Int {
        return this.ar.minus(other.ar)
    }
}
