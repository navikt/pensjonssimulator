package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import java.io.Serializable

class Skattekompensertbelop : Serializable {
    var faktor: Double = 0.0
    var formelKode: FormelKodeCti? = null
    var arsbelop: Double = 0.0
    var justertbelop: Justertbelop? = null
    var tillegg: Double = 0.0

    constructor() : super() {}

    constructor(skattekompensertbelop: Skattekompensertbelop) {
        this.faktor = skattekompensertbelop.faktor
        if (skattekompensertbelop.formelKode != null) {
            this.formelKode = FormelKodeCti(skattekompensertbelop.formelKode!!)
        }
        this.arsbelop = skattekompensertbelop.arsbelop
        if (skattekompensertbelop.justertbelop != null) {
            this.justertbelop = Justertbelop(skattekompensertbelop.justertbelop!!)
        }
        this.tillegg = skattekompensertbelop.tillegg
    }

    constructor(
            faktor: Double = 0.0,
            formelKode: FormelKodeCti? = null,
            arsbelop: Double = 0.0,
            justertbelop: Justertbelop? = null,
            tillegg: Double = 0.0
    ) {
        this.faktor = faktor
        this.formelKode = formelKode
        this.arsbelop = arsbelop
        this.justertbelop = justertbelop
        this.tillegg = tillegg
    }
}
