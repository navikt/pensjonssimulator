package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel

class PersonPensjonsbeholdning(
        var pensjonsbeholdning: Pensjonsbeholdning? = null,
        var pakkseddel: Pakkseddel? = null,
        var fodselsnummer: String? = null
) {
    constructor(o: PersonPensjonsbeholdning) : this() {
        if (o.pensjonsbeholdning != null) {
            this.pensjonsbeholdning = Pensjonsbeholdning(o.pensjonsbeholdning!!)
        }
        if (o.pakkseddel != null) {
            this.pakkseddel = Pakkseddel(o.pakkseddel!!)
        }
        this.fodselsnummer = o.fodselsnummer
    }
}
