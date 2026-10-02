package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Dagpengegrunnlag

data class PoppDagpenger(
    val ar: Int? = null,
    val dagpengerType: String? = null, // DagpengetypeEnum
    val utbetalteDagpenger: Int? = null,
    val uavkortetDagpengegrunnlag: Int? = null,
    val ferietillegg: Int? = null,
    val barnetillegg: Int? = null
) {
    fun toGrunnlag() =
        Dagpengegrunnlag().also {
            it.ar = ar ?: 0
            it.dagpengetypeEnum = dagpengerType?.let(::enumValueOf)
            it.utbetalteDagpenger = utbetalteDagpenger ?: 0
            it.uavkortetDagpengegrunnlag = uavkortetDagpengegrunnlag ?: 0
            it.ferietillegg = ferietillegg ?: 0
            it.barnetillegg = barnetillegg ?: 0
        }
}