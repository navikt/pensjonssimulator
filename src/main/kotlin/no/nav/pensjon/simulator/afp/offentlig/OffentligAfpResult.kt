package no.nav.pensjon.simulator.afp.offentlig

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpResult
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode

data class OffentligAfpResult(
    val pre2025: Pre2025OffentligAfpResult?,
    val livsvarig: LivsvarigOffentligAfpResult?,
    val kravhode: Kravhode
)
