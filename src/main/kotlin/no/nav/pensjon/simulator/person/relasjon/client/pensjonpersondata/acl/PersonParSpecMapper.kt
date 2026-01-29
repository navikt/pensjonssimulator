package no.nav.pensjon.simulator.person.relasjon.client.pensjonpersondata.acl

import no.nav.pensjon.simulator.person.relasjon.PersonPar

/**
 * Anti-corruption.
 * Isolerer eksterne feltnavn (definert av pensjon-persondata) fra interne feltnavn i domenet.
 */
object PersonParSpecMapper {

    fun dto(source: PersonPar) =
        PersonParSpecDto(
            pid = source.pid1.value,
            annenPid = source.pid2.value,
            dato = source.dato
        )
}
