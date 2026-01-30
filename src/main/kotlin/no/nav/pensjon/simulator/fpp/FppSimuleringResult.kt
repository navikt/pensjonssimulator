package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.afp.offentlig.pre2025.FolketrygdberegnetAfp
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.validity.Problem

data class FppSimuleringResult(
    val afpOrdning: AFPtypeEnum?,
    val beregnetAfp: FolketrygdberegnetAfp?,
    val problem: Problem? = null
)
