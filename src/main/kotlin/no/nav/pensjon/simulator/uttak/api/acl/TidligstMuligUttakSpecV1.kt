package no.nav.pensjon.simulator.uttak.api.acl

import java.time.LocalDate

// This file is part of the anti-corruption layer (ACL) between requests
// from external clients and the internal domain related to 'tidligst mulig uttak'.

/**
 * Version 1 of specification for finding 'tidligst mulig uttak'.
 */
data class TidligstMuligUttakSpecV1(
    val personId: String? = null,
    val fodselsdato: LocalDate? = null,
    val uttaksgrad: Int? = null,
    val heltUttakFraOgMedDato: LocalDate? = null,
    val rettTilAfpOffentligDato: LocalDate? = null,
    //val antallAarUtenlandsEtter16Aar: Int? = null,
    val fremtidigInntektListe: List<InntektSpecV1>? = null,
    //val epsHarPensjon: Boolean? = null,
    //val epsHarInntektOver2G: Boolean? = null
)

data class InntektSpecV1(
    val arligInntekt: Int,
    val fraOgMedDato: String
)
