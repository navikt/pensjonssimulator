package no.nav.pensjon.simulator.person.relasjon

import no.nav.pensjon.simulator.core.domain.regler.PenPerson

// PEN: no.nav.domain.pensjon.kjerne.grunnlag.SoskenDetalj
class Soesken {
    var mor: PenPerson? = null
    var far: PenPerson? = null
    var iKullMedBruker: Boolean? = null
}