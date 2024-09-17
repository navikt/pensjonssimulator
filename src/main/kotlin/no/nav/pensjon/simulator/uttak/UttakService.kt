package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.uttak.client.UttakClient
import org.springframework.stereotype.Component

@Component
class UttakService(private val client: UttakClient) {

    fun finnTidligstMuligUttak(spec: TidligstMuligUttakSpec): TidligstMuligUttak =
        client.finnTidligstMuligUttak(spec.sanitise())
}
