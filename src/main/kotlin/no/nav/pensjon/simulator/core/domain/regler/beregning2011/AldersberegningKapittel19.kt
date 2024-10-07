package no.nav.pensjon.simulator.core.domain.regler.beregning2011

class AldersberegningKapittel19 : Beregning2011 {

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
    var ftBenyttetArsakListe: MutableList<FtDtArsak> = mutableListOf()

    constructor() : super() {
        ftBenyttetArsakListe = mutableListOf()
    }

    constructor(source: AldersberegningKapittel19) : super(source) {
        if (source.restpensjon != null) {
            restpensjon = Basispensjon(source.restpensjon!!)
        }

        if (source.basispensjon != null) {
            basispensjon = Basispensjon(source.basispensjon!!)
        }

        if (source.restpensjonUtenGJR != null) {
            restpensjonUtenGJR = Basispensjon(source.restpensjonUtenGJR!!)
        }

        if (source.basispensjonUtenGJR != null) {
            basispensjonUtenGJR = Basispensjon(source.basispensjonUtenGJR!!)
        }

        forholdstall = source.forholdstall
        ftBenyttetArsakListe = mutableListOf()

        for (ftdt in source.ftBenyttetArsakListe) {
            ftBenyttetArsakListe.add(FtDtArsak(ftdt))
        }
    }
}
