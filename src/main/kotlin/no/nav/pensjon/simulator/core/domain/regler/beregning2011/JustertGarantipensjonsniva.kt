package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.io.Serializable

class JustertGarantipensjonsniva : Serializable {
    var garantipensjonsniva: Garantipensjonsniva? = null
    var justeringsInformasjon: JusteringsInformasjon? = null
    var belop = 0.0
}
