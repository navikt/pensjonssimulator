package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonPensjonsbeholdning
import java.util.*

class RegulerPensjonsbeholdningRequest : ServiceRequest() {

    var virkFom: Date? = null
    var beregningsgrunnlagForPensjonsbeholdning: ArrayList<PersonPensjonsbeholdning> = ArrayList()
}
