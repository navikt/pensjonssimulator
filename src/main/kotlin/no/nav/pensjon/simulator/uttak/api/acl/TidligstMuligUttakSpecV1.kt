package no.nav.pensjon.simulator.uttak.api.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

// This file is part of the anti-corruption layer (ACL) between requests
// from external clients and the internal domain related to 'tidligst mulig uttak'.

/**
 * Version 1 of specification for finding 'tidligst mulig uttak'.
 */
data class TidligstMuligUttakSpecV1(
    val personId: String = "",
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fodselsdato: LocalDate,
    val uttaksgrad: Int? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val heltUttakFraOgMedDato: LocalDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val rettTilAfpOffentligDato: LocalDate? = null,
    //val antallAarUtenlandsEtter16Aar: Int? = null,
    val fremtidigInntektListe: List<UttakInntektSpecV1>? = null
    //val epsHarPensjon: Boolean? = null,
    //val epsHarInntektOver2G: Boolean? = null
)

data class UttakInntektSpecV1(
    val arligInntekt: Int? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate
)
