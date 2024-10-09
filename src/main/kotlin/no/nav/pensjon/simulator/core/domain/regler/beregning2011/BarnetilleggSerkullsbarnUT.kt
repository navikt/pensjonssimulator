package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class BarnetilleggSerkullsbarnUT : AbstraktBarnetilleggUT {

    /**
     * Brukers gjenlevendetillegg før justering.
     */
    var brukersGjenlevendetilleggForJustering = 0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.UT_TSB

    constructor() {
        formelKodeEnum = FormelKodeEnum.BTx
    }

    constructor(source: BarnetilleggSerkullsbarnUT) : super(source) {
        brukersGjenlevendetilleggForJustering = source.brukersGjenlevendetilleggForJustering
    }
}
