package no.nav.pensjon.simulator.core.domain.regler.beregning2011

// 2025-03-18
class AldersberegningKapittel19 : Beregning2011() {
    var restpensjon: Basispensjon? = null
    var basispensjon: Basispensjon? = null

    /**
     * Basispensjon regnet uten gjenlevenderettighet.
     */
    var basispensjonUtenGJR: Basispensjon? = null

    /**
     * Restpensjon regnet uten gjenlevenderettighet.
     */
    var restpensjonUtenGJR: Basispensjon? = null
    var forholdstall = 0.0
    var ftBenyttetArsakListe: List<FtDtArsak> = mutableListOf()
}
