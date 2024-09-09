package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.MinsteytelseNivaCti

/**
 * @author Steinar Hjellvik (Decisive) - PK-11391
 */
class SatsMinsteytelse {
    var sats: Double = 0.0
    var satsType: MinsteytelseNivaCti? = null
    var benyttetSivilstand: BorMedTypeCti? = null
    var benyttetUngUfor: Boolean = false
    var oppfyltUngUfor: Boolean = false

    /**
     * Angir om ung uføregaranti ikke er benyttet pga eksportforbud.
     */
    var eksportForbudUngUfor: Boolean = false

    constructor()

    constructor(satsMinsteytelse: SatsMinsteytelse) {
        sats = satsMinsteytelse.sats
        if (satsMinsteytelse.satsType != null) {
            satsType = MinsteytelseNivaCti(satsMinsteytelse.satsType)
        }
        if (satsMinsteytelse.benyttetSivilstand != null) {
            benyttetSivilstand = BorMedTypeCti(satsMinsteytelse.benyttetSivilstand)
        }
        benyttetUngUfor = satsMinsteytelse.benyttetUngUfor
        oppfyltUngUfor = satsMinsteytelse.oppfyltUngUfor
        eksportForbudUngUfor = satsMinsteytelse.eksportForbudUngUfor
    }

    constructor(
        sats: Double = 0.0,
        satsType: MinsteytelseNivaCti? = null,
        benyttetSivilstand: BorMedTypeCti? = null,
        benyttetUngUfor: Boolean = false,
        oppfyltUngUfor: Boolean = false,
        eksportForbudUngUfor: Boolean = false
    ) {
        this.sats = sats
        this.satsType = satsType
        this.benyttetSivilstand = benyttetSivilstand
        this.benyttetUngUfor = benyttetUngUfor
        this.oppfyltUngUfor = oppfyltUngUfor
        this.eksportForbudUngUfor = eksportForbudUngUfor
    }
}
