package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class MinstenivatilleggIndividuelt : Ytelseskomponent {

    var mpn: MinstePensjonsniva? = null
    var garPN: Garantipensjonsniva? = null
    var samletPensjonForMNT = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.MIN_NIVA_TILL_INDV

    constructor() : super(typeEnum = YtelseskomponentTypeEnum.MIN_NIVA_TILL_INDV)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: MinstenivatilleggIndividuelt) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.MIN_NIVA_TILL_INDV
        samletPensjonForMNT = source.samletPensjonForMNT
        source.mpn?.let { mpn = MinstePensjonsniva(it) }
        source.garPN?.let { garPN = Garantipensjonsniva(it) }
    }
}
