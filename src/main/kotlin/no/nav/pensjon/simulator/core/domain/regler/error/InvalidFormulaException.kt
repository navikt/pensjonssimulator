package no.nav.pensjon.simulator.core.domain.regler.error

class InvalidFormulaException : RuntimeException {
    constructor() : super()
    constructor(msg: String?) : super(msg)
}
