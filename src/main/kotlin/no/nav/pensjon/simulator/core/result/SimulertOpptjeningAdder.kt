package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.result.SimulertOpptjeningMapper.simulertOpptjening
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findLatest
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import org.springframework.stereotype.Component

@Component
class SimulertOpptjeningAdder(private val normalderService: NormertPensjonsalderService) {

    // PEN: createAndMapSimulertOpptjeningListe in
    // no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OpprettOutputHelper
    fun addToOpptjeningListe(
        soekerGrunnlag: Persongrunnlag,
        opptjeningListe: MutableList<SimulertOpptjening>,
        beregningsresultatListe: List<AbstraktBeregningsResultat>,
        regelverkType: RegelverkTypeEnum?
    ) {
        if (soekerGrunnlag.opptjeningsgrunnlagListe.isEmpty()) return

        val foersteKalenderAar: Int = soekerGrunnlag.opptjeningsgrunnlagListe.minByOrNull { it.ar }?.ar ?: return

        val sisteKalenderAar: Int = soekerGrunnlag.fodselsdatoLd?.let {
            normalderService.oevreAlderOppnaasDato(foedselsdato = it).year
        } ?: foersteKalenderAar

        val poengtallListe: List<Poengtall>? =
            sisteAlderspensjonBeregningsresultat2011(regelverkType, beregningsresultatListe)
                ?.beregningsInformasjonKapittel19?.spt?.poengrekke?.poengtallListe

        val sistePensjonsbeholdningPerAar = soekerGrunnlag.sistePensjonsbeholdningPerAar()

        for (aar in foersteKalenderAar..sisteKalenderAar) {
            opptjeningListe.add(
                simulertOpptjening(
                    aar,
                    soekerGrunnlag,
                    resultatListe = beregningsresultatListe,
                    poengtallListe = poengtallListe.orEmpty(),
                    sistePensjonsbeholdningPerAar
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