package no.nav.pensjon.simulator.person.relasjon.client.pensjonpersondata.acl

import no.nav.pensjon.simulator.person.relasjon.PersonRelasjonStatus

/**
 * Anti-corruption.
 * Isolerer eksterne feltnavn (definert av pensjon-persondata) fra interne feltnavn i domenet.
 */
object SamboerStatusResultMapper {

    fun fromDto(source: SamboerStatusResultDto) =
        PersonRelasjonStatus(borSammen = source.status)
}
