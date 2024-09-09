package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonIgnore

class Yrkesskadeopptjening(

    // Poeng etter antatt årlig inntekt.
    var paa: Double = 0.0,

    /*
     * Yrkesskadegrad.
     */
    var yug: Int = 0,

    /*
     * Antatt Inntekt for yrkesskadeopptjeningen.
     */
    var antattInntektYrke: Double = 0.0,

    @JsonIgnore var ai_yp_proRata: Double = 0.0,
    @JsonIgnore var ai_yp_gradert: Double = 0.0,
    @JsonIgnore var ai_yp_gradert_proRata: Double = 0.0
) {
    constructor(paa: Double, yug: Int, antattInntektYrke: Double) : this() {
        this.paa = paa
        this.yug = yug
        this.antattInntektYrke = antattInntektYrke
    }

    constructor(yo: Yrkesskadeopptjening) : this() {
        this.paa = yo.paa
        this.yug = yo.yug
        this.antattInntektYrke = yo.antattInntektYrke
        this.ai_yp_proRata = yo.ai_yp_proRata
        this.ai_yp_gradert = yo.ai_yp_gradert
        this.ai_yp_gradert_proRata = yo.ai_yp_gradert_proRata
    }
}
