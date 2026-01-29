package no.nav.pensjon.simulator.person.relasjon.client.pensjonpersondata.acl

import java.time.LocalDate

/**
 * Feltnavn bestemmes av appen pensjon-persondata.
 */
data class PersonParSpecDto(
    val pid: String,
    val annenPid: String,
    val dato: LocalDate
)
