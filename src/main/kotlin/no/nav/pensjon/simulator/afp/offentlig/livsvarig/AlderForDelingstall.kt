package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import no.nav.pensjon.simulator.core.domain.regler.Alder
import java.time.LocalDate

data class AlderForDelingstall(val alder: Alder, val datoVedAlder: LocalDate)
