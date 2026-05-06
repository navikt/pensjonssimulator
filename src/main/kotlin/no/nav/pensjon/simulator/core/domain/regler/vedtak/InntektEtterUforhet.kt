package no.nav.pensjon.simulator.core.domain.regler.vedtak

import java.time.LocalDate

// 2026-05-05
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
    var ieuDatoLd: LocalDate? = null
}