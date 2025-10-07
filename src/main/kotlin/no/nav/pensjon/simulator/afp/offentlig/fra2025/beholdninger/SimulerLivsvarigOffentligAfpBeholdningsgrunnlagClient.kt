package no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger

import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec

interface SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient {
    fun simulerAfpBeholdningGrunnlag(spec: LivsvarigOffentligAfpSpec): List<SimulerAfpOffentligLivsvarigBeholdningsperiode>
}
