package no.nav.pensjon.simulator.core.domain.regler.vedtak

import java.time.LocalDate

// 2026-05-05
class Skadetidspunkt : AbstraktBeregningsvilkar() {
    var datoLd: LocalDate? = null
}