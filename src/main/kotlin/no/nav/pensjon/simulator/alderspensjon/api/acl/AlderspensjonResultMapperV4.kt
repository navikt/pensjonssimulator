package no.nav.pensjon.simulator.alderspensjon.api.acl

import no.nav.pensjon.simulator.alderspensjon.*

object AlderspensjonResultMapperV4 {

    fun resultV4(source: AlderspensjonResult) =
        AlderspensjonResultV4(
            simuleringSuksess = source.simuleringSuksess,
            aarsakListeIkkeSuksess = source.aarsakListeIkkeSuksess.map(::status),
            alderspensjon = source.alderspensjon.map(::alderspensjon),
            alternativerVedForLavOpptjening = source.alternativerVedForLavOpptjening?.let(::alternativer),
            harUttak = source.harUttak
        )

    private fun alderspensjon(source: AlderspensjonFraFolketrygden) =
        AlderspensjonFraFolketrygdenV4(
            fraOgMedDato = source.fom,
            delytelseListe = source.delytelseListe.map(::delytelse),
            uttaksgrad = source.uttaksgrad.prosentsats
        )

    private fun delytelse(source: PensjonDelytelse) =
        PensjonDelytelseV4(
            pensjonsType = PensjonTypeV4.fromInternalValue(source.pensjonType).externalValue,
            belop = source.beloep
        )

    private fun status(source: PensjonSimuleringStatus) =
        PensjonSimuleringStatusV4(
            statusKode = PensjonSimuleringStatusKodeV4.fromInternalValue(source.statusKode).externalValue,
            statusBeskrivelse = source.statusBeskrivelse
        )

    private fun alternativer(source: PensjonAlternativerVedForLavOpptjening) =
        PensjonAlternativerVedForLavOpptjeningV4(
            alderspensjonVedTidligstMuligUttak = source.alderspensjonVedTidligstMuligUttak.map(::alderspensjon),
            alderspensjonVedHoyestMuligGrad = source.alderspensjonVedHoeyestMuligGrad.map(::alderspensjon)
        )
}
