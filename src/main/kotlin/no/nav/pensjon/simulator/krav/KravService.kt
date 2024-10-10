package no.nav.pensjon.simulator.krav

import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.krav.client.KravClient
import org.springframework.stereotype.Service

@Service
class KravService(private val client: KravClient) {

    fun fetchKravhode(kravhodeId: Long): Kravhode = client.fetchKravhode(kravhodeId)
}
