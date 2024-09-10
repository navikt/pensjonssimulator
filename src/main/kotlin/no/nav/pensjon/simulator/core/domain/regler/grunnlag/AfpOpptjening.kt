package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.BeholdningsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti

class AfpOpptjening : Beholdning {

    constructor() : super() {
        beholdningsType = BeholdningsTypeCti("AFP")
    }

    constructor(aAfpOpptjening: AfpOpptjening) : super(aAfpOpptjening)

    constructor(
        beholdningsType: BeholdningsTypeCti = BeholdningsTypeCti("AFP"),
        ar: Int = 0,
        totalbelop: Double = 0.0,
        opptjening: Opptjening? = null,
        lonnsvekstInformasjon: LonnsvekstInformasjon? = null,
        reguleringsInformasjon: ReguleringsInformasjon? = null,
        formelkode: FormelKodeCti? = null,
        merknadListe: MutableList<Merknad> = mutableListOf()
    ) : super(
        beholdningsType = beholdningsType,
        ar = ar,
        totalbelop = totalbelop,
        opptjening = opptjening,
        lonnsvekstInformasjon = lonnsvekstInformasjon,
        reguleringsInformasjon = reguleringsInformasjon,
        formelkode = formelkode,
        merknadListe = merknadListe

    )
}