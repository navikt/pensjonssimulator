package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-23
class Skjermingstillegg : Ytelseskomponent() {
    var ft67Soker: Double? = null
    var skjermingsgrad: Double? = null
    var ufg: Int? = null
    var basGp_bruttoPerAr: Double? = null
    var basTp_bruttoPerAr: Double? = null
    var basPenT_bruttoPerAr: Double? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.SKJERMT
}
