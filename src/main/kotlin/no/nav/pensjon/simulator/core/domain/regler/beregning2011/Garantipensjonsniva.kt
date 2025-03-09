package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GarantiPensjonsnivaSatsEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GarantipenNivaCti
import java.io.Serializable

// Checked 2025-02-28
class Garantipensjonsniva : Serializable {
    var ektefelleInntektOver2G = false
    var belop = 0.0
    var belopIkkeProratisert = 0.0
    var sats = 0.0
    var satsType: GarantipenNivaCti? = null
    var satsTypeEnum: GarantiPensjonsnivaSatsEnum? = null
    var formelkode: FormelKodeCti? = null
    var formelkodeEnum: FormelKodeEnum? = null
    var pro_rata_teller_mnd = 0
    var pro_rata_nevner_mnd = 0
    var pro_rata_brok = 0.0
    var tt_anv = 0
    var faktisk_tt_avtaleland_mnd = 0
    var benyttetYug = 0

    constructor()

    constructor(source: Garantipensjonsniva) : this() {
        ektefelleInntektOver2G = source.ektefelleInntektOver2G
        belop = source.belop
        belopIkkeProratisert = source.belopIkkeProratisert
        sats = source.sats
        source.satsType?.let { satsType = GarantipenNivaCti(it) }
        satsTypeEnum = source.satsTypeEnum
        source.formelkode?.let { formelkode = FormelKodeCti(it) }
        formelkodeEnum = source.formelkodeEnum
        pro_rata_teller_mnd = source.pro_rata_teller_mnd
        pro_rata_nevner_mnd = source.pro_rata_nevner_mnd
        pro_rata_brok = source.pro_rata_brok
        tt_anv = source.tt_anv
        faktisk_tt_avtaleland_mnd = source.faktisk_tt_avtaleland_mnd
        benyttetYug = source.benyttetYug
    }
}
