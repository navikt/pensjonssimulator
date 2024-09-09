package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class Sykepenger : MotregningYtelseskomponent {

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("SP"))
    constructor(sykepenger: Sykepenger) : super(sykepenger) {}
}
