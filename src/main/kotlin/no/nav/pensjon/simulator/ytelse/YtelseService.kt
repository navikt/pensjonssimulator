package no.nav.pensjon.simulator.ytelse

import no.nav.pensjon.simulator.ytelse.client.YtelseClient
import org.springframework.stereotype.Service

@Service
class YtelseService(private val client: YtelseClient) {

    fun getLoependeYtelser(spec: LoependeYtelserSpec): LoependeYtelserResult =
        client.fetchLoependeYtelser(spec)
}
