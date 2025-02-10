package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

/**
 * Ventetillegg. netto=brutto=venteTillegg_GP+venteTillegg_TP
 */
class Ventetillegg : Ytelseskomponent {

    /**
     * Ventetillegget for GP
     */
    var venteTillegg_GP = 0

    /**
     * Ventetillegget for tillegspensjon
     */
    var venteTillegg_TP = 0

    /**
     * Prosenten
     */
    var venteTilleggProsent = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.VT

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.VT)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Ventetillegg) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.VT
        venteTillegg_GP = source.venteTillegg_GP
        venteTillegg_TP = source.venteTillegg_TP
        venteTilleggProsent = source.venteTilleggProsent
    }
    // end SIMDOM-ADD
}
