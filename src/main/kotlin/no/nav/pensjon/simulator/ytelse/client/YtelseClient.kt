package no.nav.pensjon.simulator.ytelse.client

import no.nav.pensjon.simulator.ytelse.LoependeYtelserResult
import no.nav.pensjon.simulator.ytelse.LoependeYtelserSpec

interface YtelseClient {
    fun fetchLoependeYtelser(spec: LoependeYtelserSpec): LoependeYtelserResult
}
