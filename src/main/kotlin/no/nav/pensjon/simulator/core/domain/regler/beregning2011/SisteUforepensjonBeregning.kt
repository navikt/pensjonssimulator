package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ektefelletillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.UforeEkstra
import no.nav.pensjon.simulator.core.domain.regler.kode.*
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid
import java.util.*

/**
 * @author Aasmund Nordstoga (Accenture) PK-5549
 * @author Swiddy de Louw (Capgemini) PK-7113
 * @auhtor Magnus Bakken (Accenture) PK-9158
 */
class SisteUforepensjonBeregning : SisteBeregning {
    var et: Ektefelletillegg? = null
    var tt: Trygdetid? = null
    var uforeEkstra: UforeEkstra? = null

    /**
     * Konverteringsgrunnlaget for brukers rettigheter
     */
    var konverteringsgrunnlagOrdinert: Konverteringsgrunnlag? = null

    /**
     * Betinget, Konverteringsgrunnlag hvis gjenlevenderetten vant
     */
    var konverteringsgrunnlagGJT: Konverteringsgrunnlag? = null

    /**
     * Betinget: Hvis uføregraden > 0, yrkesskadegrad < uføregrad.
     */
    var konverteringsgrunnlagUforedel: Konverteringsgrunnlag? = null

    /**
     * Betinget: Hvis uføregraden > 0, yrkesskadegrad < uføregrad.
     */
    var konverteringsgrunnlagYrkesskade: Konverteringsgrunnlag? = null

    /**
     * Betinget: Hvis det fantes en folketrygdberegning(ikke nødvendigvis vinnende) i UP
     */
    var anvendtTrygdetidFolketrygd: AnvendtTrygdetid? = null

    /**
     * Hvorvidt utbetalt uførepensjonen per 31.12.2014 ble definert som minstepensjon.
     */
    var minstepensjonType: MinstepensjonTypeCti? = null

    /**
     * Hvorvidt utbetalt uførepensjonen per 31.12.2014 ble manuelt overstyrt eller ikke.
     */
    var resultatKilde: ResultatKildeCti? = null

    /**
     * Netto særtillegg i utbetalt uførepensjonen per 31.12.2014.
     */
    var sertilleggNetto: Int = 0

    constructor() : super()

    constructor(sb: SisteUforepensjonBeregning) : super(sb) {
        if (sb.et != null) {
            et = Ektefelletillegg(sb.et!!)
        }
        if (sb.tt != null) {
            tt = Trygdetid(sb.tt!!)
        }
        if (sb.uforeEkstra != null) {
            uforeEkstra = UforeEkstra(sb.uforeEkstra!!)
        }
        if (sb.konverteringsgrunnlagOrdinert != null) {
            konverteringsgrunnlagOrdinert = Konverteringsgrunnlag(sb.konverteringsgrunnlagOrdinert!!)
        }
        if (sb.konverteringsgrunnlagGJT != null) {
            konverteringsgrunnlagGJT = Konverteringsgrunnlag(sb.konverteringsgrunnlagGJT!!)
        }
        if (sb.konverteringsgrunnlagUforedel != null) {
            konverteringsgrunnlagUforedel = Konverteringsgrunnlag(sb.konverteringsgrunnlagUforedel!!)
        }
        if (sb.konverteringsgrunnlagYrkesskade != null) {
            konverteringsgrunnlagYrkesskade = Konverteringsgrunnlag(sb.konverteringsgrunnlagYrkesskade!!)
        }
        if (sb.anvendtTrygdetidFolketrygd != null) {
            anvendtTrygdetidFolketrygd = AnvendtTrygdetid(sb.anvendtTrygdetidFolketrygd!!)
        }

        if (sb.minstepensjonType != null) {
            minstepensjonType = MinstepensjonTypeCti(sb.minstepensjonType)
        }

        if (sb.resultatKilde != null) {
            resultatKilde = ResultatKildeCti(sb.resultatKilde)
        }
    }

    constructor(
        et: Ektefelletillegg? = null,
        tt: Trygdetid? = null,
        uforeEkstra: UforeEkstra? = null,
        konverteringsgrunnlagOrdinert: Konverteringsgrunnlag? = null,
        konverteringsgrunnlagGJT: Konverteringsgrunnlag? = null,
        konverteringsgrunnlagUforedel: Konverteringsgrunnlag? = null,
        konverteringsgrunnlagYrkesskade: Konverteringsgrunnlag? = null,
        anvendtTrygdetidFolketrygd: AnvendtTrygdetid? = null,
        minstepensjonType: MinstepensjonTypeCti? = null,
        resultatKilde: ResultatKildeCti? = null,
        sertilleggNetto: Int = 0,
        /** super SisteBeregning */
            virkDato: Date? = null,
        tt_anv: Int = 0,
        resultatType: ResultatTypeCti? = null,
        sivilstandType: SivilstandTypeCti? = null,
        benyttetSivilstand: BorMedTypeCti? = null
    ) : super(
            virkDato = virkDato,
            tt_anv = tt_anv,
            resultatType = resultatType,
            sivilstandType = sivilstandType,
            benyttetSivilstand = benyttetSivilstand
    ) {
        this.et = et
        this.tt = tt
        this.uforeEkstra = uforeEkstra
        this.konverteringsgrunnlagOrdinert = konverteringsgrunnlagOrdinert
        this.konverteringsgrunnlagGJT = konverteringsgrunnlagGJT
        this.konverteringsgrunnlagUforedel = konverteringsgrunnlagUforedel
        this.konverteringsgrunnlagYrkesskade = konverteringsgrunnlagYrkesskade
        this.anvendtTrygdetidFolketrygd = anvendtTrygdetidFolketrygd
        this.minstepensjonType = minstepensjonType
        this.resultatKilde = resultatKilde
        this.sertilleggNetto = sertilleggNetto
    }

}
