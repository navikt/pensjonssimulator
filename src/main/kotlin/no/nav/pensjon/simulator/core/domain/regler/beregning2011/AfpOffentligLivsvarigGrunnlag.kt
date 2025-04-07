package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate

// 2025-04-06
data class AfpOffentligLivsvarigGrunnlag(
    val sistRegulertG: Int,
    val bruttoPerAr: Double,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val uttaksdato: LocalDate? = null,

    //--- Extra:
    @JsonIgnore
    var virkTom: LocalDate? = null
)
