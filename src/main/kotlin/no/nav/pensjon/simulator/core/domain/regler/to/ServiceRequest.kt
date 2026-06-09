package no.nav.pensjon.simulator.core.domain.regler.to

// Copied from pensjon-regler-api v2.0.0 2026-06-08
// plus given value for satstabell to make end-to-end tests stable
abstract class ServiceRequest {

    /**
     * Satsstabell som skal benyttes ved beregning.
     * Kun lest av pensjon-regler i test miljø.
     */
    var satstabell: String? = "PROD_20260701"
}