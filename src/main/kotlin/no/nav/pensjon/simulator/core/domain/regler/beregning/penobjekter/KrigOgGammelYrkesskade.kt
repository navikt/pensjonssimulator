package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

/**
 * Brukes i pensjon-regler kun ved g-omregning
 */
class KrigOgGammelYrkesskade : Ytelseskomponent {

    /**
     * Pensjonsgraden
     */
    var pensjonsgrad = 0

    /**
     * grunnlag for utbetaling;
     */
    var grunnlagForUtbetaling = 0

    /**
     * Kapital utløsning
     */
    var kapitalutlosning = 0

    /**
     * poengtall
     */
    var ps = 0.0

    /**
     * forholdstall yg
     */
    var yg = 0.0

    /**
     * Men del
     */
    var mendel = 0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.KRIG_GY

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.KRIG_GY)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: KrigOgGammelYrkesskade) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.KRIG_GY
        pensjonsgrad = source.pensjonsgrad
        grunnlagForUtbetaling = source.grunnlagForUtbetaling
        kapitalutlosning = source.kapitalutlosning
        ps = source.ps
        yg = source.yg
        mendel = source.mendel
    }
    // end SIMDOM-ADD
}
