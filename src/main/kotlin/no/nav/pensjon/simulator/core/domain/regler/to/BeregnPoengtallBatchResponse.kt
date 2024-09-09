package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonOpptjeningsgrunnlag
import java.util.*

class BeregnPoengtallBatchResponse(
    var personOpptjeningsgrunnlagListe: MutableList<PersonOpptjeningsgrunnlag> = mutableListOf()
) : ServiceResponse() {
    override fun virkFom(): Date? {
        return null
    }

    override fun persons(): String {
        return ""
    }
}
