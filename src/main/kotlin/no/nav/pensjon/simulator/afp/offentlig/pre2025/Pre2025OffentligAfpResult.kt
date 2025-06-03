package no.nav.pensjon.simulator.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat

// no.nav.service.pensjon.simulering.abstractsimulerapfra2011.BeregnAfpOffentligResponse
data class Pre2025OffentligAfpResult(
    val simuleringResult: Simuleringsresultat?,
    val kravhode: Kravhode
)
