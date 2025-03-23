package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-23
class Sertillegg : Ytelseskomponent() {
    /**
     * Prosentsatsen
     */
    var pSats_st = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.ST
}
