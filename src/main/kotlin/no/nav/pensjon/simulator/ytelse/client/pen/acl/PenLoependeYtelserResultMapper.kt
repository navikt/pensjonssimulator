package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.ytelse.AlderspensjonYtelser
import no.nav.pensjon.simulator.ytelse.LoependeYtelserResult
import no.nav.pensjon.simulator.ytelse.PrivatAfpYtelser

/**
 * Maps løpende ytelser from DTO (data transfer object) to pensjonssimulator domain.
 * The DTO is a hybrid of PEN and pensjon-regler properties.
 * This basically performs the inverse mapping of ReglerLoependeYtelserMapper in PEN.
 */
object PenLoependeYtelserResultMapper {

    fun fromDto(source: PenLoependeYtelserResultV1) =
        LoependeYtelserResult(
            alderspensjon = source.ytelser.alderspensjon?.let {
                alderspensjonYtelser(
                    alderspensjon = it,
                    extraInfo = source
                )
            },
            afpPrivat = source.ytelser.afpPrivat?.let {
                privatAfpYtelser(
                    afp = it,
                    extraInfo = source.extraAlderspensjonInfo
                )
            },
        )

    private fun alderspensjonYtelser(
        alderspensjon: PenAlderspensjonYtelser,
        extraInfo: PenLoependeYtelserResultV1
    ): AlderspensjonYtelser {
        val beregningResultatInfo = extraInfo.extraAlderspensjonInfo
        val kapittel19Alderspensjon2011Info = extraInfo.extraKapittel19Alderspensjon2011Info
        val kapittel19Alderspensjon2016Info = extraInfo.extraKapittel19Alderspensjon2016Info
        val kapittel20AlderspensjonInfo = extraInfo.extraKapittel20AlderspensjonInfo

        return AlderspensjonYtelser(
            sokerVirkningFom = alderspensjon.sokerVirkningFom.toLocalDate(),
            avdodVirkningFom = alderspensjon.avdodVirkningFom.toLocalDate(),
            sisteBeregning = alderspensjon.sisteBeregning,

            forrigeBeregningsresultat = alderspensjon.forrigeBeregningsresultat?.apply {
                kravId = beregningResultatInfo.kravId
                virkTom = beregningResultatInfo.virkTom
                epsMottarPensjon = beregningResultatInfo.epsMottarPensjon
                epsPaavirkerBeregning = beregningResultatInfo.epsPaavirkerBeregning
                harGjenlevenderett = beregningResultatInfo.harGjenlevenderett
                (this as? BeregningsResultatAlderspensjon2011)?.beregningsInformasjonKapittel19?.let {
                    it.epsOver2G = kapittel19Alderspensjon2011Info.epsOver2G
                    it.epsMottarPensjon = kapittel19Alderspensjon2011Info.epsMottarPensjon
                }
                ((this as? BeregningsResultatAlderspensjon2016)?.beregningsResultat2011)?.beregningsInformasjonKapittel19?.let {
                    it.epsOver2G = kapittel19Alderspensjon2016Info.epsOver2G
                    it.epsMottarPensjon = kapittel19Alderspensjon2016Info.epsMottarPensjon
                }
                (this as? BeregningsResultatAlderspensjon2025)?.beregningsInformasjonKapittel20?.let {
                    it.epsOver2G = kapittel20AlderspensjonInfo.epsOver2G
                    it.epsMottarPensjon = kapittel20AlderspensjonInfo.epsMottarPensjon
                }
            },
            forrigeVilkarsvedtakListe = alderspensjon.forrigeVilkarsvedtakListe.orEmpty()
        )
    }

    private fun privatAfpYtelser(afp: PenPrivatAfpYtelser, extraInfo: PenYtelserExtraInfo) =
        PrivatAfpYtelser(
            virkningFom = afp.virkningFom.toLocalDate(),

            forrigeBeregningsresultat = afp.forrigeBeregningsresultat?.apply {
                kravId = extraInfo.kravId
                virkTom = extraInfo.virkTom
                epsMottarPensjon = extraInfo.epsMottarPensjon
                epsPaavirkerBeregning = extraInfo.epsPaavirkerBeregning
                harGjenlevenderett = extraInfo.harGjenlevenderett
            }
        )
}
