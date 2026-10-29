package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.sisteordning

import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsforhold
import org.springframework.stereotype.Service

@Service
class SisteTpOrdningNavService : SisteTpOrdningService {
    override fun finnSisteOrdningKandidater(tpForholdListe: List<Tjenestepensjonsforhold>): List<String> =
        tpForholdListe
            .sortedWith(
                compareByDescending<Tjenestepensjonsforhold> { it.datoSistOpptjening == null }
                    .thenByDescending { it.datoSistOpptjening }
            )
            .map { it.tpNr }
}