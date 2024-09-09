package no.nav.pensjon.simulator.core.domain

import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class Avdoed(
    val pid: Pid,
    val antallAarUtenlands: Int,
    val inntektFoerDoed: Int, // for ALDER_M_GJEN
    val doedDato: LocalDate,
    val erMedlemAvFolketrygden: Boolean = false, // for ALDER_M_GJEN
    val harInntektOver1G: Boolean = false, // for ALDER_M_GJEN
)
