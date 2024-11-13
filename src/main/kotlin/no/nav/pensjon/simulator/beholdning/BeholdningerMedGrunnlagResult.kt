package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*

/**
 * Corresponds with BeholdningerMedGrunnlagResult in PEN.
 * Any changes to this class must be aligned with PEN.
 */
data class BeholdningerMedGrunnlagResult(
    val beholdningListe: List<Beholdning>,
    val opptjeningGrunnlagListe: List<Opptjeningsgrunnlag>,
    val inntektGrunnlagListe: List<Inntektsgrunnlag>,
    val dagpengerGrunnlagListe: List<Dagpengegrunnlag>,
    val omsorgGrunnlagListe: List<Omsorgsgrunnlag>,
    val foerstegangstjeneste: Forstegangstjeneste?
)
