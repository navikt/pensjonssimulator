package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.ytelse.AlderspensjonYtelser
import no.nav.pensjon.simulator.ytelse.InformasjonOmAvdoed
import no.nav.pensjon.simulator.ytelse.LoependeYtelserResult
import no.nav.pensjon.simulator.ytelse.PrivatAfpYtelser

/**
 * Maps løpende ytelser from DTO (data transfer object) to pensjonssimulator domain.
 * The DTO is a hybrid of PEN and pensjon-regler properties.
 * This basically performs the inverse mapping of ReglerLoependeYtelserMapper in PEN.
 */
object PenLoependeYtelserResultMapper {

    fun fromDto(source: PenLoependeYtelserResult) =
        LoependeYtelserResult(
            alderspensjon = source.alderspensjon?.let(::alderspensjonYtelser),
            afpPrivat = source.afpPrivat?.let(::privatAfpYtelser)
        )

    private fun alderspensjonYtelser(source: PenAlderspensjonYtelser) =
        AlderspensjonYtelser(
            sokerVirkningFom = source.sokerVirkningFom,
            sisteBeregning = source.sisteBeregning,
            forrigeBeregningsresultat = source.forrigeBeregningsresultat,
            forrigeVilkarsvedtakListe = source.forrigeVilkarsvedtakListe.orEmpty().map(::setErHovedkrav),
            avdoed = source.avdoed?.let(::avdoedYtelser)
        )

    private fun privatAfpYtelser(source: PenPrivatAfpYtelser) =
        PrivatAfpYtelser(
            virkningFom = source.virkningFom,
            forrigeBeregningsresultat = source.forrigeBeregningsresultat
        )

    private fun avdoedYtelser(source: PenInformasjonOmAvdoed) =
        InformasjonOmAvdoed(
            pid = source.pid?.let(::Pid),
            doedsdato = source.doedsdato,
            foersteVirkningsdato = source.foersteVirkningsdato,
            aarligPensjonsgivendeInntektErMinst1G = source.aarligPensjonsgivendeInntektErMinst1G,
            harTilstrekkeligMedlemskapIFolketrygden = source.harTilstrekkeligMedlemskapIFolketrygden,
            antallAarUtenlands = source.antallAarUtenlands,
            erFlyktning = source.erFlyktning
        )

    private fun setErHovedkrav(vedtak: VilkarsVedtak): VilkarsVedtak {
        vedtak.kravlinje?.let {
            it.hovedKravlinje = it.erHovedkravlinje()
        }

        return vedtak
    }
}
