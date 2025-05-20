package no.nav.pensjon.simulator.person

import no.nav.pensjon.simulator.generelt.GenerelleDataSpec
import no.nav.pensjon.simulator.generelt.client.GenerelleDataClient
import no.nav.pensjon.simulator.person.client.GeneralPersonClient
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class GeneralPersonService(private val client: GeneralPersonClient, private val client2: GenerelleDataClient) {

    fun foedselsdato(pid: Pid): LocalDate =
        //client.fetchFoedselsdato(pid) ?: throw RuntimeException("Fødselsdato ikke funnet for PID $pid")
        client2.fetchGenerelleData(GenerelleDataSpec.forPerson(pid)).person.foedselDato
}
