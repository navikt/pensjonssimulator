package no.nav.pensjon.simulator.core.domain.regler.gomregning

import no.nav.pensjon.simulator.core.domain.regler.BatchStatus
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning

class BeregningerFaktoromregningResultat(
    var vedtakId: Long? = null,
    var batchStatus: BatchStatus? = null,
    var beregningResultatListe: MutableList<Beregning> = mutableListOf()
)
