package no.nav.pensjon.simulator.core.person.eps

import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import java.util.EnumSet

/**
 * Utility functions for EPS (ektefelle/partner/samboer).
 */
object EpsUtil {
    // VilkarsprovOgBeregnAlderHelper.isEktefelleMottarPensjon
    fun epsMottarPensjon(spec: SimuleringSpec): Boolean {
        if (gjelderGjenlevenderett(spec.type)) {
            return false
        }

        //TODO check this statement:
        // "The sivilstandsjekk is needed because ESB default sets epsPensjon to true when a samhandler is calling the simulering service"
        return if (erEps(spec.sivilstatus))
            spec.epsHarPensjon
        else
            false
    }

    // VilkarsprovOgBeregnAlderHelper.setEpsMottarPensjonOnForrigeBeregningsresultat
    fun setEpsMottarPensjon(resultat: AbstraktBeregningsResultat?, spec: SimuleringSpec) {
        if (!epsMottarPensjon(spec)) return

        when (resultat) {
            is BeregningsResultatAlderspensjon2011 -> {
                resultat.beregningsInformasjonKapittel19?.let { it.epsMottarPensjon = true }
            }

            is BeregningsResultatAlderspensjon2016 -> {
                resultat.beregningsResultat2011?.beregningsInformasjonKapittel19?.let {
                    it.epsMottarPensjon = true
                }

                resultat.beregningsResultat2025?.beregningsInformasjonKapittel20?.let {
                    it.epsMottarPensjon = true
                }
            }

            is BeregningsResultatAlderspensjon2025 -> {
                resultat.beregningsInformasjonKapittel20?.let { it.epsMottarPensjon = true }
            }
        }
    }

    fun erEps(sivilstatus: SivilstatusType) =
        EnumSet.of(SivilstatusType.GIFT, SivilstatusType.REPA, SivilstatusType.SAMB).contains(sivilstatus)

    fun gjelderGjenlevenderett(simuleringType: SimuleringTypeEnum) =
        EnumSet.of(SimuleringTypeEnum.ALDER_M_GJEN, SimuleringTypeEnum.ENDR_ALDER_M_GJEN).contains(simuleringType)
}
