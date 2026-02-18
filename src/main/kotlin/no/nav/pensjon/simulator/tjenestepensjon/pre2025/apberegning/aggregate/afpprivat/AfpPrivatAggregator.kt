package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.afpprivat

import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SimulertPrivatAfp

object AfpPrivatAggregator {

    fun aggregate(afpPeriodeListe: List<PrivatAfpPeriode>, gjelderOffentligAfp: Boolean): SimulertPrivatAfp? =
        if (gjelderOffentligAfp)
            null
        else
            afpPeriodeListe.minWithOrNull(comparePerioderBasedOnAlder())?.let(::simulertPrivatAfp)

    private fun simulertPrivatAfp(periode: PrivatAfpPeriode) =
        SimulertPrivatAfp(
            totalAfpBeholdning = periode.afpOpptjening!!,
            kompensasjonstillegg = periode.kompensasjonstillegg!!.toDouble()
        )

    private fun comparePerioderBasedOnAlder(): Comparator<PrivatAfpPeriode> =
        compareBy(nullsLast(naturalOrder())) { it.alderAar }
}