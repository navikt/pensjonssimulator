package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-23
/**
 * Ventetillegg. netto=brutto=venteTillegg_GP+venteTillegg_TP
 */
class Ventetillegg : Ytelseskomponent() {
    /**
     * Ventetillegget for GP
     */
    var venteTillegg_GP = 0

    /**
     * Ventetillegget for tillegspensjon
     */
    var venteTillegg_TP = 0

    /**
     * Prosenten
     */
    var venteTilleggProsent = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.VT
}
