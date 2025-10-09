package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v4

import no.nav.pensjon.simulator.alderspensjon.AlderspensjonFraFolketrygden
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonResult
import no.nav.pensjon.simulator.alderspensjon.ForslagVedForLavOpptjening
import no.nav.pensjon.simulator.alderspensjon.GradertUttak
import no.nav.pensjon.simulator.alderspensjon.PensjonDelytelse
import no.nav.pensjon.simulator.alderspensjon.PensjonSimuleringStatus
import no.nav.pensjon.simulator.alderspensjon.PensjonType

/**
 * Anti-corruption layer (ACL).
 * Maps from the 'free' internal domain object to version 4 of the externally constrained data transfer object.
 * The object represents a result of the 'simuler alderspensjon' service.
 */
object AlderspensjonResultMapperV4 {

    /**
     * Takes a result in the form of a domain object and maps it to a data transfer object (DTO).
     */
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
            delytelseListe = source.delytelseListe.filter { it.pensjonType != PensjonType.NONE }.map(::delytelse),
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
