package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class Sykepenger : MotregningYtelseskomponent() {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.SP
}
