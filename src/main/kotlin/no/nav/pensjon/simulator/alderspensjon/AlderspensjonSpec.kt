package no.nav.pensjon.simulator.alderspensjon

import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class AlderspensjonSpec(
    val pid: Pid,
    val gradertUttak: GradertUttakSpec?,
    val heltUttakFom: LocalDate,
    val antallAarUtenlandsEtter16Aar: Int,
    val epsHarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean,
    val fremtidigInntektListe: List<InntektSpec>,
    val rettTilAfpOffentligDato: LocalDate?
)

data class GradertUttakSpec(
    val uttaksgrad: Uttaksgrad,
    val fom: LocalDate
)

data class InntektSpec(
    val aarligBeloep: Int,
    val fom: LocalDate
)
