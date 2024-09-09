package no.nav.pensjon.simulator.core.beregn

import java.time.LocalDate

// no.nav.domain.pensjon.kjerne.simulering.PensjonsbeholdningPeriode
// NB: See also no.nav.pensjon.simulator.beholdning.BeholdningPeriode
data class BeholdningPeriode(
    var datoFom: LocalDate,
    var pensjonsbeholdning: Double?,
    var garantipensjonsbeholdning: Double?,
    var garantitilleggsbeholdning: Double?,
    var garantipensjonsniva: GarantipensjonNivaa?
)

// no.nav.domain.pensjon.kjerne.simulering.Garantipensjonsniva
// NB: See also no.nav.pensjon.simulator.beholdning.GarantipensjonNivaa
data class GarantipensjonNivaa(
    val beloep: Double,
    //val satsType: SatsType = SatsType.ORDINAER,
    val satsType: String,
    val sats: Double,
    val anvendtTrygdetid: Int
)
