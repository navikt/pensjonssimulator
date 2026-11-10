package no.nav.pensjon.simulator.krav.client.pen.acl

/**
 * PEN-person DTO (data transfer object) received from PEN.
 * Corresponds to no.nav.pensjon.pen.domain.api.simulator.PenPerson in PEN.
 */
data class PenPenPerson(
    val penPersonId: Long? = 0
)