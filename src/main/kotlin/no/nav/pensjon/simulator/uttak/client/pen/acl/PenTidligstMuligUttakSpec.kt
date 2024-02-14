package no.nav.pensjon.simulator.uttak.client.pen.acl

import java.time.LocalDate

/**
 * Egress-spesifikasjon for å finne 'tidligst mulig uttak' (TMU).
 */
data class PenTidligstMuligUttakSpec(
    val pid: String,
    val foedselsdato: LocalDate,
    val gradertUttak: PenTmuGradertUttakSpec?,
    val rettTilOffentligAfpFom: LocalDate?,
    val antallAarUtenlandsEtter16Aar: Int,
    val fremtidigInntektListe: List<PenTmuInntektSpec>,
    val epsHarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean
)

data class PenTmuGradertUttakSpec(
    val grad: String,
    val heltUttakFom: LocalDate
)

data class PenTmuInntektSpec(
    val fom: LocalDate,
    val aarligBeloep: Int
)
