package no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl.OpptjeningsperiodeDto
import no.nav.pensjon.simulator.tpregisteret.TpOrdning
import java.util.concurrent.ExecutionException

class OpptjeningsperiodeResponse(
    val tpOrdningOpptjeningsperiodeMap: Map<TpOrdning, List<OpptjeningsperiodeDto>>,
    val exceptions: List<ExecutionException>
)