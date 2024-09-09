package no.nav.pensjon.simulator.core.beholdning

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.person.Pid

/**
 * Inneholder all nødvendig informasjon for beregning av pensjonsbeholdning.
 * Objektet holder på et fødselsnr, liste av beholdninger samt grunnlag for inntekt, omsorg,
 * dagpenger og forstegangstjeneste. Objektet brukes for å kunne sende inn en liste av disse fra POPP som
 * sendes videre til PREG ved beregning av pensjonsbeholdning.
 */
// no.nav.domain.pensjon.kjerne.grunnlag.BeregningsgrunnlagForPensjonsbeholdning
class BeholdningBeregningsgrunnlag {
    var pid: Pid? = null
    var beholdning: Pensjonsbeholdning? = null
    var foerstegangstjenesteGrunnlag: Forstegangstjeneste? = null
    var dagpengerGrunnlagListe: List<Dagpengegrunnlag> = mutableListOf()
    var omsorgGrunnlagListe: List<Omsorgsgrunnlag> = mutableListOf()
    var opptjeningGrunnlagListe: List<Opptjeningsgrunnlag> = mutableListOf()
    var ufoerFoer2009: Boolean = false
}
