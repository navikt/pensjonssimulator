package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.ytelse.AlderspensjonYtelser
import no.nav.pensjon.simulator.ytelse.LoependeYtelserResult
import no.nav.pensjon.simulator.ytelse.PrivatAfpYtelser

object PenLoependeYtelserResultMapper {
    fun fromDto(source: PenLoependeYtelserResult) =
        LoependeYtelserResult(
            alderspensjon = alderspensjonYtelser(source.alderspensjon),
            afpPrivat = privatAfpYtelser(source.afpPrivat),
        )

    private fun alderspensjonYtelser(source: PenAlderspensjonYtelser) =
        AlderspensjonYtelser(
            sokerVirkningFom = source.sokerVirkningFom.toLocalDate(),
            avdodVirkningFom = source.avdodVirkningFom.toLocalDate(),
            sisteBeregning = source.sisteBeregning,
            forrigeBeregningsresultat = source.forrigeBeregningsresultat,
            forrigeVilkarsvedtakListe = source.forrigeVilkarsvedtakListe,
        )

    private fun privatAfpYtelser(source: PenPrivatAfpYtelser) =
        PrivatAfpYtelser(
            virkningFom = source.virkningFom.toLocalDate(),
            forrigeBeregningsresultat = source.forrigeBeregningsresultat,
        )
}
