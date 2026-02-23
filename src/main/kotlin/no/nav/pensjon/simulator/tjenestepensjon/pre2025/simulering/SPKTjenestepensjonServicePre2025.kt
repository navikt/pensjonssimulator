package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening.OpptjeningsperiodeUtil
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.TjenestepensjonClientPre2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.TpOrdning
import org.springframework.stereotype.Service

@Service
class SPKTjenestepensjonServicePre2025(private val tjenestepensjonClient: TjenestepensjonClientPre2025) {
    fun simulerOffentligTjenestepensjon(
        spec: TjenestepensjonSimuleringPre2025Spec,
        stillingsprosentListe: List<Stillingsprosent>,
        tpOrdning: TpOrdning
    ): SimulerOffentligTjenestepensjonResult {
        val opptjeningsperioderVedTpOrdning =
            OpptjeningsperiodeUtil.getOpptjeningsperiodeListe(tpOrdning, stillingsprosentListe)

        val finalSpec = spec.withTpInfo(
            tpNummer = tpOrdning.tpNr,
            tpForholdListe = tpForholdListe(opptjeningsperioderVedTpOrdning)
        )

        return tjenestepensjonClient.getPrognose(finalSpec, tpOrdning.tpNr)
    }

    private fun tpForholdListe(
        opptjeningsperioderVedTpOrdning: Map<TpOrdning, List<Opptjeningsperiode>>
    ): List<TpForhold> =
        opptjeningsperioderVedTpOrdning.map {
            TpForhold(
                tpNr = it.key.tpNr,
                opptjeningsperioder = it.value
            )
        }
}
