package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.offentligafp

import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SimulertOffentligAfp

object OffentligAfpAggregator {

    fun aggregate(
        tidsbegrensetOffentligAfp: Simuleringsresultat?,
        gjelderOffentligAfp: Boolean
    ): SimulertOffentligAfp? =
        if (gjelderOffentligAfp)
            tidsbegrensetOffentligAfp?.beregning?.let(::simulertOffentligAfp)
        else
            null

    private fun simulertOffentligAfp(beregning: Beregning) =
        SimulertOffentligAfp(
            brutto = beregning.brutto,
            tidligerePensjonsgivendeInntekt = beregning.tp?.spt?.poengrekke?.tpi ?: 0
        )
}