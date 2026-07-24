package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat

data class TidsbegrensetOffentligAfpResult(
    val simuleringResult: Simuleringsresultat?,
    val kravhode: Kravhode
)