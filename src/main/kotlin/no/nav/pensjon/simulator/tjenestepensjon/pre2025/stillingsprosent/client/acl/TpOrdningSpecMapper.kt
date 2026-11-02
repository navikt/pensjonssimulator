package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.acl

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.TpOrdningDto
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsordning

object TpOrdningSpecMapper {

    fun toDto(source: Tjenestepensjonsordning) =
        TpOrdningDto(
            navn = source.navn,
            tpNr = source.tpNr,
            datoSistOpptjening = source.datoSistOpptjening,
            tssId = source.tssId
        )
}