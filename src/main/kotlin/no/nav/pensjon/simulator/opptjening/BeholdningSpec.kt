package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class BeholdningSpec(
    val pid: Pid,
    val beholdningType: BeholdningtypeEnum,
    val serviceDirektiv: String,
    val fom: LocalDate? = null,
    val tom: LocalDate? = null
)