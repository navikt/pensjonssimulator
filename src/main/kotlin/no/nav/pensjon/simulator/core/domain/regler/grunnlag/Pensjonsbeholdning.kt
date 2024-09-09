package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.BeholdningsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import java.io.Serializable
import java.util.*

class Pensjonsbeholdning : Beholdning, Serializable {

    @JsonIgnore var fom: Date? = null // SIMDOM-ADD
    @JsonIgnore var tom: Date? = null // SIMDOM-ADD

    constructor() : super() {
        beholdningsType = BeholdningsTypeCti("PEN_B")
    }

    constructor(pb: Pensjonsbeholdning) : super(pb) {
        fom = pb.fom
        tom = pb.tom
    }

    constructor(
        beholdningsType: BeholdningsTypeCti = BeholdningsTypeCti("PEN_B"),
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
