package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-20
class MinstenivatilleggIndividuelt : Ytelseskomponent() {
    var mpn: MinstePensjonsniva? = null
    var garPN: Garantipensjonsniva? = null
    var samletPensjonForMNT = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.MIN_NIVA_TILL_INDV
}
