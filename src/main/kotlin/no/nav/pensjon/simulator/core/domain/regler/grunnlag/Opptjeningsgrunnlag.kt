package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.OpptjeningTypeCti

// Checked 2025-02-28
class Opptjeningsgrunnlag {
    /**
     * år for opptjeningen.
     */
    var ar = 0

    /**
     * Pensjonsgivende inntekt.
     */
    var pi = 0

    /**
     * Anvendt pensjonsgivende inntekt.Redusert pi etter 1/3-regelen.
     * Brukes ved beregning av poengtallene.<br></br>
     * `0 =< pia <= 8.33G (som int)`
     */
    var pia = 0

    /**
     * Beregnet pensjonspoeng.
     */
    var pp = 0.0

    /**
     * Angir type opptjening. Se K_OPPTJN_T.
     * Pr september 2007 så tabellen slik ut:
     * OSFE Omsorg for syke/funksjonshemmede/eldre
     * OBO7H Omsorg for barn over 7 år med hjelpestønad sats 3 eller 4
     * OBU7 Omsorg for barn under 7 år
     * PPI Pensjonsgivende inntekt
     */
    var opptjeningTypeEnum: OpptjeningtypeEnum? = null
    var opptjeningType: OpptjeningTypeCti? = null

    /**
     * Maks uføregrad for dette året
     */
    var maksUforegrad = 0

    /**
     * Angir om opptjeningsgrunnlaget brukes somm grunnlag på kravet.
     */
    var bruk: Boolean = true // NB: false in regler-api 2025-02-28

    /**
     * Kilden til opptjeningsgrunnlaget.
     */
    var grunnlagKildeEnum: GrunnlagkildeEnum? = null
    var grunnlagKilde: GrunnlagKildeCti? = null

    /**
     * Inneholder alle inntektstyper for dette året
     */
    var opptjeningTypeListe: MutableList<OpptjeningTypeMapping> = mutableListOf()

    constructor()

    constructor(source: Opptjeningsgrunnlag) : this() {
        ar = source.ar
        pi = source.pi
        pia = source.pia
        pp = source.pp
        opptjeningType = source.opptjeningType
        opptjeningTypeEnum = source.opptjeningTypeEnum
        maksUforegrad = source.maksUforegrad
        bruk = source.bruk
        grunnlagKilde = source.grunnlagKilde
        grunnlagKildeEnum = source.grunnlagKildeEnum
        source.opptjeningTypeListe.forEach {
            opptjeningTypeListe.add(OpptjeningTypeMapping().apply {
                opptjeningPOPPTypeCti = it.opptjeningPOPPTypeCti
                opptjeningPOPPTypeEnum = it.opptjeningPOPPTypeEnum
            })
        }
    }
}
