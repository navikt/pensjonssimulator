package no.nav.pensjon.simulator.core.domain.regler.gomregning

class KravFaktoromregningGrunnlag(
    var kravId: Long? = null,
    var persongrunnlagOmregningGrunnlagListe: MutableList<PersongrunnlagOmregningGrunnlag> = mutableListOf()
)
