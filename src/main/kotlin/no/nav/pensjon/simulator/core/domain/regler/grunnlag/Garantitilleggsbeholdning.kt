package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.GarantitilleggInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.BeholdningsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import java.io.Serializable

class Garantitilleggsbeholdning : Serializable, Beholdning {
    var garantitilleggInformasjon: GarantitilleggInformasjon? = null

    constructor() : super() {
        beholdningsType = BeholdningsTypeCti("GAR_T_B")
    }

    constructor(gb: Garantitilleggsbeholdning) : super(gb) {
        if (gb.garantitilleggInformasjon != null) {
            garantitilleggInformasjon = GarantitilleggInformasjon(gb.garantitilleggInformasjon!!)
        }
    }

    constructor(
            //Local parameters
        garantitilleggInformasjon: GarantitilleggInformasjon? = null,
            //Super parametes
        beholdningsType:BeholdningsTypeCti = BeholdningsTypeCti("GAR_T_B"),
        ar: Int = 0,
        totalbelop: Double = 0.0,
        opptjening: Opptjening? = null,
        lonnsvekstInformasjon: LonnsvekstInformasjon? = null,
        reguleringsInformasjon: ReguleringsInformasjon? = null,
        formelkode: FormelKodeCti? = null,
        merknadListe: MutableList<Merknad> = mutableListOf()
    ): super(
            beholdningsType = beholdningsType,
            ar = ar,
            totalbelop = totalbelop,
            opptjening = opptjening,
            lonnsvekstInformasjon = lonnsvekstInformasjon,
            reguleringsInformasjon = reguleringsInformasjon,
            formelkode = formelkode,
            merknadListe = merknadListe
    ) {
        this.garantitilleggInformasjon = garantitilleggInformasjon
    }
}
