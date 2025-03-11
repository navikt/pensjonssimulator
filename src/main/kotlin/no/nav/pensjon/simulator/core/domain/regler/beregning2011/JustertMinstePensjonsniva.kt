package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy

// 2025-03-11
class JustertMinstePensjonsniva {
    var minstePensjonsniva: MinstePensjonsniva? = null
    var justeringsInformasjon: JusteringsInformasjon? = null
    var belop = 0.0

    constructor()

    constructor(source: JustertMinstePensjonsniva) : super() {
        belop = source.belop
        minstePensjonsniva = source.minstePensjonsniva?.let(::MinstePensjonsniva)
        justeringsInformasjon = source.justeringsInformasjon?.copy()
    }
}
