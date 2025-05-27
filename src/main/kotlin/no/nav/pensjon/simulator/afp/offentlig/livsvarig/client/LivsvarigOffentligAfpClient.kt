package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec

interface LivsvarigOffentligAfpClient {

    fun simuler(spec: LivsvarigOffentligAfpSpec): LivsvarigOffentligAfpResult
}
