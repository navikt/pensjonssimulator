package no.nav.pensjon.simulator.core.domain.regler.to

import java.time.LocalDate

// 2026-05-05
class HentGrunnbelopListeRequest : ServiceRequest() {
    var fomLd: LocalDate? = null
    var tomLd: LocalDate? = null
}