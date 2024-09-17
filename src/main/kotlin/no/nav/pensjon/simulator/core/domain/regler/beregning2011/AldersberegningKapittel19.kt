package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningGjelderTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningMetodeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ResultatTypeCti

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

    var forholdstall: Double = 0.0
    var ftBenyttetArsakListe: MutableList<FtDtArsak> = mutableListOf()

    constructor() : super() {
        ftBenyttetArsakListe = mutableListOf()
    }

    constructor(b: AldersberegningKapittel19) : super(b) {

        if (b.restpensjon != null) {
            restpensjon = Basispensjon(b.restpensjon!!)
        }
        if (b.basispensjon != null) {
            basispensjon = Basispensjon(b.basispensjon!!)
        }
        if (b.restpensjonUtenGJR != null) {
            restpensjonUtenGJR = Basispensjon(b.restpensjonUtenGJR!!)
        }
        if (b.basispensjonUtenGJR != null) {
            basispensjonUtenGJR = Basispensjon(b.basispensjonUtenGJR!!)
        }
        forholdstall = b.forholdstall
            ftBenyttetArsakListe = mutableListOf()
            for (ftdt in b.ftBenyttetArsakListe) {
                ftBenyttetArsakListe.add(FtDtArsak(ftdt))
            }
    }

    constructor(
        restpensjon: Basispensjon? = null,
        basispensjon: Basispensjon? = null,
        basispensjonUtenGJR: Basispensjon? = null,
        restpensjonUtenGJR: Basispensjon? = null,
        forholdstall: Double = 0.0,
        ftBenyttetArsakListe: MutableList<FtDtArsak> = mutableListOf(),
        /** super Beregning2011 */
            gjelderPerson: PenPerson? = null,
        grunnbelop: Int = 0,
        tt_anv: Int = 0,
        resultatType: ResultatTypeCti? = null,
        beregningsMetode: BeregningMetodeTypeCti? = null,
        beregningType: BeregningTypeCti? = null,
        delberegning2011Liste: MutableList<BeregningRelasjon> = mutableListOf(),
        merknadListe: MutableList<Merknad> = mutableListOf(),
        beregningGjelderType: BeregningGjelderTypeCti? = null,
        beregningsnavn: String = "Ukjentnavn",
        beregningsrelasjon: BeregningRelasjon? = null,
        delberegning1967: BeregningRelasjon? = null
    ) : super(
            gjelderPerson = gjelderPerson,
            grunnbelop = grunnbelop,
            tt_anv = tt_anv,
            resultatType = resultatType,
            beregningsMetode = beregningsMetode,
            beregningType = beregningType,
            delberegning2011Liste = delberegning2011Liste,
            merknadListe = merknadListe,
            beregningGjelderType = beregningGjelderType,
            beregningsnavn = beregningsnavn,
            beregningsrelasjon = beregningsrelasjon,
            delberegning1967 = delberegning1967) {
        this.restpensjon = restpensjon
        this.basispensjon = basispensjon
        this.basispensjonUtenGJR = basispensjonUtenGJR
        this.restpensjonUtenGJR = restpensjonUtenGJR
        this.forholdstall = forholdstall
        this.ftBenyttetArsakListe = ftBenyttetArsakListe
    }
}
