package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class FasteUtgifterTilleggUT : Ytelseskomponent {

    var nettoAkk: Int? = null
    var nettoRestAr: Int? = null
    var avkortningsbelopPerAr: Int? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.UT_FAST_UTGIFT_T

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.UT_FAST_UTGIFT_T)

    constructor(source: FasteUtgifterTilleggUT) : super(source) {
        nettoAkk = source.nettoAkk
        nettoRestAr = source.nettoRestAr
        avkortningsbelopPerAr = source.avkortningsbelopPerAr
    }
    // end SIMDOM-ADD
}
