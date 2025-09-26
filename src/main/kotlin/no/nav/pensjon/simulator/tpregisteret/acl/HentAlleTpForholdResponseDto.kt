package no.nav.pensjon.simulator.tpregisteret.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class HentAlleTpForholdResponseDto(
    val fnr: String,
    val forhold: List<TpForholdResponseDto>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TpForholdResponseDto(
    val tpNr: String,
    val tpOrdningNavn: String?,
    val datoSistOpptjening: LocalDate?,
)