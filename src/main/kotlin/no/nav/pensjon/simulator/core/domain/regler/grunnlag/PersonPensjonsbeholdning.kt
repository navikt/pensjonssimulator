package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

class PersonPensjonsbeholdning(
    var pensjonsbeholdning: Pensjonsbeholdning? = null,
    var pakkseddel: Pakkseddel? = null,
    var fodselsnummer: String? = null
) {
    constructor(o: PersonPensjonsbeholdning) : this() {
        if (o.pensjonsbeholdning != null) {
            this.pensjonsbeholdning = Pensjonsbeholdning(o.pensjonsbeholdning!!)
        }
        this.pakkseddel = o.pakkseddel?.copy()
        this.fodselsnummer = o.fodselsnummer
    }
}
