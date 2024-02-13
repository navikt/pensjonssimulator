package no.nav.pensjon.simulator.uttak.client.pen.acl

import java.time.LocalDate

/**
 * Egress-spesifikasjon for å finne 'tidligst mulig uttak' (TMU).
 */
data class PenTidligstMuligUttakSpec(
    val pid: String,
    val foedselDato: LocalDate,
    val uttakGrad: String,
    val rettTilOffentligAfpFom: LocalDate?,
    val antallAarUtenlandsEtter16Aar: Int,
    val fremtidigInntektListe: List<PenTmuInntektSpec>,
    val epsHarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean
)

data class PenTmuInntektSpec(
    val fom: LocalDate,
    val aarligBeloep: Int
)
