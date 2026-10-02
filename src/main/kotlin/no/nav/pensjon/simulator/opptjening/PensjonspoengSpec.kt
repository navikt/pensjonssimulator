package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.person.Pid

data class PensjonspoengSpec(
    val pid: Pid,
    val pensjonspoengType: OpptjeningtypeEnum? = null,
    val fomAar: Int? = null,
    val tomAar: Int? = null
)