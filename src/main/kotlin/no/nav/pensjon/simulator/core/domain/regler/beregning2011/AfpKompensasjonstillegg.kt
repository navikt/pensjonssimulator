package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class AfpKompensasjonstillegg : Ytelseskomponent {

    var referansebelop = 0
    var reduksjonsbelop = 0
    var forholdstallKompensasjonstillegg = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.AFP_KOMP_TILLEGG

    // SIMDOM-ADD
    constructor() : super()

    constructor(source: AfpKompensasjonstillegg) : super(source) {
        referansebelop = source.referansebelop
        reduksjonsbelop = source.reduksjonsbelop
        forholdstallKompensasjonstillegg = source.forholdstallKompensasjonstillegg
    }
    // end SIMDOM-ADD
}
