package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonPensjonsbeholdning
import java.util.*

class RegulerPensjonsbeholdningRequest(
    var virkFom: Date? = null,
    var beregningsgrunnlagForPensjonsbeholdning: ArrayList<PersonPensjonsbeholdning> = arrayListOf()
) : ServiceRequest() {
    override fun virkFom(): Date? {
        return this.virkFom
    }

    override fun persons(): String {
        return ""
    }
}
