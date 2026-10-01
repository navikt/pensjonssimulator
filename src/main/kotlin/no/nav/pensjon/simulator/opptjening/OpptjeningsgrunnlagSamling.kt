package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Dagpengegrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Forstegangstjeneste
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Omsorgsgrunnlag
import no.nav.pensjon.simulator.person.Pid

data class OpptjeningsgrunnlagSamling(
    val pid: Pid,
    val inntektListe: List<Inntektsgrunnlag>,
    val omsorgListe: List<Omsorgsgrunnlag>,
    val dagpengeListe: List<Dagpengegrunnlag>,
    val foerstegangstjeneste: Forstegangstjeneste?
) {
    companion object {
        fun empty(pid: Pid) =
            OpptjeningsgrunnlagSamling(
                pid,
                inntektListe = emptyList(),
                omsorgListe = emptyList(),
                dagpengeListe = emptyList(),
                foerstegangstjeneste = null
            )
    }
}