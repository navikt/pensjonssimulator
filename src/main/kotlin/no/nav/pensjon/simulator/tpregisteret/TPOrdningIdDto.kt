package no.nav.pensjon.simulator.tpregisteret

data class TPOrdningIdDto(
    var tssId: String, var tpId: String
) {

    fun mapTilTpOrdningFullDto(dto: TpForhold): TpOrdningFullDto {
        return TpOrdningFullDto(
            navn = dto.navn,
            tpNr = dto.tpNr,
            datoSistOpptjening = dto.datoSistOpptjening,
            tssId = this.tssId,
        )
    }
}
