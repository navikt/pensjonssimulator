package no.nav.pensjon.simulator.person

import no.nav.pensjon.simulator.person.client.GeneralPersonClient
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class GeneralPersonService(private val client: GeneralPersonClient) {

    fun foedselsdato(pid: Pid): LocalDate =
        client.fetchFoedselsdato(pid) ?: throw RuntimeException("Fødselsdato ikke funnet for PID $pid")
}
