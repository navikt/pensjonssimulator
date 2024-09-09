package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonOpptjeningsgrunnlag
import java.util.*

class BeregnPoengtallBatchRequest(
    var personOpptjeningsgrunnlagListe: MutableList<PersonOpptjeningsgrunnlag> = mutableListOf()
) : ServiceRequest() {
    override fun virkFom(): Date? {
        return null
    }

    override fun persons(): String {
        return ""
    }
}
