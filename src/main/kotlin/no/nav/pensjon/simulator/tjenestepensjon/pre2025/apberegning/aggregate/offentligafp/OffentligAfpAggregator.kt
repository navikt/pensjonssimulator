package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.offentligafp

import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result.ApForTpBeregningV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result.ApForTpPoengrekkeV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result.ApForTpSluttpoengtallV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result.ApForTpTilleggspensjonV2
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengrekke
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.PEN249KunTilltatMedEnTpiVerdiException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.SimulertOffentligAfp

object OffentligAfpAggregator {

    fun aggregate(pre2025OffentligAfp: Simuleringsresultat?, afpEtterfAlder: Boolean): SimulertOffentligAfp? {
        return if (afpEtterfAlder) {
            pre2025OffentligAfp //TODO forenkle uten mellom-mapping +
                ?.let { beregning(it.beregning!!) }
                ?.let { createSimulertAfpOffentlig(it) }
        } else {
            null
        }
    }

    @Throws(PEN249KunTilltatMedEnTpiVerdiException::class)
    fun createSimulertAfpOffentlig(offentligAfp: ApForTpBeregningV2): SimulertOffentligAfp {

        if (offentligAfp.tilleggspensjonListe?.isEmpty() == true) {
            return SimulertOffentligAfp(brutto = offentligAfp.brutto!!)
        }

        val tpiValueList: List<Int?> = offentligAfp.tilleggspensjonListe
            ?.map { it.spt?.poengrekke?.tpi }
            ?.filter { it != 0 }
            ?.toList() ?: emptyList()

        if (tpiValueList.size == 1) {
            return SimulertOffentligAfp(
                brutto = offentligAfp.brutto!!,
                tidligerePensjonsgivendeInntekt = tpiValueList.first()!!
            )
        } else if (tpiValueList.size > 1) {
            throw PEN249KunTilltatMedEnTpiVerdiException("Should not be able to have more then one TPI value")
        }

        return SimulertOffentligAfp(brutto = offentligAfp.brutto!!)
    }

    private fun beregning(source: Beregning) =
        ApForTpBeregningV2(
            brutto = source.brutto,
            tilleggspensjonListe = source.tp?.let(::tilleggspensjon)?.let(::listOf) //TODO check list
        )

    private fun tilleggspensjon(source: Tilleggspensjon) =
        ApForTpTilleggspensjonV2(
            spt = source.spt?.let(::sluttpoengtall)
        )

    private fun sluttpoengtall(source: Sluttpoengtall) =
        ApForTpSluttpoengtallV2(
            poengrekke = source.poengrekke!!.let(::poengrekke) //TODO null-check
        )

    private fun poengrekke(source: Poengrekke) =
        ApForTpPoengrekkeV2(
            tpi = source.tpi
        )
}