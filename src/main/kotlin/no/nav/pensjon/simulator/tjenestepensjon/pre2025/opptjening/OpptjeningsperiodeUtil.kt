package no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Opptjeningsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.TpOrdning

object OpptjeningsperiodeUtil {

    fun getOpptjeningsperiodeListe(tpOrdning: TpOrdning, stillingsprosentListe: List<Stillingsprosent>) =
        mapOf(tpOrdning to opptjeningsperiodeListe(stillingsprosentListe))

    private fun opptjeningsperiodeListe(
        stillingsprosentListe: List<Stillingsprosent>
    ): List<Opptjeningsperiode> =
        stillingsprosentListe.map {
            Opptjeningsperiode(
                fom = it.datoFom,
                tom = it.datoTom,
                stillingsprosent = it.stillingsprosent,
                aldersgrense = it.aldersgrense,
                faktiskHovedloenn = it.faktiskHovedlonn?.toIntOrNull(),
                stillingsuavhengigTilleggsloenn = it.stillingsuavhengigTilleggslonn?.toIntOrNull()
            )
        }
}