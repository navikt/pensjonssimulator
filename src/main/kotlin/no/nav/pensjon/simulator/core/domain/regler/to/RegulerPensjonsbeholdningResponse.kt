package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonPensjonsbeholdning

class RegulerPensjonsbeholdningResponse : ServiceResponse() {

    var regulertBeregningsgrunnlagForPensjonsbeholdning: ArrayList<PersonPensjonsbeholdning> = ArrayList()
}
