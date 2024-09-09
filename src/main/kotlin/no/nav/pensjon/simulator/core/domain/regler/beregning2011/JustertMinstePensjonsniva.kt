package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.io.Serializable

class JustertMinstePensjonsniva : Serializable {
    var minstePensjonsniva: MinstePensjonsniva? = null
    var justeringsInformasjon: JusteringsInformasjon? = null
    var belop: Double = 0.0

    constructor() : super() {}

    constructor(fmpn: JustertMinstePensjonsniva) : super() {
        belop = fmpn.belop
        if (fmpn.minstePensjonsniva != null) {
            minstePensjonsniva = MinstePensjonsniva(fmpn.minstePensjonsniva!!)
        }
        if (fmpn.justeringsInformasjon != null) {
            justeringsInformasjon = JusteringsInformasjon(fmpn.justeringsInformasjon!!)
        }
    }

    constructor(mpn: MinstePensjonsniva?) {
        if (mpn != null) {
            minstePensjonsniva = MinstePensjonsniva(mpn)
        }
    }

    constructor(
            minstePensjonsniva: MinstePensjonsniva? = null,
            justeringsInformasjon: JusteringsInformasjon? = null,
            belop: Double = 0.0
    ) {
        this.minstePensjonsniva = minstePensjonsniva
        this.justeringsInformasjon = justeringsInformasjon
        this.belop = belop
    }
}
