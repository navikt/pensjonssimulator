package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonOpptjeningsgrunnlag

class BeregnPoengtallBatchRequest : ServiceRequest() {

    var personOpptjeningsgrunnlagListe: MutableList<PersonOpptjeningsgrunnlag?> = mutableListOf()
}
