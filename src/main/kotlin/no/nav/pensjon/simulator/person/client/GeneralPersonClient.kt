package no.nav.pensjon.simulator.person.client

import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

interface GeneralPersonClient {
    fun fetchFoedselsdato(pid: Pid): LocalDate?
}
