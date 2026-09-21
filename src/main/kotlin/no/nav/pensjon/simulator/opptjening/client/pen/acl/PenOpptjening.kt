package no.nav.pensjon.simulator.opptjening.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.opptjening.Pensjonsopptjening
import no.nav.pensjon.simulator.opptjening.client.pen.acl.Pensjonsbeholdning as PenPensjonsbeholdning

/**
 * Corresponds with BeholdningerMedGrunnlagResult in pensjon-pen.
 */
data class PenOpptjening(
    val beholdningListe: List<PenBeholdning>,
    val opptjeningGrunnlagListe: List<Opptjeningsgrunnlag>,
    val inntektGrunnlagListe: List<Inntektsgrunnlag>,
    val dagpengerGrunnlagListe: List<Dagpengegrunnlag>,
    val omsorgGrunnlagListe: List<Omsorgsgrunnlag>,
    val forstegangstjeneste: Forstegangstjeneste?
) {
    fun toInternalValue() =
        Pensjonsopptjening(
            beholdningListe = beholdningListe
                .filter { it.beholdningsTypeEnum == BeholdningtypeEnum.PEN_B }
                .map { (it as PenPensjonsbeholdning).toInternalValue() },
            opptjeningGrunnlagListe = opptjeningGrunnlagListe,
            inntektGrunnlagListe = inntektGrunnlagListe,
            dagpengerGrunnlagListe = dagpengerGrunnlagListe,
            omsorgGrunnlagListe = omsorgGrunnlagListe,
            forstegangstjeneste = forstegangstjeneste
        )
}