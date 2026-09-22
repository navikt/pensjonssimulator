package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Dagpengegrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Forstegangstjeneste
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Omsorgsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag

data class Pensjonsopptjening(
    val beholdningListe: List<Pensjonsbeholdning>,
    val opptjeningsgrunnlagListe: List<Opptjeningsgrunnlag>,
    val inntektsgrunnlagListe: List<Inntektsgrunnlag>,
    val dagpengegrunnlagListe: List<Dagpengegrunnlag>,
    val omsorgsgrunnlagListe: List<Omsorgsgrunnlag>,
    val foerstegangstjeneste: Forstegangstjeneste?
) {
    fun utenBeholdninger() =
        copy(beholdningListe = emptyList())

    companion object {
        fun emptyInstance() =
            Pensjonsopptjening(
                beholdningListe = emptyList(),
                opptjeningsgrunnlagListe = emptyList(),
                inntektsgrunnlagListe = emptyList(),
                dagpengegrunnlagListe = emptyList(),
                omsorgsgrunnlagListe = emptyList(),
                foerstegangstjeneste = null
            )
    }
}