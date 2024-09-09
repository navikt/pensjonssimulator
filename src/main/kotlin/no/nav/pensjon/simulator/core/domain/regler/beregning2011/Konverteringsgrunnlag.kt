package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Sertillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.Grunnpensjon
import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon

/**
 * Grunnlag for konvertering
 *
 * @author Swiddy de Louw (Capgemini) - PK-7113
 */

class Konverteringsgrunnlag {

    /**
     * Grunnpensjon for dette konverteringsgrunnlaget
     */
    var gp: Grunnpensjon? = null

    /**
     * Tilleggspensjon for dette konverteringsgrunnlaget
     */
    var tp: Tilleggspensjon? = null

    /**
     * Særtillegg for dette konverteringsgrunnlaget
     * Betinget, hvis uførepensjon er beregnet med særtillegg
     */
    var st: Sertillegg? = null

    constructor() : super() {}

    constructor(kg: Konverteringsgrunnlag) : this() {
        if (kg.gp != null) {
            gp = Grunnpensjon(kg.gp!!)
        }
        if (kg.tp != null) {
            tp = Tilleggspensjon(kg.tp!!)
        }
        if (kg.st != null) {
            st = Sertillegg(kg.st!!)
        }
    }

    constructor(
            gp: Grunnpensjon? = null,
            tp: Tilleggspensjon? = null,
            st: Sertillegg? = null
    ) {
        this.gp = gp
        this.tp = tp
        this.st = st
    }

}
