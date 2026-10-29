package no.nav.pensjon.simulator.tjenestepensjon.forhold.client.tp.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsforhold
import java.time.LocalDate

/**
 * Korresponderer med SamhandlerPersonDto i tp
 * (github.com/navikt/tp/.../samhandling/tp/controller/dto/SamhandlerPersonDto.kt)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TpForholdResult(
    val fnr: String,
    val forhold: List<TpForhold>
) {
    fun toInternalValue(): List<Tjenestepensjonsforhold> =
        forhold.map { it.toInternalValue() }
}

/**
 * Korresponderer med SamhandlerForholdsinfoDto i tp
 * (github.com/navikt/tp/.../samhandling/tp/controller/dto/SamhandlerForholdDto.kt)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TpForhold(
    val tpNr: String,
    val tpOrdningNavn: String?,
    val datoSistOpptjening: LocalDate?
) {
    fun toInternalValue() =
        Tjenestepensjonsforhold(
            tpNr,
            navn = tpOrdningNavn ?: tpNr,
            datoSistOpptjening
        )
}