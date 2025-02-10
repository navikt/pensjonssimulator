package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.Hjelpeloshetsbidrag
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class MinstenivatilleggPensjonistpar : Ytelseskomponent {

    var bruker: BeregningsInformasjonMinstenivatilleggPensjonistpar? = null
    var ektefelle: BeregningsInformasjonMinstenivatilleggPensjonistpar? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.MIN_NIVA_TILL_PPAR

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.MIN_NIVA_TILL_PPAR)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: MinstenivatilleggPensjonistpar) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.MIN_NIVA_TILL_PPAR
        bruker = source.bruker?.let(::BeregningsInformasjonMinstenivatilleggPensjonistpar)
        ektefelle = source.ektefelle?.let(::BeregningsInformasjonMinstenivatilleggPensjonistpar)
    }
    // end SIMDOM-ADD
}
