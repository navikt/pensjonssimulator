package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import no.nav.pensjon.simulator.tpregisteret.TpForhold

interface FinnSisteTpOrdningService {
    fun finnSisteOrdningKandidater(tpOrdninger: List<TpForhold>): List<String>
}