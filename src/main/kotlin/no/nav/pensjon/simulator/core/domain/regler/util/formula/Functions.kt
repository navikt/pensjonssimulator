package no.nav.pensjon.simulator.core.domain.regler.util.formula

fun avrund(inKFormel: Formel): Formel {
    return inKFormel.apply {
        this.notasjon = "avrund( ${this.notasjon} )"
        this.innhold = "avrund( ${this.innhold} )"
    }.apply {
        expectDoubleResult = false
    }
}

fun afpAvrundBrutto(inKFormel: Formel): Formel {
    return inKFormel.copy().apply {
        this.notasjon = "afpAvrundBrutto( ${this.notasjon} )"
        this.innhold = "afpAvrundBrutto( ${this.innhold} )"
    }.apply {
        expectDoubleResult = false
    }
}

fun afpAvrundNetto(a: Formel, b: Formel): Formel {
    return a.copy().apply {
        this.notasjon = "afpAvrundNetto( ${a.notasjon}, ${b.notasjon})"
        this.innhold = "afpAvrundNetto( ${a.innhold}, ${b.innhold})"
    }.apply {
        expectDoubleResult = false
    }
}

fun kMax(inFormelA: Formel, inFormelB: Formel): Formel {
    return inFormelA.apply {
        this.notasjon = "max( ${this.notasjon}, ${inFormelB.notasjon} )"
        this.innhold = "max( ${this.innhold}, ${inFormelB.innhold} )"
    }
}

fun kMax(number: Number, inFormelB: Formel): Formel {
    return inFormelB.apply {
        this.notasjon = "max( $number, ${this.notasjon} )"
        this.innhold = "max( $number, ${this.innhold} )"
    }
}

fun kMin(inFormelA: Formel, inFormelB: Formel): Formel {
    return inFormelA.apply {
        this.notasjon = "min( ${this.notasjon}, ${inFormelB.notasjon} )"
        this.innhold = "min( ${this.innhold}, ${inFormelB.innhold} )"
    }
}

fun kMin(number: Number, inFormelB: Formel): Formel {
    return inFormelB.apply {
        this.notasjon = "min( $number, ${this.notasjon} )"
        this.innhold = "min( $number, ${this.innhold} )"
    }
}

fun avrund2Desimal(inKFormel: Formel): Formel {
    return inKFormel.apply {
        this.notasjon = "avrund2Desimal( ${this.notasjon} )"
        this.innhold = "avrund2Desimal( ${this.innhold} )"
    }.apply {
        expectDoubleResult = true
    }
}
