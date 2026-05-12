package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.time.LocalDate

// 2026-04-10
class LonnsvekstInformasjon {
    var lonnsvekst = 0.0
    var reguleringsDatoLd: LocalDate? = null
    var uttaksgradVedRegulering = 0
}
