package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.io.Serializable

class Ventetilleggsgrunnlag(
    /**
     * Ventetilleggprosenten.
     */
    var ventetilleggprosent: Double = 0.0,
    /**
     * Sluttpoengtallet til søker ved 67 år.
     */
    var vt_spt: Double = 0.0,
    /**
     * Sluttpoengtallet til søker ved 67 år relatert til overkompensasjon.
     */
    var vt_opt: Double = 0.0,
    /**
     * Antall poengår til søker ved 67 år.
     */
    var vt_pa: Int = 0,
    /**
     * Anvendt trygdetid til søker ved 67 år.
     */
    var tt_vent: Int = 0
) : Serializable {

    constructor(ventetilleggsgrunnlag: Ventetilleggsgrunnlag) : this() {
        this.ventetilleggprosent = ventetilleggsgrunnlag.ventetilleggprosent
        this.vt_spt = ventetilleggsgrunnlag.vt_spt
        this.vt_opt = ventetilleggsgrunnlag.vt_opt
        this.vt_pa = ventetilleggsgrunnlag.vt_pa
        this.tt_vent = ventetilleggsgrunnlag.tt_vent
    }

}
