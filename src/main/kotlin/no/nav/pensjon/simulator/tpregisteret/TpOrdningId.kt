package no.nav.pensjon.simulator.tpregisteret

data class TpOrdningId(
    var tssId: String,
    var tpId: String
) {
    fun toTpOrdning(forhold: TpForhold) =
        TpOrdning(
            navn = forhold.navn,
            tpNr = forhold.tpNr,
            datoSistOpptjening = forhold.datoSistOpptjening,
            tssId = this.tssId,
        )
}
