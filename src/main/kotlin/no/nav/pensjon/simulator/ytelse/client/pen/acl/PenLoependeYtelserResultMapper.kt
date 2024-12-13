package no.nav.pensjon.simulator.ytelse.client.pen.acl

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
                    source = it,
                    extraInfo = source.extraAlderspensjonInfo
                )
            },
            afpPrivat = source.ytelser.afpPrivat?.let {
                privatAfpYtelser(
                    source = it,
                    extraInfo = source.extraAlderspensjonInfo
                )
            },
        )

    private fun alderspensjonYtelser(source: PenAlderspensjonYtelser, extraInfo: PenYtelserExtraInfo) =
        AlderspensjonYtelser(
            sokerVirkningFom = source.sokerVirkningFom.toLocalDate(),
            avdodVirkningFom = source.avdodVirkningFom.toLocalDate(),
            sisteBeregning = source.sisteBeregning,
            forrigeBeregningsresultat = source.forrigeBeregningsresultat?.apply {
                kravId = extraInfo.kravId
                virkTom = extraInfo.virkTom
                epsMottarPensjon = extraInfo.epsMottarPensjon
                extraInfo.beregningInformasjon?.let {
                    setBeregningsinformasjon(it)
                    hentBeregningsinformasjon()!!.epsOver2G = extraInfo.epsOver2G
                }
            },
            forrigeVilkarsvedtakListe = source.forrigeVilkarsvedtakListe.orEmpty()
        )

    private fun privatAfpYtelser(source: PenPrivatAfpYtelser, extraInfo: PenYtelserExtraInfo) =
        PrivatAfpYtelser(
            virkningFom = source.virkningFom.toLocalDate(),
            forrigeBeregningsresultat = source.forrigeBeregningsresultat?.apply {
                kravId = extraInfo.kravId
                virkTom = extraInfo.virkTom
                epsMottarPensjon = extraInfo.epsMottarPensjon
                extraInfo.beregningInformasjon?.let {
                    setBeregningsinformasjon(it)
                    hentBeregningsinformasjon()!!.epsOver2G = extraInfo.epsOver2G
                }
            }
        )
}
