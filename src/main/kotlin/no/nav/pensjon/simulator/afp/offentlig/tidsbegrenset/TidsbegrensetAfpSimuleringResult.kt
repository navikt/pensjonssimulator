package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.opptjening.OpptjeningGrunnlag
import no.nav.pensjon.simulator.validity.Problem

data class TidsbegrensetAfpSimuleringResult(
    val afpOrdning: AFPtypeEnum?,
    val beregnetAfp: FolketrygdberegnetAfp?,
    val opptjeningListe: List<OpptjeningGrunnlag>,
    val problem: Problem? = null
)