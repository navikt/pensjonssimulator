package no.nav.pensjon.simulator.alderspensjon.spec

import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

/**
 * Specification for 'simuler alderspensjon'.
 */
data class AlderspensjonSpec(
    val pid: Pid,
    val gradertUttak: GradertUttakSpec?,
    val heltUttakFom: LocalDate,
    val antallAarUtenlandsEtter16: Int,
    val epsHarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean,
    val fremtidigInntektListe: List<PensjonInntektSpec>,
    val livsvarigOffentligAfpRettFom: LocalDate?
)

// NB: Compare with GradertUttakSpec in TidligstMuligUttakSpec
data class GradertUttakSpec(
    val uttaksgrad: UttakGradKode,
    val fom: LocalDate
)

data class PensjonInntektSpec(
    val aarligBeloep: Int,
    val fom: LocalDate
)
