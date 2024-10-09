package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat

class SimuleringResponse : ServiceResponse() {

    var simuleringsResultat: Simuleringsresultat? = null
}
