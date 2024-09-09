package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import java.util.Date

// https://github.com/navikt/pensjon-regler/blob/master/system/nav-system-pensjon-domain/src/main/kotlin/no/nav/pensjon/regler/internal/to/BeregnPensjonsBeholdningRequest.kt
class BeregnPensjonsBeholdningRequest(
    var beholdningTom: Date? = null,
    var persongrunnlag: Persongrunnlag? = null,
    var beholdning: Pensjonsbeholdning? = null
) : ServiceRequest() {
    override fun virkFom(): Date? = beholdningTom

    override fun persons(): String = ""
}
