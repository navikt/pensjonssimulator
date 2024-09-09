package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpOpptjening
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningGjelderTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningMetodeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ResultatTypeCti

class AfpPrivatBeregning : Beregning2011 {
    var afpLivsvarig: AfpLivsvarig? = null
    var afpKompensasjonstillegg: AfpKompensasjonstillegg? = null
    var afpKronetillegg: AfpKronetillegg? = null
    var afpOpptjening: AfpOpptjening? = null

    constructor(
        afpLivsvarig: AfpLivsvarig? = null,
        afpKompensasjonstillegg: AfpKompensasjonstillegg? = null,
        afpKronetillegg: AfpKronetillegg? = null,
        afpOpptjening: AfpOpptjening? = null,
        /** super beregning2011 */
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
        delberegning1967 = delberegning1967
    ) {
        this.afpLivsvarig = afpLivsvarig
        this.afpKompensasjonstillegg = afpKompensasjonstillegg
        this.afpKronetillegg = afpKronetillegg
        this.afpOpptjening = afpOpptjening
    }

    constructor(aAfpPrivatBeregning: AfpPrivatBeregning) : super(aAfpPrivatBeregning) {
        if (aAfpPrivatBeregning.afpLivsvarig != null) {
            afpLivsvarig = AfpLivsvarig(aAfpPrivatBeregning.afpLivsvarig!!)
        }
        if (aAfpPrivatBeregning.afpKompensasjonstillegg != null) {
            afpKompensasjonstillegg = AfpKompensasjonstillegg(aAfpPrivatBeregning.afpKompensasjonstillegg!!)
        }
        if (aAfpPrivatBeregning.afpKronetillegg != null) {
            afpKronetillegg = AfpKronetillegg(aAfpPrivatBeregning.afpKronetillegg!!)
        }
        if (aAfpPrivatBeregning.afpOpptjening != null) {
            afpOpptjening = AfpOpptjening(aAfpPrivatBeregning.afpOpptjening!!)
        }
    }

    constructor() : super()
}
