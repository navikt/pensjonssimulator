package no.nav.pensjon.simulator.core.domain.regler.gomregning

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag

class PersongrunnlagOmregningResultat(
    var persongrunnlagId: Long? = null,
    var inntektsgrunnlagResultatListe: MutableList<Inntektsgrunnlag> = mutableListOf()
)
