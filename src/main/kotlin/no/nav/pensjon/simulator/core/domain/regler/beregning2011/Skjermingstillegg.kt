package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class Skjermingstillegg : Ytelseskomponent {

    var ft67Soker: Double? = null
    var skjermingsgrad: Double? = null
    var ufg: Int? = null
    var basGp_bruttoPerAr: Double? = null
    var basTp_bruttoPerAr: Double? = null
    var basPenT_bruttoPerAr: Double? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.SKJERMT

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.SKJERMT)

    constructor(source: Skjermingstillegg) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.SKJERMT
        ft67Soker = source.ft67Soker
        skjermingsgrad = source.skjermingsgrad
        ufg = source.ufg
        basGp_bruttoPerAr = source.basGp_bruttoPerAr
        basTp_bruttoPerAr = source.basTp_bruttoPerAr
        basPenT_bruttoPerAr = source.basPenT_bruttoPerAr
    }
    // end SIMDOM-ADD
}
