package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Trygdetid

// Copied from pensjon-regler-api 2026-01-16
class TrygdetidBeregningsvilkar : AbstraktBeregningsvilkar() {
    /**
     * Trygdetiden.
     */
    var trygdetid: Trygdetid? = null
}
