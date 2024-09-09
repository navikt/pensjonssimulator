package no.nav.pensjon.simulator.core.domain.regler.gomregning

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag

class PersongrunnlagOmregningGrunnlag(
    var persongrunnlagId: Long? = null,
    var inntektsgrunnlagGrunnlagListe: MutableList<Inntektsgrunnlag> = mutableListOf(),
    var lonnsvekstOmregn: Boolean = false
)
