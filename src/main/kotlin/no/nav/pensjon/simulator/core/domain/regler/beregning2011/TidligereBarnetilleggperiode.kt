package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok
import java.util.*

class TidligereBarnetilleggperiode : AbstraktBarnetilleggperiode {
    /**
     * Hva barnetillegget i tidligere periode faktisk ble avkortet med per år.
     */
    var faktiskFradragPerAr: Double = 0.0

    /**
     * Periodens bidrag til avviksbeløp.
     */
    var avviksbelop: Double = 0.0

    constructor() {}

    constructor(tbtp: TidligereBarnetilleggperiode) : super(tbtp) {
        this.faktiskFradragPerAr = tbtp.faktiskFradragPerAr
        this.avviksbelop = tbtp.avviksbelop
    }

    constructor(
            faktiskFradragPerAr: Double = 0.0,
            avviksbelop: Double = 0.0,
            /** super AbstraktBarnetilleggperiode */
            fomDato: Date? = null,
            tomDato: Date? = null,
            lengde: Int = 0,
            antallBarn: Int = 0,
            fribelop: Int = 0,
            bruttoPerAr: Int = 0,
            reguleringsfaktor: Brok? = null,
            avkortingsbelopPerAr: Int = 0
    ) : super(
            fomDato = fomDato,
            tomDato = tomDato,
            lengde = lengde,
            antallBarn = antallBarn,
            fribelop = fribelop,
            bruttoPerAr = bruttoPerAr,
            reguleringsfaktor = reguleringsfaktor,
            avkortingsbelopPerAr = avkortingsbelopPerAr
    ) {
        this.faktiskFradragPerAr = faktiskFradragPerAr
        this.avviksbelop = avviksbelop
    }
}
