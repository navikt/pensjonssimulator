package no.nav.pensjon.simulator.uttak.api.acl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import no.nav.pensjon.simulator.person.Pid.Companion.redact
import no.nav.pensjon.simulator.tech.json.Stringifier.listAsString
import no.nav.pensjon.simulator.tech.json.Stringifier.textAsString
import java.time.LocalDate

// This file is part of the anti-corruption layer (ACL) between requests
// from external clients and the internal domain related to 'tidligst mulig uttak'.

/**
 * Version 1 of specification for finding 'tidligst mulig uttak'.
 */
data class TidligstMuligUttakSpecV1(
    val personId: String = "",
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fodselsdato: LocalDate,
    val uttaksgrad: Int? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val heltUttakFraOgMedDato: LocalDate? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val rettTilAfpOffentligDato: LocalDate? = null,
    val fremtidigInntektListe: List<UttakInntektSpecV1>? = null,
    val arIUtlandetEtter16: Int? = null
) {
    /**
     * toString with redacted person ID
     */
    override fun toString() =
        "{ \"personId\": ${textAsString(redact(personId))}, " +
                "\"fodselsdato\": ${textAsString(fodselsdato)}, " +
                "\"uttaksgrad\": $uttaksgrad, " +
                "\"heltUttakFraOgMedDato\": ${textAsString(heltUttakFraOgMedDato)}, " +
                "\"rettTilAfpOffentligDato\": ${textAsString(rettTilAfpOffentligDato)}, " +
                "\"fremtidigInntektListe\": ${listAsString(fremtidigInntektListe)}, " +
                "\"arIUtlandetEtter16\": $arIUtlandetEtter16 }"
}

data class UttakInntektSpecV1(
    val arligInntekt: Int? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate
) {
    override fun toString() =
        "{ \"arligInntekt\": $arligInntekt, " +
                "\"fraOgMedDato\": ${textAsString(fraOgMedDato)} }"
}
