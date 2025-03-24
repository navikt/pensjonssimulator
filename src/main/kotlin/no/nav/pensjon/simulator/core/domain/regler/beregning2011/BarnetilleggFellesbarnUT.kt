package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class BarnetilleggFellesbarnUT : AbstraktBarnetilleggUT {
    /**
     * beløp som er fratrukket annen forelders inntekt (inntil 1G)
     */
    var belopFratrukketAnnenForeldersInntekt = 0

    /**
     * Brukers inntekt
     */
    var brukersInntektTilAvkortning = 0

    /**
     * Annen forelders inntekt brukt i behovsprøving
     */
    var inntektAnnenForelder = 0

    /**
     * Annen forelders uføretrygd før justering.
     */
    var annenForelderUforetrygdForJustering = 0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.UT_TFB

    constructor() {
        formelKodeEnum = FormelKodeEnum.BTx
    }
}
