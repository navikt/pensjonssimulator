package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.afpprivat

import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.SimulertPrivatAfp

object AfpPrivatAggregator {

    fun aggregate(privatAfpPeriodeListe: List<PrivatAfpPeriode>, afpEtterfAlder: Boolean): SimulertPrivatAfp? {
        return if (afpEtterfAlder){
            null
        }
        else {
            privatAfpPeriodeListe
                .minWithOrNull(getComparatorAfpPrivatePeriodeBasedOnAlder())
                ?.let { SimulertPrivatAfp(it.afpOpptjening!!, it.kompensasjonstillegg!!.toDouble()) }
        }
    }

    private fun getComparatorAfpPrivatePeriodeBasedOnAlder(): Comparator<PrivatAfpPeriode> {
        return compareBy(nullsLast(naturalOrder())) { it.alderAar }
    }
}