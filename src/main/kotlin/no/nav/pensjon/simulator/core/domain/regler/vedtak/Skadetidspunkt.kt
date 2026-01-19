package no.nav.pensjon.simulator.core.domain.regler.vedtak

import java.util.Date

// Copied from pensjon-regler-api 2026-01-16
class Skadetidspunkt : AbstraktBeregningsvilkar() {
    var dato: Date? = null
}
