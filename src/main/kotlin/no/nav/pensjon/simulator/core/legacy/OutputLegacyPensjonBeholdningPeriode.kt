package no.nav.pensjon.simulator.core.legacy

import java.util.*

data class OutputLegacyPensjonBeholdningPeriode (
     val pensjonBeholdning: Double? = null,
     val garantipensjonBeholdning: Double? = null,
     val garantitilleggBeholdning: Double? = null,
     val datoFom: Date? = null,
     val garantipensjonNivaa: OutputLegacyGarantipensjonNivaa? = null
)
