package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import no.nav.pensjon.simulator.tpregisteret.TpForhold
import org.springframework.stereotype.Service

@Service
class FinnSisteTpOrdningNavService : FinnSisteTpOrdningService {
    override fun finnSisteOrdningKandidater(tpOrdninger: List<TpForhold>): List<String> {
        return tpOrdninger
            .sortedWith(
                compareByDescending<TpForhold> { it.datoSistOpptjening == null }
                    .thenByDescending { it.datoSistOpptjening }
            )
            .map { it.tpNr }
    }

}