package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.person.Pid

data class OpptjeningsgrunnlagSpec(
    val pid: Pid,
    val grunnlagstypeListe: List<OpptjeningsgrunnlagType>,
    val fomAar: Int? = null,
    val tomAar: Int? = null
)