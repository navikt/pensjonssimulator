package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class FremskrevetAfpLivsvarig : AfpLivsvarig, Regulering {

    override var reguleringsfaktor = 0.0
    override var gap = 0
    var gjennomsnittligUttaksgradSisteAr = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.FREM_AFP_LIVSVARIG

    // SIMDOM-ADD
    constructor() : super()

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: FremskrevetAfpLivsvarig) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.FREM_AFP_LIVSVARIG
        reguleringsfaktor = source.reguleringsfaktor
        gap = source.gap
        gjennomsnittligUttaksgradSisteAr = source.gjennomsnittligUttaksgradSisteAr
    }
    // end SIMDOM-ADD
}
