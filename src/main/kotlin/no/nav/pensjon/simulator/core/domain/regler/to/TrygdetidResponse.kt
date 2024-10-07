package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Trygdetid

class TrygdetidResponse : ServiceResponse() {
    /**
     * Fastsatt trygdetid.
     */
    var trygdetid: Trygdetid? = null

    /**
     * Fastsatt trygdetid for AP2016 iht. kapittel 20 og AP2025.
     */
    var trygdetidKapittel20: Trygdetid? = null

    /**
     * Fastsatt trygdetid for annet uføretidspunkt.
     */
    var trygdetidAlternativ: Trygdetid? = null
}
