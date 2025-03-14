package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
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
            alderspensjon = source.alderspensjon?.let(::alderspensjonYtelser),
            afpPrivat = source.afpPrivat?.let(::privatAfpYtelser)
        )

    private fun alderspensjonYtelser(alderspensjon: PenAlderspensjonYtelser): AlderspensjonYtelser {
        return AlderspensjonYtelser(
            sokerVirkningFom = alderspensjon.sokerVirkningFom?.toNorwegianLocalDate(),
            avdodVirkningFom = alderspensjon.avdodVirkningFom?.toNorwegianLocalDate(),
            sisteBeregning = alderspensjon.sisteBeregning,
            alderspensjon.forrigeBeregningsresultat,
            forrigeVilkarsvedtakListe = alderspensjon.forrigeVilkarsvedtakListe.orEmpty().map(::setErHovedkrav)
        )
    }

    private fun privatAfpYtelser(afp: PenPrivatAfpYtelser) =
        PrivatAfpYtelser(
            virkningFom = afp.virkningFom?.toNorwegianLocalDate(),
            forrigeBeregningsresultat = afp.forrigeBeregningsresultat
        )

    private fun setErHovedkrav(vedtak: VilkarsVedtak): VilkarsVedtak {
        vedtak.kravlinje?.let {
            it.hovedKravlinje = it.erHovedkravlinje()
        }

        return vedtak
    }
}
