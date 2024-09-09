package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdninger
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningGjelderTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningMetodeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ResultatTypeCti
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok
import java.io.Serializable

class AldersberegningKapittel20 : Beregning2011, Serializable {
    var delingstall: Double = 0.0
    var beholdninger: Beholdninger? = null
    var dtBenyttetArsakListe: MutableList<FtDtArsak> = mutableListOf()
    /**
     * De faktiske beholdningene som ble brukt i beregningen ved førstegangsuttaket
     */
    var beholdningerForForsteuttak: Beholdninger? = null

    /**
     * Anvendt proratabrøk i trygdeavtaleberegninger.
     */
    var prorataBrok: Brok? = null

    @JsonIgnore
    var avtaleBeregningsmetode: String? = null

    var pensjonUnderUtbetaling: PensjonUnderUtbetaling? = null

    constructor() : super() {
        dtBenyttetArsakListe = mutableListOf()
    }

    constructor(b: AldersberegningKapittel20) : super(b) {
        delingstall = b.delingstall
        if (b.beholdninger != null) {
            beholdninger = Beholdninger(b.beholdninger!!)
        }
        dtBenyttetArsakListe = mutableListOf()
        for (ftdt in b.dtBenyttetArsakListe) {
            dtBenyttetArsakListe.add(FtDtArsak(ftdt))
        }
        if (b.beholdningerForForsteuttak != null) {
            beholdningerForForsteuttak = Beholdninger(b.beholdningerForForsteuttak!!)
        }
        prorataBrok = b.prorataBrok
        pensjonUnderUtbetaling = b.pensjonUnderUtbetaling
        avtaleBeregningsmetode = b.avtaleBeregningsmetode
    }

    constructor(
        delingstall: Double = 0.0,
        beholdninger: Beholdninger? = null,
        dtBenyttetArsakListe: MutableList<FtDtArsak> = mutableListOf(),
        beholdningerForForsteuttak: Beholdninger? = null,
        prorataBrok: Brok? = null,
        avtaleBeregningsmetode: String? = null,
        pensjonUnderUtbetaling: PensjonUnderUtbetaling? = null,
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
        this.delingstall = delingstall
        this.beholdninger = beholdninger
        this.dtBenyttetArsakListe = dtBenyttetArsakListe
        this.beholdningerForForsteuttak = beholdningerForForsteuttak
        this.prorataBrok = prorataBrok
        this.avtaleBeregningsmetode = avtaleBeregningsmetode
        this.pensjonUnderUtbetaling = pensjonUnderUtbetaling
    }
}
