package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-20
class FasteUtgifterTilleggUT : Ytelseskomponent() {
    var nettoAkk: Int? = null
    var nettoRestAr: Int? = null
    var avkortningsbelopPerAr: Int? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.UT_FAST_UTGIFT_T
}
