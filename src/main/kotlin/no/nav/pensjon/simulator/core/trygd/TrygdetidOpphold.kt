package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.TrygdetidsgrunnlagWithArbeid
data class TrygdetidOpphold (
    val periode: TTPeriode,
    val arbeidet: Boolean
)
