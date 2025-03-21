package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok

// 2025-03-20
class Inntektspensjon : Ytelseskomponent() {
    /**
     * brøken angir PenB_EKSPORT / PenB_NORGE som inntektspensjonen er redusert med.
     */
    var eksportBrok: Brok? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.IP
}
