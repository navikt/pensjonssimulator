package no.nav.pensjon.simulator.core.ufoere

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UfoereService(
    private val personService: PersonService
) {
    fun ufoerehistorikk(pid: Pid, uttakDato: LocalDate): Uforehistorikk? =
        relevantUfoerehistorikk(pid, uttakDato)?.let {
            if (it.containsActualUforeperiode()) it else null
        }

    fun hasUfoereperiode(pid: Pid, uttakDato: LocalDate) =
        relevantUfoerehistorikk(pid, uttakDato)?.containsActualUforeperiode() == true

    // SimulerPensjonsberegningCommand.getUforehistorikkForPenPerson
    private fun relevantUfoerehistorikk(pid: Pid, uttakDato: LocalDate): Uforehistorikk? =
        personService.person(pid)?.uforehistorikk?.apply {
            uforeperiodeListe = uforeperiodeListe.filter { it.virkLd?.isBefore(uttakDato) == true }.toMutableList()
        }
}
