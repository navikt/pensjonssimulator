package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.result.SimulatorOutputMapper.mapToSimulertOpptjening
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findLatest
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import org.springframework.stereotype.Component

@Component
class SimulertOpptjeningAdder(private val normalderService: NormertPensjonsalderService) {

    // PEN: createAndMapSimulertOpptjeningListe in
    // no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OpprettOutputHelper
    fun addToOpptjeningListe(
        opptjeningListe: MutableList<SimulertOpptjening>,
        beregningsresultatListe: List<AbstraktBeregningsResultat>,
        soekerGrunnlag: Persongrunnlag,
        regelverkType: RegelverkTypeEnum?
    ) {
        if (soekerGrunnlag.opptjeningsgrunnlagListe.isEmpty()) return

        val poengtallListe: List<Poengtall>? =
            sisteAlderspensjonBeregningsresultat2011(regelverkType, beregningsresultatListe)
                ?.beregningsInformasjonKapittel19?.spt?.poengrekke?.poengtallListe

        val foersteKalenderAar: Int = soekerGrunnlag.opptjeningsgrunnlagListe.minByOrNull { it.ar }?.ar ?: return

        // 'Siste kalenderår' er pr. 2025 det året personen fyller 75 år,
        // men det kan bli høyere i framtiden pga. økt pensjonsalder
        val sisteKalenderAar =
            normalderService.oevreAlderOppnaasDato(soekerGrunnlag.fodselsdato!!.toNorwegianLocalDate()).year

        for (aar in foersteKalenderAar..sisteKalenderAar) {
            opptjeningListe.add(
                mapToSimulertOpptjening(
                    kalenderAar = aar,
                    resultatListe = beregningsresultatListe,
                    soekerGrunnlag,
                    poengtallListe = poengtallListe.orEmpty(),
                    useNullAsDefaultPensjonspoeng = poengtallListe == null
                )
            )
        }
    }

    private companion object {

        private fun sisteAlderspensjonBeregningsresultat2011(
            regelverkType: RegelverkTypeEnum?,
            resultatListe: List<AbstraktBeregningsResultat>
        ): BeregningsResultatAlderspensjon2011? =
            when (regelverkType) {
                RegelverkTypeEnum.N_REG_G_OPPTJ -> findLatest(resultatListe) as? BeregningsResultatAlderspensjon2011
                RegelverkTypeEnum.N_REG_G_N_OPPTJ -> (findLatest(resultatListe) as? BeregningsResultatAlderspensjon2016)?.beregningsResultat2011
                else -> null
            }
    }
}
