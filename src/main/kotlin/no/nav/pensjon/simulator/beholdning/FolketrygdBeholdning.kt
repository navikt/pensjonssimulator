package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.core.domain.regler.enum.GarantiPensjonsnivaSatsEnum
import java.time.LocalDate

data class FolketrygdBeholdning(
    val pensjonBeholdningPeriodeListe: List<BeholdningPeriode> = emptyList()
)

// PEN: no.nav.domain.pensjon.kjerne.simulering.PensjonsbeholdningPeriode
data class BeholdningPeriode(
    val pensjonBeholdning: Int = 0,
    val garantipensjonBeholdning: Int = 0,
    val garantitilleggBeholdning: Int = 0,
    val garantipensjonNivaa: GarantipensjonNivaa,
    val fom: LocalDate
)

// PEN: no.nav.domain.pensjon.kjerne.simulering.Garantipensjonsniva
// with Int instead of Double
data class GarantipensjonNivaa(
    val beloep: Int = 0,
    val satsType: GarantiPensjonsnivaSatsEnum = GarantiPensjonsnivaSatsEnum.ORDINAER,
    val sats: Int = 0,
    val anvendtTrygdetid: Int = 0
)
