package no.nav.pensjon.simulator.core.domain.regler.to

// 2026-04-23 + default satstabell
abstract class ServiceRequest {

    /**
     * Satsstabell som skal benyttes ved beregning.
     * Kun lest av pensjon-regler i test miljø.
     */
    var satstabell: String? = "PROD_20250701"
}
