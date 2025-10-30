package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.ytelse.AlderspensjonYtelser
import no.nav.pensjon.simulator.ytelse.AvdoedYtelser
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

    private fun alderspensjonYtelser(source: PenAlderspensjonYtelser) =
        AlderspensjonYtelser(
            sokerVirkningFom = source.sokerVirkningFom?.toNorwegianLocalDate(),
            avdodVirkningFom = source.avdodVirkningFom?.toNorwegianLocalDate(),
            sisteBeregning = source.sisteBeregning,
            forrigeBeregningsresultat = source.forrigeBeregningsresultat,
            forrigeVilkarsvedtakListe = source.forrigeVilkarsvedtakListe.orEmpty().map(::setErHovedkrav),
            avdoed = source.avdoed?.let(::avdoedYtelser)
        )

    private fun privatAfpYtelser(source: PenPrivatAfpYtelser) =
        PrivatAfpYtelser(
            virkningFom = source.virkningFom?.toNorwegianLocalDate(),
            forrigeBeregningsresultat = source.forrigeBeregningsresultat
        )

    private fun avdoedYtelser(source: PenAvdoedYtelser) =
        AvdoedYtelser(
            pid = source.pid?.let(::Pid) ?: throw EgressException("Missing PID for avdød in PEN response"),
            doedsdato = source.doedsdato?.toNorwegianLocalDate()
                ?: throw EgressException("Missing dødsdato in PEN response"),
            foersteVirkningsdato = source.foersteVirkningsdato?.toNorwegianLocalDate() // may be null
        )

    private fun setErHovedkrav(vedtak: VilkarsVedtak): VilkarsVedtak {
        vedtak.kravlinje?.let {
            it.hovedKravlinje = it.erHovedkravlinje()
        }

        return vedtak
    }
}
