package no.nav.pensjon.simulator.core.domain.regler.gomregning

import no.nav.pensjon.simulator.core.domain.regler.BatchStatus

class KravFaktoromregningResultat(
    var kravId: Long? = null,
    var batchStatus: BatchStatus? = null,
    var persongrunnlagOmregningResultatListe: MutableList<PersongrunnlagOmregningResultat> = mutableListOf()
) {
    fun beregningResultatListe(): Array<PersongrunnlagOmregningResultat> {
        return persongrunnlagOmregningResultatListe.toTypedArray()
    }
}
