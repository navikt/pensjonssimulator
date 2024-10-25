package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4

import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.*

object AlderspensjonResultMapperV4 {

    fun resultV4(source: AlderspensjonResult) =
        AlderspensjonResultV4(
            simuleringSuksess = source.simuleringSuksess,
            aarsakListeIkkeSuksess = source.aarsakListeIkkeSuksess.map(::status),
            alderspensjon = source.alderspensjon.map(::alderspensjon),
            forslagVedForLavOpptjening = source.forslagVedForLavOpptjening?.let(::forslagVedForLavOpptjening),
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

    private fun forslagVedForLavOpptjening(source: ForslagVedForLavOpptjening) =
        ForslagVedForLavOpptjeningV4(
            gradertUttak = source.gradertUttak?.let(::gradertUttak),
            heltUttakFraOgMedDato = source.heltUttakFom
        )

    private fun gradertUttak(source: GradertUttak) =
        GradertUttakV4(
            fraOgMedDato = source.fom,
            uttaksgrad = source.uttaksgrad.prosentsats
        )
}
