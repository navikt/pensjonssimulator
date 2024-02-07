package no.nav.pensjon.simulator.uttak.api

// This file contains the anti-corruption layer (ACL) between requests
// from external clients and the internal domain related to 'tidligst mulig uttak'.

/**
 * Version 1 of specification for finding 'tidligst mulig uttak'.
 */
data class TidligstMuligUttakSpecV1(
    val fodselsnummer: String,
    val sivilstandVedPensjonering: String? = null,
    val epsHarPensjon: Boolean? = null,
    val epsHarInntektOver2G: Boolean? = null,
    val antallAarUtenlandsEtter16Aar: Int? = null,
    val simulerMedAfpPrivat: Boolean? = null,
    val fremtidigInntektListe: List<InntektSpecV1>? = null,
    val gradertUttak: GradertUttakSpecV1? = null
)

data class InntektSpecV1(
    val fom: String,
    val aarligBeloep: Int
)

data class GradertUttakSpecV1(
    val gradertUttakFom: String,
    val grad: Int,
    val heltUttakFom: String? = null
)

data class AlderV1(
    val aar: Int,
    val maaneder: Int
)
