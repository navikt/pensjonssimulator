package no.nav.pensjon.simulator.core.ufoere

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.Date

@Service
class UfoereService(
    private val personService: PersonService
) {
    fun ufoerehistorikk(pid: Pid, uttakDato: LocalDate): Uforehistorikk? =
        relevantUfoerehistorikk(pid, uttakDato.toNorwegianDateAtNoon())?.let {
            if (it.containsActualUforeperiode() == true) it else null
        }

    fun hasUfoereperiode(pid: Pid, uttakDato: LocalDate) =
        relevantUfoerehistorikk(pid, uttakDato.toNorwegianDateAtNoon())?.containsActualUforeperiode() == true

    // SimulerPensjonsberegningCommand.getUforehistorikkForPenPerson
    private fun relevantUfoerehistorikk(pid: Pid, uttakDato: Date): Uforehistorikk? =
        personService.person(pid)?.uforehistorikk?.apply {
            uforeperiodeListe = uforeperiodeListe.filter { it.virk?.before(uttakDato) == true }.toMutableList()
        }
}
