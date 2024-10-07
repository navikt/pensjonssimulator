package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class Arbeidsavklaringspenger : MotregningYtelseskomponent() {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.AAP
}
