package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdning
import no.nav.pensjon.simulator.core.domain.regler.kode.BeholdningsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GarantipenNivaCti
import java.io.Serializable

class Garantipensjonsbeholdning : Serializable, Beholdning {

    var justertGarantipensjonsniva: JustertGarantipensjonsniva? = null
    var pensjonsbeholdning: Double = 0.0
    var delingstall67: Double = 0.0

    /**
     * Satstype brukt i garantipensjonsnivå.
     */
    var satsType: GarantipenNivaCti? = null

    /**
     * Garantipensjonsnivå sats
     */
    var sats: Double = 0.0

    /**
     * Garantipensjonsnivå justert for trygdetid
     */
    var garPN_tt_anv: Double = 0.0

    /**
     * Garantipensjonsnivå fremskrevet.
     */
    var garPN_justert: Double = 0.0

    constructor() : super() {
        beholdningsType = BeholdningsTypeCti("GAR_PEN_B")
    }

    constructor(garb: Garantipensjonsbeholdning) : super(garb) {
        pensjonsbeholdning = garb.pensjonsbeholdning
        delingstall67 = garb.delingstall67
        sats = garb.sats
        garPN_tt_anv = garb.garPN_tt_anv
        garPN_justert = garb.garPN_justert
        if (garb.satsType != null) {
            satsType = GarantipenNivaCti(garb.satsType)
        }
        if (garb.justertGarantipensjonsniva != null) {
            justertGarantipensjonsniva = JustertGarantipensjonsniva(garb.justertGarantipensjonsniva!!)
        }
    }

    constructor(
        //Local parameters
        justertGarantipensjonsniva: JustertGarantipensjonsniva? = null,
        pensjonsbeholdning: Double = 0.0,
        delingstall67: Double = 0.0,
        satsType: GarantipenNivaCti? = null,
        sats: Double = 0.0,
        garPN_tt_anv: Double = 0.0,
        garPN_justert: Double = 0.0,
            //Beholdning parameters
        beholdningsType:BeholdningsTypeCti = BeholdningsTypeCti("GAR_PEN_B"),
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
        this.justertGarantipensjonsniva = justertGarantipensjonsniva
        this.pensjonsbeholdning = pensjonsbeholdning
        this.delingstall67 = delingstall67
        this.satsType = satsType
        this.sats = sats
        this.garPN_tt_anv = garPN_tt_anv
        this.garPN_justert = garPN_justert
    }
}
