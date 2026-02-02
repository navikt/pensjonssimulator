package no.nav.pensjon.simulator.uttak.api.acl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

/**
 * "Anti-corruption data transfer object" (API-diktert objekt isolert fra internt domeneobjekt).
 * Versjonert utgave av spesifikasjon av parametre for å finne tidligst mulig uttak.
 */
data class TidligstMuligUttakSpecV1(
    val personId: String = "",
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fodselsdato: LocalDate,
    val uttaksgrad: Int? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val heltUttakFraOgMedDato: LocalDate? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val rettTilAfpOffentligDato: LocalDate? = null,
    val fremtidigInntektListe: List<UttakInntektSpecV1>? = null,
    val arIUtlandetEtter16: Int? = null
)

data class UttakInntektSpecV1(
    val arligInntekt: Int? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate
)
