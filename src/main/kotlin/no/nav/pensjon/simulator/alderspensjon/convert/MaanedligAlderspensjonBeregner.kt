package no.nav.pensjon.simulator.alderspensjon.convert

import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertMaanedligAlderspensjon
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertMaanedligGarantipensjon
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import java.time.LocalDate

class MaanedligAlderspensjonBeregner(
    private val beregningsinfoListe: List<SimulertBeregningInformasjon>,
    private val periodeListe: List<PensjonPeriode>,
    private val kapittel19Andel: Double?,
    private val kapittel20Andel: Double?
) {
    fun maanedligAlderspensjon(fom: LocalDate): SimulertMaanedligAlderspensjon? =
        beregningsinfoListe.firstOrNull { it.datoFom == fom }?.let {
            maanedligAlderspensjon(
                info = it,
                kapittel19Andel,
                kapittel20Andel,
                beholdningFoerUttak = beholdningFoerUttak(periodeListe, fom),
                garantipensjonssats = garantipensjonssats(beregningsinfoListe, periodeListe)
            )
        }

    private companion object {

        private fun maanedligAlderspensjon(
            info: SimulertBeregningInformasjon,
            kapittel19Andel: Double?,
            kapittel20Andel: Double?,
            beholdningFoerUttak: Int?,
            garantipensjonssats: Double?
        ) =
            SimulertMaanedligAlderspensjon(
                beloep = info.maanedligBeloep ?: 0,
                inntektspensjon = info.inntektspensjonPerMaaned,
                delingstall = info.delingstall,
                pensjonBeholdningFoerUttak = beholdningFoerUttak,
                pensjonBeholdningEtterUttak = info.pensjonBeholdningEtterUttak,
                sluttpoengtall = info.spt,
                poengaarFoer92 = info.pa_f92,
                poengaarEtter91 = info.pa_e91,
                forholdstall = info.forholdstall,
                grunnpensjon = info.grunnpensjonPerMaaned,
                tilleggspensjon = info.tilleggspensjonPerMaaned,
                pensjonstillegg = info.pensjonstilleggPerMaaned,
                skjermingstillegg = info.skjermingstilleggPerMaaned,
                andelsbroekKap19 = kapittel19Andel,
                andelsbroekKap20 = kapittel20Andel,
                basispensjon = info.basispensjon,
                restpensjon = info.restBasisPensjon,
                gjenlevendetillegg = info.gjtAPKap19PerMaaned,
                minstePensjonsnivaaSats = info.minstePensjonsnivaSats,
                trygdetidKap19 = info.tt_anv_kap19,
                trygdetidKap20 = info.tt_anv_kap20,
                garantipensjon = info.garantipensjonPerMaaned?.let {
                    SimulertMaanedligGarantipensjon(maanedligBeloep = it, sats = garantipensjonssats ?: 0.0)
                },
                garantitillegg = info.garantitilleggPerMaaned
            )

        private fun garantipensjonssats(
            beregningsinfoListe: List<SimulertBeregningInformasjon>,
            periodeListe: List<PensjonPeriode>
        ): Double? =
            beregningsinfoListe.firstNotNullOfOrNull { it.garantipensjonssats }
                ?: periodeListe
                    .flatMap { it.simulertBeregningInformasjonListe }
                    .firstNotNullOfOrNull { it.garantipensjonssats }

        private fun beholdningFoerUttak(periodeListe: List<PensjonPeriode>, fom: LocalDate): Int? =
            periodeListe
                .flatMap { it.simulertBeregningInformasjonListe }
                .firstOrNull { it.datoFom == fom }
                ?.pensjonBeholdningFoerUttak
    }
}