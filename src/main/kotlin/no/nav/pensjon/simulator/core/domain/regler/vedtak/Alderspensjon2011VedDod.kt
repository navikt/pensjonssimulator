package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseVedDodEnum

// Copied from pensjon-regler-api 2026-01-16
class Alderspensjon2011VedDod : AbstraktBeregningsvilkar() {
    /**
     * Angir hvilken ytelse avdøde hadde før død.
     */
    var ytelseVedDodEnum: YtelseVedDodEnum? = null
}
