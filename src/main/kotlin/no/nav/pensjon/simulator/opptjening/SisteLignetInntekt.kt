package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.person.Pid

interface SisteLignetInntekt {

    fun hentSisteLignetInntekt(pid: Pid): Inntekt
}