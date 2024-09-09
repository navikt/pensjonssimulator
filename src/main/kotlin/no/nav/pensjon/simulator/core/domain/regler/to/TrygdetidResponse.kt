package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import java.util.*

class TrygdetidResponse(
    /**
     * Fastsatt trygdetid.
     */
    var trygdetid: Trygdetid? = null,

    /**
     * Fastsatt trygdetid for AP2016 iht. kapittel 20 og AP2025.
     */
    var trygdetidKapittel20: Trygdetid? = null,

    /**
     * Fastsatt trygdetid for annet uf�retidspunkt.
     */
    var trygdetidAlternativ: Trygdetid? = null,
    override val pakkseddel: Pakkseddel = Pakkseddel()
) : ServiceResponse(pakkseddel) {
    override fun virkFom(): Date? {
        return null
    }

    override fun persons(): String {
        return ""
    }
}
