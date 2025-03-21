package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-20
class MinstenivatilleggPensjonistpar : Ytelseskomponent() {
    var bruker: BeregningsInformasjonMinstenivatilleggPensjonistpar? = null
    var ektefelle: BeregningsInformasjonMinstenivatilleggPensjonistpar? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.MIN_NIVA_TILL_PPAR
}
