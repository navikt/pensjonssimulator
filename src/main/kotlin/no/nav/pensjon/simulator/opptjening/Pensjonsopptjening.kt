package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Dagpengegrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Forstegangstjeneste
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Omsorgsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag

data class Pensjonsopptjening(
    val beholdningListe: List<Pensjonsbeholdning>,
    val opptjeningGrunnlagListe: List<Opptjeningsgrunnlag>,
    val inntektGrunnlagListe: List<Inntektsgrunnlag>,
    val dagpengerGrunnlagListe: List<Dagpengegrunnlag>,
    val omsorgGrunnlagListe: List<Omsorgsgrunnlag>,
    val forstegangstjeneste: Forstegangstjeneste?
) {
    fun utenBeholdninger() =
        copy(beholdningListe = emptyList())

    companion object {
        fun emptyInstance() =
            Pensjonsopptjening(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
    }
}