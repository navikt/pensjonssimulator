package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat

// 2026-04-07
class SimuleringResponse : ServiceResponse() {

    var simuleringsResultat: Simuleringsresultat? = null
}
