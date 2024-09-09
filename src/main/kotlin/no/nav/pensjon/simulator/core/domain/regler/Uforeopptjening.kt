package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonIgnore

class Uforeopptjening(
    /*
     * Beløp for hva uføreopptjeningen utgjør.
     */
    var belop: Double = 0.0,

    /*
     * Angir hvorvidt opptjeningen baseres på en proRata beregnet uførepensjon.
     */
    var proRataBeregnetUP: Boolean = false,

    /*
     * Angir poengtallet som ligger til grunn utregningen.
     */
    var poengtall: Double = 0.0,

    /*
     * Uførepensjonens uføregrad.
     */
    var ufg: Int = 0,

    /*
     * Antatt Inntekt.
     */
    var antattInntekt: Double = 0.0,

    /*
     * Antatt Inntekt. ProRata variant.
     */
    var antattInntekt_proRata: Double = 0.0,

    /*
     * Angir hvor stor andel av opptjeningen kan benyttes i året (fra 0.00 til 1.00).
     */
    var andel_proRata: Double = 0.0,

    /*
     * Poengår teller for proRata brøk.
     */
    var poengarTeller_proRata: Int = 0,

    /*
     * Poengår nevner for proRata brøk.
     */
    var poengarNevner_proRata: Int = 0,

    /*
     * Antall fremtidige år, etter avkortning fra evt redFTT.
     */
    var antFremtidigeAr_proRata: Int = 0,

    /*
     * Angir yrkesskade delen av opptjeningen.
     */
    var yrkesskadeopptjening: Yrkesskadeopptjening? = null,

    /**
     * Angir om opptjening er basert på uføretrygd (true) eller uførepensjon (false).
     */
    var uforetrygd: Boolean = false,

    /**
     * Angir om årets uføreopptjening er relatert til uføreperiode hvor uføretidspunktet er konvertert fra uførepensjon.
     */
    var konvertertUFT: Boolean = false,

    @JsonIgnore var ai_up_gradert: Double = 0.0,
    @JsonIgnore var ai_up_gradert_proRata: Double = 0.0,
    @JsonIgnore var ai_up_restGradert: Double = 0.0,
    @JsonIgnore var ai_up_restGradert_proRata: Double = 0.0,
    @JsonIgnore var ai_upyp: Double = 0.0
) {
    // SIMDOM-ADD
    @JsonIgnore var uforear: Boolean? = null

    constructor(
        belop: Double,
        proRataBeregnetUP: Boolean,
        poengtall: Double,
        ufg: Int,
        antattInntekt: Double,
        antattInntekt_proRata: Double,
        andel_proRata: Double,
        poengarTeller_proRata: Int,
        poengarNevner_proRata: Int,
        antFremtidigeAr_proRata: Int,
        yrkesskadeopptjening: Yrkesskadeopptjening?
    ) : this() {
        this.belop = belop
        this.proRataBeregnetUP = proRataBeregnetUP
        this.poengtall = poengtall
        this.ufg = ufg
        this.antattInntekt = antattInntekt
        this.antattInntekt_proRata = antattInntekt_proRata
        this.andel_proRata = andel_proRata
        this.poengarTeller_proRata = poengarTeller_proRata
        this.poengarNevner_proRata = poengarNevner_proRata
        this.antFremtidigeAr_proRata = antFremtidigeAr_proRata
        this.yrkesskadeopptjening = yrkesskadeopptjening
    }

    constructor(uo: Uforeopptjening) : this() {
        this.belop = uo.belop
        this.proRataBeregnetUP = uo.proRataBeregnetUP
        this.poengtall = uo.poengtall
        this.ufg = uo.ufg
        this.antattInntekt = uo.antattInntekt
        this.antattInntekt_proRata = uo.antattInntekt_proRata
        this.andel_proRata = uo.andel_proRata
        this.poengarTeller_proRata = uo.poengarTeller_proRata
        this.poengarNevner_proRata = uo.poengarNevner_proRata
        this.antFremtidigeAr_proRata = uo.antFremtidigeAr_proRata
        if (uo.yrkesskadeopptjening != null) {
            this.yrkesskadeopptjening = Yrkesskadeopptjening(uo.yrkesskadeopptjening!!)
        }
        this.uforetrygd = uo.uforetrygd
        this.konvertertUFT = uo.konvertertUFT
        this.ai_up_gradert = uo.ai_up_gradert
        this.ai_up_gradert_proRata = uo.ai_up_gradert_proRata
        this.ai_up_restGradert = uo.ai_up_restGradert
        this.ai_up_restGradert_proRata = uo.ai_up_restGradert_proRata
        this.ai_upyp = uo.ai_upyp
    }
}
