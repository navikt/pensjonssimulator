package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Service

@Service
class HentSisteLignetInntektService(
    val opptjeningClient: OpptjeningClient
) {

    fun hentSisteLignetInntekt(pid: Pid): Int {
        return opptjeningClient.hentSisteLignetInntekt(pid).aarligBeloep
    }

}