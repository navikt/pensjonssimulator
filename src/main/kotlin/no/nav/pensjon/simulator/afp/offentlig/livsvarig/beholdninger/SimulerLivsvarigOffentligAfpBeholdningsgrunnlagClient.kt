package no.nav.pensjon.simulator.afp.offentlig.livsvarig.beholdninger

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec

interface SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient {
    fun simuler(spec: LivsvarigOffentligAfpSpec): List<SimulerLivsvarigOffentligAfpBeholdningsperiode>
}
