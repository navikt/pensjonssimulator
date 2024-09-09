package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GarantipenNivaCti
import java.io.Serializable

class Garantipensjonsniva : Serializable {
    var ektefelleInntektOver2G: Boolean = false
    var belop: Double = 0.0
    var belopIkkeProratisert: Double = 0.0
    var sats: Double = 0.0
    var satsType: GarantipenNivaCti? = null
    var formelkode: FormelKodeCti? = null
    var pro_rata_teller_mnd: Int = 0
    var pro_rata_nevner_mnd: Int = 0
    var pro_rata_brok: Double = 0.0
    var tt_anv: Int = 0
    var faktisk_tt_avtaleland_mnd: Int = 0
    val benyttetYug: Int = 0 // SIMDOM-ADD to equalize simdom and legacy requests

    constructor()

    constructor(garPN: Garantipensjonsniva) : this() {
        belop = garPN.belop
        sats = garPN.sats
        belopIkkeProratisert = garPN.belopIkkeProratisert
        if (garPN.satsType != null) {
            satsType = GarantipenNivaCti(garPN.satsType)
        }
        if (garPN.formelkode != null) {
            this.formelkode = FormelKodeCti(garPN.formelkode!!)
        }
        pro_rata_teller_mnd = garPN.pro_rata_teller_mnd
        pro_rata_nevner_mnd = garPN.pro_rata_nevner_mnd
        pro_rata_brok = garPN.pro_rata_brok
        tt_anv = garPN.tt_anv
        ektefelleInntektOver2G = garPN.ektefelleInntektOver2G
        faktisk_tt_avtaleland_mnd = garPN.faktisk_tt_avtaleland_mnd
    }

    constructor(
            ektefelleInntektOver2G: Boolean = false,
            belop: Double = 0.0,
            belopIkkeProratisert: Double = 0.0,
            sats: Double = 0.0,
            satsType: GarantipenNivaCti? = null,
            formelkode: FormelKodeCti? = null,
            pro_rata_teller_mnd: Int = 0,
            pro_rata_nevner_mnd: Int = 0,
            pro_rata_brok: Double = 0.0,
            tt_anv: Int = 0,
            faktisk_tt_avtaleland_mnd: Int = 0
    ) {
        this.ektefelleInntektOver2G = ektefelleInntektOver2G
        this.belop = belop
        this.belopIkkeProratisert = belopIkkeProratisert
        this.sats = sats
        this.satsType = satsType
        this.formelkode = formelkode
        this.pro_rata_teller_mnd = pro_rata_teller_mnd
        this.pro_rata_nevner_mnd = pro_rata_nevner_mnd
        this.pro_rata_brok = pro_rata_brok
        this.tt_anv = tt_anv
        this.faktisk_tt_avtaleland_mnd = faktisk_tt_avtaleland_mnd
    }
}
