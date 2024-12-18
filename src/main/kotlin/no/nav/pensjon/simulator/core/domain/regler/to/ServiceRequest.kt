package no.nav.pensjon.simulator.core.domain.regler.to

import java.io.Serializable

//TODO remove ServiceRequest
abstract class ServiceRequest : Serializable {

    /**
     * Satsstabell som skal benyttes ved beregning.
     * Kun lest av pensjon-regler i test miljø.
     */
    var satstabell: String? = null
}
