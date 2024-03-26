package no.nav.pensjon.simulator.beholdning

import java.time.LocalDate

data class FolketrygdBeholdning(
    val pensjonBeholdningPeriodeListe: List<BeholdningPeriode> = emptyList()
)

data class BeholdningPeriode(
    val pensjonBeholdning: Int = 0,
    val garantipensjonBeholdning: Int = 0,
    val garantipensjonNivaa: GarantipensjonNivaa,
    val fom: LocalDate
)

data class GarantipensjonNivaa(
    val beloep: Int = 0,
    val satsType: SatsType = SatsType.ORDINAER,
    val sats: Int = 0,
    val anvendtTrygdetid: Int = 0
)
