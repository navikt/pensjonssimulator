package no.nav.pensjon.simulator.core.domain.regler.to

// 2025-03-18
abstract class ServiceRequest {

    /**
     * Satsstabell som skal benyttes ved beregning.
     * Kun lest av pensjon-regler i test miljø.
     */
    var satstabell: String? = null
}
