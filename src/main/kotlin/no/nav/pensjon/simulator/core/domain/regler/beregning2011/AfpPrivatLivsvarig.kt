package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-18
class AfpPrivatLivsvarig : AbstraktAfpLivsvarig() {
    var justeringsbelop: Int = 0
    var afpProsentgrad: Double = 0.0
    var afpForholdstall: Double = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.AFP_PRIVAT_LIVSVARIG
}
