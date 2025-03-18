package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.time.LocalDate

// 2025-03-18
data class AfpOffentligLivsvarigGrunnlag(
    val sistRegulertG: Int,
    val bruttoPerAr: Double,
    val uttaksdato: LocalDate
)
