package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import java.time.LocalDate

// 2025-03-18
open class AfpOffentligLivsvarig : AbstraktAfpLivsvarig() {
    var sistRegulertG: Int? = null
    var uttaksdato: LocalDate? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.AFP_OFFENTLIG_LIVSVARIG
}
