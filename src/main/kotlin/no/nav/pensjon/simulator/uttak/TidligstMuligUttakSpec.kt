package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class TidligstMuligUttakSpec(
    val pid: Pid,
    val foedselDato: LocalDate,
    val gradertUttak: GradertUttakSpec?,
    val rettTilOffentligAfpFom: LocalDate?,
    val antallAarUtenlandsEtter16Aar: Int,
    val fremtidigInntektListe: List<InntektSpec>,
    val epsHarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean
) {
    fun withHeltUttakFom(dato: LocalDate?) =
        TidligstMuligUttakSpec(
            pid,
            foedselDato,
            gradertUttak = dato?.let { gradertUttak?.withHeltUttakFom(it) },
            rettTilOffentligAfpFom,
            antallAarUtenlandsEtter16Aar,
            fremtidigInntektListe,
            epsHarPensjon,
            epsHarInntektOver2G
        )
}

data class GradertUttakSpec(
    val grad: UttakGrad,
    val heltUttakFom: LocalDate
) {
    fun withHeltUttakFom(dato: LocalDate) =
        GradertUttakSpec(
            grad,
            heltUttakFom = dato
        )
}

data class InntektSpec(
    val fom: LocalDate,
    val aarligBeloep: Int
)
