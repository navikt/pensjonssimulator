package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.MinsteytelseNivaEnum

// 2025-03-10
class SatsMinsteytelse {
    var sats = 0.0
    var satsTypeEnum: MinsteytelseNivaEnum? = null
    var benyttetSivilstandEnum: BorMedTypeEnum? = null
    var benyttetUngUfor = false
    var oppfyltUngUfor = false

    /**
     * Angir om ung Uføregaranti ikke er benyttet pga eksportforbud.
     */
    var eksportForbudUngUfor = false

    constructor()

    constructor(source: SatsMinsteytelse) {
        sats = source.sats
        satsTypeEnum = source.satsTypeEnum
        benyttetSivilstandEnum = source.benyttetSivilstandEnum
        benyttetUngUfor = source.benyttetUngUfor
        oppfyltUngUfor = source.oppfyltUngUfor
        eksportForbudUngUfor = source.eksportForbudUngUfor
    }
}
