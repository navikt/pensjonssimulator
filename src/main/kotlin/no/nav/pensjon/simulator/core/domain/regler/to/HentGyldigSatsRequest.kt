package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.kode.SatsTypeCti
import java.util.Date

class HentGyldigSatsRequest(
    var fomDato: Date? = null,
    var tomDato: Date? = null,
    var satsType: SatsTypeCti? = null,
    var beregnetSomGift: Boolean = false,
    var forsorgerEPSOver60: Boolean = false,
    var ungUfor: Boolean? = null
) : ServiceRequest() {
    override fun virkFom(): Date? {
        return fomDato
    }

    override fun persons(): String {
        return ""
    }
}
