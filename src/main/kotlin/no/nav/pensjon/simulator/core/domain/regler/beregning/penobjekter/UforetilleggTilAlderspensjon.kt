package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class UforetilleggTilAlderspensjon : Ytelseskomponent {

    var beregning: Beregning? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.UFORETILLEGG_AP

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.UFORETILLEGG_AP)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: UforetilleggTilAlderspensjon) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.UFORETILLEGG_AP
        beregning = source.beregning?.let(::Beregning)
    }
    // end SIMDOM-ADD
}
