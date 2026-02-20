package no.nav.pensjon.simulator.tpregisteret

data class TPOrdningIdDto(
    var tssId: String,
    var tpId: String
) {
    fun mapTilTpOrdningFull(dto: TpForhold) =
        TpOrdning(
            navn = dto.navn,
            tpNr = dto.tpNr,
            datoSistOpptjening = dto.datoSistOpptjening,
            tssId = this.tssId,
        )
}
