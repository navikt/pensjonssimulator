package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request

import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto

data class HentStillingsprosentListeRequest(
        val tssEksternId: String? = null,
        val fnr: FNR? = null,
        val tpnr: String? = null,
        val simuleringsKode: String? = null,
) {
    constructor(fnr: FNR, tpOrdning: TpOrdningFullDto) : this(
            fnr = fnr,
            tpnr = tpOrdning.tpNr,
            tssEksternId = tpOrdning.tssId,
            simuleringsKode = "AP"
    )
}

