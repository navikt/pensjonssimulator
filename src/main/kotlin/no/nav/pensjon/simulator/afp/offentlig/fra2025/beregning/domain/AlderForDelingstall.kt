package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain

import no.nav.pensjon.simulator.alder.Alder
import java.time.LocalDate

data class AlderForDelingstall(val alder: Alder, val datoVedAlder: LocalDate)