package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel

abstract class ServiceResponse(
    open var pakkseddel: Pakkseddel = Pakkseddel()
)
