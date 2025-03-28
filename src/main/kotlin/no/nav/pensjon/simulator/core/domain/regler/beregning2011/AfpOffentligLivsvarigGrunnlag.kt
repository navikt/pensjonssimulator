package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

// 2025-03-19
data class AfpOffentligLivsvarigGrunnlag(
    val sistRegulertG: Int,
    val bruttoPerAr: Double,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val uttaksdato: LocalDate? = null
)
