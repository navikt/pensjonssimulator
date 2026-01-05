package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.offentligafp

import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.SimulertOffentligAfp

object OffentligAfpAggregator {

    fun aggregate(pre2025OffentligAfp: Simuleringsresultat?, afpEtterfAlder: Boolean): SimulertOffentligAfp? {
        return if (afpEtterfAlder) {
            pre2025OffentligAfp?.beregning?.let {
                SimulertOffentligAfp(
                    brutto = it.brutto,
                    tidligerePensjonsgivendeInntekt = it.tp?.spt?.poengrekke?.tpi ?: 0
                )
            }
        } else {
            null
        }
    }
}