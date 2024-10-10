package no.nav.pensjon.simulator.krav.client

import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode

interface KravClient {
    fun fetchKravhode(kravhodeId: Long): Kravhode
}
