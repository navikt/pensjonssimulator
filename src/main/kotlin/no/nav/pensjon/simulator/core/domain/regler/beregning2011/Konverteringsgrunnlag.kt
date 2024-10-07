package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Sertillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.Grunnpensjon
import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon

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
}
