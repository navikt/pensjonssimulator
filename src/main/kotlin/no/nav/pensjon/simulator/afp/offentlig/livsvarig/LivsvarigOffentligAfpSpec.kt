package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class LivsvarigOffentligAfpSpec(
    val pid: Pid,
    val foedselsdato: LocalDate,
    val fom: LocalDate,
    val fremtidigInntektListe: List<Inntekt>
)
