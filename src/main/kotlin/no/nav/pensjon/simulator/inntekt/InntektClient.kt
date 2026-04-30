package no.nav.pensjon.simulator.inntekt

import no.nav.pensjon.simulator.person.Pid

interface InntektClient {
    fun fetchSistLignedeInntekt(pid: Pid): LoependeInntekt
}