package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.LivsvarigOffentligAfpClient
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import org.springframework.stereotype.Service

@Service
class LivsvarigOffentligAfpService(private val client: LivsvarigOffentligAfpClient) {

    fun simuler(spec: LivsvarigOffentligAfpSpec): LivsvarigOffentligAfpResult = client.simuler(spec)
}
