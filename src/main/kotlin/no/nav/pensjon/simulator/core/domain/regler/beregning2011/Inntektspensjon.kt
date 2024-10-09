package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok

class Inntektspensjon : Ytelseskomponent {

    /**
     * brøken angir PenB_EKSPORT / PenB_NORGE som inntektspensjonen er redusert med.
     */
    var eksportBrok: Brok? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.IP

    constructor() : super(typeEnum = YtelseskomponentTypeEnum.IP)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Inntektspensjon) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.IP
        source.eksportBrok?.let { eksportBrok = Brok(it) }
    }
}
