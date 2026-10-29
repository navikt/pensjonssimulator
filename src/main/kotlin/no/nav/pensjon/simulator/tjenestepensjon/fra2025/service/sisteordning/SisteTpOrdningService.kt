package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.sisteordning

import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsforhold

interface SisteTpOrdningService {
    fun finnSisteOrdningKandidater(tpForholdListe: List<Tjenestepensjonsforhold>): List<String>
}