package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning

class BeregnPensjonsBeholdningResponse : ServiceResponse() {

    var beholdninger: ArrayList<Pensjonsbeholdning> = ArrayList()
}
