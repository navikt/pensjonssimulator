package no.nav.pensjon.simulator.opptjening.client

import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.person.Pid

interface InntektClient {
    fun fetchSistLignedeInntekt(pid: Pid): LoependeInntekt
}