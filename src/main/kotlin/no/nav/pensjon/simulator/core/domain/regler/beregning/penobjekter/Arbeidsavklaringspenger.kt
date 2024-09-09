package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class Arbeidsavklaringspenger : MotregningYtelseskomponent {
    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("AAP"))

    constructor(arbeidsavklaringspenger: Arbeidsavklaringspenger) : super(arbeidsavklaringspenger)
}
