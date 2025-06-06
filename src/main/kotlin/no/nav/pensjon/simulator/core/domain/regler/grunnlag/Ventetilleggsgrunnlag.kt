package no.nav.pensjon.simulator.core.domain.regler.grunnlag

// 2025-06-06
class Ventetilleggsgrunnlag {
    /**
     * Ventetilleggprosenten.
     */
    var ventetilleggprosent: Double? = null

    /**
     * Sluttpoengtallet til søker ved 67 år.
     */
    var vt_spt: Double? = null

    /**
     * Sluttpoengtallet til søker ved 67 år relatert til overkompensasjon.
     */
    var vt_opt: Double? = null

    /**
     * Antall poengår til søker ved 67 år.
     */
    var vt_pa: Int? = null

    /**
     * Anvendt trygdetid til søker ved 67 år.
     */
    var tt_vent: Int? = null
}
