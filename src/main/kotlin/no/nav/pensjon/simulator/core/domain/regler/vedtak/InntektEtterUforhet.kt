package no.nav.pensjon.simulator.core.domain.regler.vedtak

import java.util.Date

// Copied from pensjon-regler-api 2026-01-16
/**
 * Angir inntekt etter uførhet (IEU).
 */
class InntektEtterUforhet : AbstraktBeregningsvilkar() {
    /**
     * Inntekten.
     */
    var inntekt = 0

    /**
     * Virkningstidspunktet for inntekt etter uførhet.
     */
    var ieuDato: Date? = null
}
