package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
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
            alderspensjon = (source.alderspensjon ?: source.ytelser?.alderspensjon)?.let {
                alderspensjonYtelser(
                    alderspensjon = it,
                    extraInfo = source
                )
            },
            afpPrivat = (source.afpPrivat ?: source.ytelser?.afpPrivat)?.let {
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
            sokerVirkningFom = alderspensjon.sokerVirkningFom?.toNorwegianLocalDate(),
            avdodVirkningFom = alderspensjon.avdodVirkningFom?.toNorwegianLocalDate(),
            sisteBeregning = alderspensjon.sisteBeregning,

            forrigeBeregningsresultat = beregningResultatInfo?.let {
                beregningResultat(
                    alderspensjon,
                    it,
                    kapittel19Alderspensjon2011Info!!,
                    kapittel19Alderspensjon2016Info!!,
                    kapittel20AlderspensjonInfo!!
                )
            } ?: alderspensjon.forrigeBeregningsresultat,
            forrigeVilkarsvedtakListe = alderspensjon.forrigeVilkarsvedtakListe.orEmpty()
        )
    }

    private fun beregningResultat(
        alderspensjon: PenAlderspensjonYtelser,
        beregningResultatInfo: PenYtelserExtraInfo,
        kapittel19Alderspensjon2011Info: PenYtelserExtraInfo,
        kapittel19Alderspensjon2016Info: PenYtelserExtraInfo,
        kapittel20AlderspensjonInfo: PenYtelserExtraInfo
    ): AbstraktBeregningsResultat? =
        alderspensjon.forrigeBeregningsresultat?.apply {
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
        }

    private fun privatAfpYtelser(afp: PenPrivatAfpYtelser, extraInfo: PenYtelserExtraInfo?) =
        PrivatAfpYtelser(
            virkningFom = afp.virkningFom?.toNorwegianLocalDate(),

            forrigeBeregningsresultat = extraInfo?.let {
                afp.forrigeBeregningsresultat?.apply {
                    kravId = it.kravId
                    virkTom = it.virkTom
                    epsMottarPensjon = it.epsMottarPensjon
                    epsPaavirkerBeregning = it.epsPaavirkerBeregning
                    harGjenlevenderett = it.harGjenlevenderett
                }
            } ?: afp.forrigeBeregningsresultat
        )
}
