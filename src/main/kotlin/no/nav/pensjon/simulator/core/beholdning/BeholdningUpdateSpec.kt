package no.nav.pensjon.simulator.core.beholdning

// no.nav.service.pensjon.beregning.OppdaterPensjonsbeholdningerRequest
class BeholdningUpdateSpec {
    var pensjonBeholdningBeregningGrunnlag: List<BeholdningBeregningsgrunnlag> = emptyList()
    var opptjeningModus: String = ""
    var sisteGyldigeOpptjeningAar: String = ""
    var beregnBeholdningUtenUttak = false
}
