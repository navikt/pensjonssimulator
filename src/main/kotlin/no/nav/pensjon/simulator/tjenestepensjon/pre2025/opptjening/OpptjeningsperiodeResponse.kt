package no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.OpptjeningsperiodeDto
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import java.util.concurrent.ExecutionException

class OpptjeningsperiodeResponse(
    val tpOrdningOpptjeningsperiodeMap: Map<TpOrdningFullDto, List<OpptjeningsperiodeDto>>,
    val exceptions: List<ExecutionException>
)