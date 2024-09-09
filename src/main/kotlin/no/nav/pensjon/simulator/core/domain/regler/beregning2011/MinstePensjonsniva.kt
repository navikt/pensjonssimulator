package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.MinstepenNivaCti
import java.io.Serializable

class MinstePensjonsniva : Serializable {
    var belop: Double = 0.0
    var sats: Double = 0.0
    var benyttetYug: Int? = null
    var pro_rata_teller_mnd: Int = 0
    var pro_rata_nevner_mnd: Int = 0
    var pro_rata_brok: Double = 0.0
    var formelKode: FormelKodeCti? = null

    /**
     * Minstepensjonsnivå. Kan være lav, ordinær og forhøyet. Benytter tabellen
     */
    var satsType: MinstepenNivaCti? = null
    var merknadListe: MutableList<Merknad> = mutableListOf()
    var faktisk_tt_avtaleland_mnd: Int = 0

    @JsonIgnore
    var belopIkkeProratisert: Double = 0.0

    constructor() : super() {
        formelKode = FormelKodeCti("MPNx")
    }

    constructor(mpn: MinstePensjonsniva) : super() {
        belop = mpn.belop
        sats = mpn.sats
        benyttetYug = mpn.benyttetYug
        pro_rata_teller_mnd = mpn.pro_rata_teller_mnd
        pro_rata_nevner_mnd = mpn.pro_rata_nevner_mnd
        pro_rata_brok = mpn.pro_rata_brok
        if (mpn.satsType != null) {
            satsType = MinstepenNivaCti(mpn.satsType)
        }
        if (mpn.formelKode != null) {
            formelKode = FormelKodeCti(mpn.formelKode!!)
        }
        for (merknad in mpn.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }
        belopIkkeProratisert = mpn.belopIkkeProratisert
        faktisk_tt_avtaleland_mnd = mpn.faktisk_tt_avtaleland_mnd
    }

    constructor(
        belop: Double = 0.0,
        sats: Double = 0.0,
        benyttetYug: Int = 0,
        pro_rata_teller_mnd: Int = 0,
        pro_rata_nevner_mnd: Int = 0,
        pro_rata_brok: Double = 0.0,
        formelKode: FormelKodeCti? = null,
        satsType: MinstepenNivaCti? = null,
        merknadListe: MutableList<Merknad> = mutableListOf(),
        faktisk_tt_avtaleland_mnd: Int = 0,
        belopIkkeProratisert: Double = 0.0
    ) {
        this.belop = belop
        this.sats = sats
        this.benyttetYug = benyttetYug
        this.pro_rata_teller_mnd = pro_rata_teller_mnd
        this.pro_rata_nevner_mnd = pro_rata_nevner_mnd
        this.pro_rata_brok = pro_rata_brok
        this.formelKode = formelKode
        this.satsType = satsType
        this.merknadListe = merknadListe
        this.faktisk_tt_avtaleland_mnd = faktisk_tt_avtaleland_mnd
        this.belopIkkeProratisert = belopIkkeProratisert
    }
}
