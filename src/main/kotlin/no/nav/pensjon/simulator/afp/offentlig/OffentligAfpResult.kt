package no.nav.pensjon.simulator.afp.offentlig

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetOffentligAfpResult
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode

data class OffentligAfpResult(
    val tidsbegrenset: TidsbegrensetOffentligAfpResult?,
    val livsvarig: LivsvarigOffentligAfpResult?,
    val kravhode: Kravhode
)