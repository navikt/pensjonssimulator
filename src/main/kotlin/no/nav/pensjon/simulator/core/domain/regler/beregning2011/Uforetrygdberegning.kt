package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.*
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok
import java.util.*

/**
 * @author Swiddy de Louw (Capgemini) - PK-10228
 * @author Aasmund Nordstoga (Accenture) - PKFEIL-2605
 * @author Steinar Hjellvik (Decisive) - PK-11391
 * @author Lars Hartvigsen (Decisive) - PK-12169
 * @author Nabil Safadi (Decisive) - PK-8518
 */
class Uforetrygdberegning : Beregning2011 {
    var bruttoPerAr: Int = 0
    var formelKode: FormelKodeCti? = null
    var minsteytelse: Minsteytelse? = null
    var prorataBrok: Brok? = null
    var uforegrad: Int = 0
    var uforetidspunkt: Date? = null
    var egenopptjentUforetrygd: EgenopptjentUforetrygd? = null
    var egenopptjentUforetrygdBest: Boolean = false
    var yrkesskadegrad: Int = 0
    var yrkesskadetidspunkt: Date? = null
    var mottarMinsteytelse: Boolean = false

    /* Bygger opp årsakskoder som viser hvorfor personen mottar minsteytelse */
    var minsteytelseArsak: String? = null

    @JsonIgnore
    var avtaleBeregningsmetode: String? = null

    /**
     *  Viser hvilken type institusjonsopphold det er beregnet for. Kodene hentes fra K_JUST_PERIODE
     */
    var instOppholdType: JustertPeriodeCti? = null

    /**
     * Angir om ytelsen er endret, enten  økt eller redusert.
     */
    var instOpphAnvendt: Boolean = false

    /**
     * Ekstra informasjon til beregnet uføretrygd.
     * Brukes for at PREG skal beregne en uførehistorikk for uføretrygd.
     */
    var uforeEkstra: UforeEkstraUT? = null

    /**
     * Satt på de beregninger hvor avdødes ytelse har påvirket beregningen.
     */
    var ytelseVedDod: YtelseVedDodCti? = null

    constructor() : super()

    /**
     * Kopi-konstruktør som kan kopiere delberegninger.
     *
     * @param b beregningene som skal kopieres.
     * @param kopierDelberegning2011Liste settes til true dersom delberegninger skal kopieres.
     */
    constructor(b: Uforetrygdberegning, kopierDelberegning2011Liste: Boolean) : super(b, kopierDelberegning2011Liste) {
        copyCommonFields(b)
    }

    /**
     * Kopi-konstruktør som benyttes i normaltilfellet der man ikke ønsker å eksplisitt kopiere delberegninger.
     */
    constructor(b: Uforetrygdberegning) : super(b, false) {
        copyCommonFields(b)
    }

    constructor(
        bruttoPerAr: Int = 0,
        formelKode: FormelKodeCti? = null,
        minsteytelse: Minsteytelse? = null,
        prorataBrok: Brok? = null,
        uforegrad: Int = 0,
        uforetidspunkt: Date? = null,
        egenopptjentUforetrygd: EgenopptjentUforetrygd? = null,
        egenopptjentUforetrygdBest: Boolean = false,
        yrkesskadegrad: Int = 0,
        yrkesskadetidspunkt: Date? = null,
        mottarMinsteytelse: Boolean = false,
        avtaleBeregningsmetode: String? = null,
        instOppholdType: JustertPeriodeCti? = null,
        instOpphAnvendt: Boolean = false,
        uforeEkstra: UforeEkstraUT? = null,
        ytelseVedDod: YtelseVedDodCti? = null,
        /** super beregning2011 */
            gjelderPerson: PenPerson? = null,
        grunnbelop: Int = 0,
        tt_anv: Int = 0,
        resultatType: ResultatTypeCti? = null,
        beregningsMetode: BeregningMetodeTypeCti? = null,
        beregningType: BeregningTypeCti? = null,
        delberegning2011Liste: MutableList<BeregningRelasjon> = mutableListOf(),
        merknadListe: MutableList<Merknad> = mutableListOf(),
        beregningGjelderType: BeregningGjelderTypeCti? = null,
        beregningsnavn: String = "Ukjentnavn",
        beregningsrelasjon: BeregningRelasjon? = null,
        delberegning1967: BeregningRelasjon? = null
    ) : super(
            gjelderPerson = gjelderPerson,
            grunnbelop = grunnbelop,
            tt_anv = tt_anv,
            resultatType = resultatType,
            beregningsMetode = beregningsMetode,
            beregningType = beregningType,
            delberegning2011Liste = delberegning2011Liste,
            merknadListe = merknadListe,
            beregningGjelderType = beregningGjelderType,
            beregningsnavn = beregningsnavn,
            beregningsrelasjon = beregningsrelasjon,
            delberegning1967 = delberegning1967
    ) {
        this.bruttoPerAr = bruttoPerAr
        this.formelKode = formelKode
        this.minsteytelse = minsteytelse
        this.prorataBrok = prorataBrok
        this.uforegrad = uforegrad
        this.uforetidspunkt = uforetidspunkt
        this.egenopptjentUforetrygd = egenopptjentUforetrygd
        this.egenopptjentUforetrygdBest = egenopptjentUforetrygdBest
        this.yrkesskadegrad = yrkesskadegrad
        this.yrkesskadetidspunkt = yrkesskadetidspunkt
        this.mottarMinsteytelse = mottarMinsteytelse
        this.avtaleBeregningsmetode = avtaleBeregningsmetode
        this.instOppholdType = instOppholdType
        this.instOpphAnvendt = instOpphAnvendt
        this.uforeEkstra = uforeEkstra
        this.ytelseVedDod = ytelseVedDod
    }

    private fun copyCommonFields(b: Uforetrygdberegning) {
        bruttoPerAr = b.bruttoPerAr
        grunnbelop = b.grunnbelop
        uforegrad = b.uforegrad
        egenopptjentUforetrygdBest = b.egenopptjentUforetrygdBest
        yrkesskadegrad = b.yrkesskadegrad
        avtaleBeregningsmetode = b.avtaleBeregningsmetode
        mottarMinsteytelse = b.mottarMinsteytelse
        minsteytelseArsak = b.minsteytelseArsak
        instOpphAnvendt = b.instOpphAnvendt

        if (b.formelKode != null) {
            formelKode = FormelKodeCti(b.formelKode!!)
        }
        if (b.uforetidspunkt != null) {
            uforetidspunkt = b.uforetidspunkt!!.clone() as Date
        }
        if (b.minsteytelse != null) {
            minsteytelse = Minsteytelse(b.minsteytelse!!)
        }
        if (b.egenopptjentUforetrygd != null) {
            egenopptjentUforetrygd = EgenopptjentUforetrygd(b.egenopptjentUforetrygd!!)
        }
        if (b.instOppholdType != null) {
            instOppholdType = JustertPeriodeCti(b.instOppholdType)
        }
        if (b.yrkesskadetidspunkt != null) {
            yrkesskadetidspunkt = b.yrkesskadetidspunkt!!.clone() as Date
        }
        if (b.prorataBrok != null) {
            prorataBrok = Brok(b.prorataBrok!!)
        }
        if (b.uforeEkstra != null) {
            uforeEkstra = UforeEkstraUT(b.uforeEkstra!!)
        }
        if (b.ytelseVedDod != null) {
            this.ytelseVedDod = YtelseVedDodCti(b.ytelseVedDod)
        }
    }

}
