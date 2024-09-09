package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import java.io.Serializable
import java.util.*

abstract class ServiceRequest : Serializable, CommonProperties {
    var satstabell: String? = null
}

abstract class ServiceResponse(
    open val pakkseddel: Pakkseddel = Pakkseddel()
) : Serializable, CommonProperties

interface CommonProperties {
    fun virkFom(): Date?
    fun persons(): String
}
