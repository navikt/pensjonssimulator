package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn

import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.FolketrygdberegnetAfp
import no.nav.pensjon.simulator.opptjening.AarligOpptjening
import no.nav.pensjon.simulator.validity.Problem

// PSELV: Simuleringsresultat
data class ServiceberegningAfpResult(
    val beregnetAfp: FolketrygdberegnetAfp?,
    val opptjeningListe: List<AarligOpptjening>,
    val problem: Problem? = null
)