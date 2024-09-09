package no.nav.pensjon.simulator.core.domain.regler.to

import java.util.*

class HentGrunnbelopListeRequest(
    val fom: Date,
    val tom: Date?
) : ServiceRequest() {

    override fun virkFom(): Date? = null

    override fun persons(): String = ""
}
