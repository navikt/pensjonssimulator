package no.nav.pensjon.simulator.core.legacy

import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode

data class OutputLegacyPensjon(
    val alderspensjon: OutputLegacyAlderspensjon,
    val privatAfp: List<SimulertPrivatAfpPeriode>,
)
