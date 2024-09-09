package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.io.Serializable

class JustertGarantipensjonsniva : Serializable {
    var garantipensjonsniva: Garantipensjonsniva? = null
    var justeringsInformasjon: JusteringsInformasjon? = null
    var belop: Double = 0.0

    constructor() : super() {}

    constructor(jgarPN: JustertGarantipensjonsniva) : super() {
        belop = jgarPN.belop
        if (jgarPN.garantipensjonsniva != null) {
            garantipensjonsniva = Garantipensjonsniva(jgarPN.garantipensjonsniva!!)
        }
        if (jgarPN.justeringsInformasjon != null) {
            justeringsInformasjon = JusteringsInformasjon(jgarPN.justeringsInformasjon!!)
        }
    }

    constructor(garPN: Garantipensjonsniva?) {
        if (garPN != null) {
            garantipensjonsniva = Garantipensjonsniva(garPN)
        }
    }

    constructor(
            garantipensjonsniva: Garantipensjonsniva? = null,
            justeringsInformasjon: JusteringsInformasjon? = null,
            belop: Double = 0.0
    ) {
        this.garantipensjonsniva = garantipensjonsniva
        this.justeringsInformasjon = justeringsInformasjon
        this.belop = belop
    }

}
