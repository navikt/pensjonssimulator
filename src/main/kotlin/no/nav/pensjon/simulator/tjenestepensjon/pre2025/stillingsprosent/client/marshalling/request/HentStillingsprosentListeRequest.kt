package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request

import java.time.LocalDate

data class HentStillingsprosentListeRequest(
    val tssEksternId: String? = null,
    val fnr: FNR? = null,
    val tpnr: String? = null,
    val simuleringsKode: String? = null,
) {
    constructor(fnr: FNR, tpOrdning: TpOrdningDto) : this(
        fnr = fnr,
        tpnr = tpOrdning.tpNr,
        tssEksternId = tpOrdning.tssId,
        simuleringsKode = "AP"
    )
}

data class TpOrdningDto(
    val navn: String,
    val tpNr: String,
    val datoSistOpptjening: LocalDate? = null,
    val tssId: String
)
