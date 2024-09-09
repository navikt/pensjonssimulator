package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.io.Serializable

class ReguleringsInformasjon : Serializable {

    var lonnsvekst: Double = 0.0
    var fratrekksfaktor: Double = 0.0
    var gammelG: Int = 0
    var nyG: Int = 0
    var reguleringsfaktor: Double = 0.0
    var gjennomsnittligUttaksgradSisteAr: Double = 0.0
    var reguleringsbelop: Double = 0.0
    var prisOgLonnsvekst: Double = 0.0 // SIMDOM-MOVE

    constructor()

    constructor(r: ReguleringsInformasjon) : this() {
        lonnsvekst = r.lonnsvekst
        prisOgLonnsvekst = r.prisOgLonnsvekst
        fratrekksfaktor = r.fratrekksfaktor
        gammelG = r.gammelG
        nyG = r.nyG
        reguleringsfaktor = r.reguleringsfaktor
        gjennomsnittligUttaksgradSisteAr = r.gjennomsnittligUttaksgradSisteAr
        reguleringsbelop = r.reguleringsbelop
    }

    constructor(
        lonnsvekst: Double = 0.0,
        prisOgLonnsvekst: Double = 0.0,
        fratrekksfaktor: Double = 0.0,
        gammelG: Int = 0,
        nyG: Int = 0,
        reguleringsfaktor: Double = 0.0,
        gjennomsnittligUttaksgradSisteAr: Double = 0.0,
        reguleringsbelop: Double = 0.0
    ) {
        this.lonnsvekst = lonnsvekst
        this.prisOgLonnsvekst = prisOgLonnsvekst
        this.fratrekksfaktor = fratrekksfaktor
        this.gammelG = gammelG
        this.nyG = nyG
        this.reguleringsfaktor = reguleringsfaktor
        this.gjennomsnittligUttaksgradSisteAr = gjennomsnittligUttaksgradSisteAr
        this.reguleringsbelop = reguleringsbelop
    }
}
