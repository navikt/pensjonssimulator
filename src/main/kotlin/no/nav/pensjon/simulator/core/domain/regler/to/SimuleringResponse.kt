package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import java.util.Date

class SimuleringResponse : ServiceResponse() {
    var simuleringsResultat: Simuleringsresultat? = null

    override fun virkFom(): Date? = null

    override fun persons(): String = ""
}
