package no.nav.pensjon.simulator.person

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.person.client.GeneralPersonClient
import no.nav.pensjon.simulator.person.relasjon.PersonPar
import no.nav.pensjon.simulator.person.relasjon.client.PersonRelasjonClient
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class GeneralPersonService(
    private val personaliaClient: GeneralPersonClient,
    private val relasjonClient: PersonRelasjonClient
) {
    fun person(pid: Pid): Person =
        personaliaClient.fetchPerson(pid)
            ?: throw PersonIkkeFunnetException(message = "Person ikke funnet for PID $pid")

    fun foedselsdato(pid: Pid): LocalDate =
        person(pid).foedselsdato
            ?: throw PersonIkkeFunnetException(message = "Fødselsdato ikke funnet for PID $pid")

    fun statsborgerskap(pid: Pid): LandkodeEnum =
        person(pid).statsborgerskap
            ?: throw PersonIkkeFunnetException(message = "Statsborgerskap ikke funnet for PID $pid")

    fun borSammen(personer: PersonPar): Boolean =
        relasjonClient.fetchPersonRelasjonStatus(personer)?.borSammen == true
}
