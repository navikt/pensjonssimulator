package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning

// 2026-05-05
class BeregnPensjonsBeholdningResponse : ServiceResponse() {

    var beholdninger: ArrayList<Pensjonsbeholdning> = ArrayList()
}
