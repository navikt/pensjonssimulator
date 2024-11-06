package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult

interface LivsvarigOffentligAfpClient {

    fun simuler(spec: LivsvarigOffentligAfpSpec): LivsvarigOffentligAfpResult
}
