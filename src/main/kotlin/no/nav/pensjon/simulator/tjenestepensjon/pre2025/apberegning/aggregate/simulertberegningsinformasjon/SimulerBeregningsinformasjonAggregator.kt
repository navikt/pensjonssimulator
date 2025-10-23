package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simulertberegningsinformasjon

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alder.Alder.Companion.fromAlder
import no.nav.pensjon.simulator.core.legacy.util.DateUtil
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.Simuleringsdata
import java.time.LocalDate

object SimulerBeregningsinformasjonAggregator {

    fun aggregate(
        foedselsdato: LocalDate,
        foersteUttakDato: LocalDate?,
        simulertBeregningInformasjonListe: List<SimulertBeregningInformasjon>?
    ): List<Simuleringsdata> {
        val datoVedNormAlder: LocalDate = fromAlder(foedselsdato, Alder(67, 0)) //TODO normalder

        val firstSimuleringsdataDato = if (DateUtil.isBeforeByDay(foersteUttakDato, datoVedNormAlder, true)) {
            datoVedNormAlder
        } else {
            foersteUttakDato
        }

        return simulertBeregningInformasjonListe
            ?.filter { info -> DateUtil.isAfterByDay(info.datoFom, firstSimuleringsdataDato, true) }
            ?.map { toSimuleringsdata(it) }
            ?.toList() ?: emptyList()
    }

    fun toSimuleringsdata(info: SimulertBeregningInformasjon) = Simuleringsdata(
        fom = info.datoFom!!,
        andvendtTrygdetid = info.tt_anv_kap19!!,
        poengAarTom1991 = info.pa_f92!!,
        poengAarFom1992 = info.pa_e91!!,
        ufoeregradVedOmregning = info.ufoereGrad,
        basisGrunnpensjon = info.basisGrunnpensjon,
        basisPensjonstillegg = info.basisPensjonstillegg,
        basisTilleggspensjon = info.basisTilleggspensjon,
        delingstallUttak = info.delingstall!!,
        forholdstallUttak = info.forholdstall!!,
        sluttpoengtall = info.spt,
    )
}