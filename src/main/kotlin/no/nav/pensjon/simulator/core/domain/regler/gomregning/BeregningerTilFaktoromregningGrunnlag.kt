package no.nav.pensjon.simulator.core.domain.regler.gomregning

import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning

class BeregningerTilFaktoromregningGrunnlag(
    var vedtakId: Long? = null,
    var beregningGrunnlagListe: MutableList<Beregning> = mutableListOf()
)
