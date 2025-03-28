package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-18
class FremskrevetAfpLivsvarig : AbstraktAfpLivsvarig(), Regulering {
    override var reguleringsfaktor = 0.0
    override var gap = 0
    var gjennomsnittligUttaksgradSisteAr = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.FREM_AFP_LIVSVARIG
}
