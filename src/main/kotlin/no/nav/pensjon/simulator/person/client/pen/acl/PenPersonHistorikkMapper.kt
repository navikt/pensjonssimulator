package no.nav.pensjon.simulator.person.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.person.Pid

object PenPersonHistorikkMapper {

    fun fromDto(source: Map<String, PenPersonHistorikk>): Map<Pid, PenPerson> =
        source.entries.associateBy({ Pid(it.key) }, { person(it.value) })

    /**
     * NB: Fields are just assigned by reference from the DTO (not mapped to other objects);
     * it is assumed that PEN returns regler-compatible response body
     */
    fun person(source: PenPersonHistorikk) =
        PenPerson(source.penPersonId).apply {
            pid = source.pid?.let(::Pid)
            fodselsdato = source.fodselsdato
            afpHistorikkListe = source.afpHistorikkListe.toMutableList()
            uforehistorikk = source.uforehistorikk
            generellHistorikk = source.generellHistorikk
        }
}
