package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningMetodeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.JustertPeriodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ResultatTypeCti
import java.io.Serializable

class BeregningsInformasjon : Serializable {
    @JsonIgnore var epsMottarPensjon: Boolean = false
    @JsonIgnore var epsOver2G: Boolean = false // SIMDOM-ADD

    var delingstallUttak: Double = 0.0
    var delingstall67: Double = 0.0

    // Avdødes tilleggspensjon
    var tp: Double = 0.0
    var ttBeregnetForGrunnlagsrolle: Int = 0
    var ungUforGarantiFrafalt: Boolean = false

    var forholdstallUttak: Double = 0.0
    var forholdstall67: Double = 0.0
    var spt: Sluttpoengtall? = null
    var opt: Sluttpoengtall? = null
    var ypt: Sluttpoengtall? = null
    var grunnpensjonAvkortet: Boolean = false
    var gpAvkortingsArsakList: MutableList<Merknad> = mutableListOf()
    var mottarMinstePensjonsniva: Boolean = false
    var minstepensjonArsak: String? = null
    var rettPaGjenlevenderett: Boolean = false
    var gjenlevenderettAnvendt: Boolean = false
    var avdodesTilleggspensjonBrukt: Boolean = false
    var avdodesTrygdetidBrukt: Boolean = false
    var ungUfor: Boolean = false
    var ungUforAnvendt: Boolean = false
    var yrkesskadeRegistrert: Boolean = false
    var yrkesskadeAnvendt: Boolean = false
    var yrkesskadegrad: Int = 0
    var penPerson: PenPerson? = null
    var beregningsMetode: BeregningMetodeTypeCti? = null
    var eksport: Boolean = false
    var resultatType: ResultatTypeCti? = null
    var tapendeBeregningsmetodeListe: MutableList<TapendeBeregningsmetode> = mutableListOf()
    var trygdetid: Int = 0
    var tt_anv: Int = 0
    var vurdertBosattland: LandCti? = null
    var ensligPensjonInstOpph: Boolean = false
    var instOppholdType: JustertPeriodeCti? = null
    var instOpphAnvendt: Boolean = false

    constructor()

    constructor(bi: BeregningsInformasjon) : this() {
        if (bi.penPerson != null) {
            penPerson = PenPerson(bi.penPerson!!)
        }
        if (bi.resultatType != null) {
            resultatType = ResultatTypeCti(bi.resultatType)
        }
        if (bi.beregningsMetode != null) {
            beregningsMetode = BeregningMetodeTypeCti(bi.beregningsMetode)
        }
        for (m in bi.tapendeBeregningsmetodeListe) {
            tapendeBeregningsmetodeListe.add(TapendeBeregningsmetode(m))
        }
        tt_anv = bi.tt_anv
        trygdetid = bi.trygdetid
        delingstall67 = bi.delingstall67
        delingstallUttak = bi.delingstallUttak
        ensligPensjonInstOpph = bi.ensligPensjonInstOpph
        instOppholdType = bi.instOppholdType
        if (bi.instOppholdType != null) {
            instOppholdType = JustertPeriodeCti(bi.instOppholdType)
        }
        instOpphAnvendt = bi.instOpphAnvendt
        vurdertBosattland = bi.vurdertBosattland

        forholdstall67 = bi.forholdstall67
        forholdstallUttak = bi.forholdstallUttak
        mottarMinstePensjonsniva = bi.mottarMinstePensjonsniva
        minstepensjonArsak = bi.minstepensjonArsak
        if (bi.spt != null) {
            spt = Sluttpoengtall(bi.spt!!)
        }
        if (bi.opt != null) {
            opt = Sluttpoengtall(bi.opt!!)
        }
        if (bi.ypt != null) {
            ypt = Sluttpoengtall(bi.ypt!!)
        }
        grunnpensjonAvkortet = bi.grunnpensjonAvkortet
        for (m in bi.gpAvkortingsArsakList) {
            gpAvkortingsArsakList.add(Merknad(m))
        }
        rettPaGjenlevenderett = bi.rettPaGjenlevenderett
        gjenlevenderettAnvendt = bi.gjenlevenderettAnvendt
        avdodesTilleggspensjonBrukt = bi.avdodesTilleggspensjonBrukt
        avdodesTrygdetidBrukt = bi.avdodesTrygdetidBrukt
        ungUfor = bi.ungUfor
        ungUforAnvendt = bi.ungUforAnvendt
        yrkesskadeRegistrert = bi.yrkesskadeRegistrert
        yrkesskadeAnvendt = bi.yrkesskadeAnvendt
        yrkesskadegrad = bi.yrkesskadegrad
        ttBeregnetForGrunnlagsrolle = bi.ttBeregnetForGrunnlagsrolle
        ungUforGarantiFrafalt = bi.ungUforGarantiFrafalt
        eksport = bi.eksport
        // SIMDOM-ADD:
        epsMottarPensjon = bi.epsMottarPensjon
        epsOver2G = bi.epsOver2G
        unclearedDelingstallUttak = bi.unclearedDelingstallUttak
        unclearedDelingstall67 = bi.unclearedDelingstall67
        // end SIMDOM-ADD
    }

    constructor(
        delingstallUttak: Double = 0.0,
        delingstall67: Double = 0.0,
        tp: Double = 0.0,
        ttBeregnetForGrunnlagsrolle: Int = 0,
        ungUforGarantiFrafalt: Boolean = false,
        /** interface IBeregningsInformasjon2011 */
        forholdstall67: Double = 0.0,
        avdodesTilleggspensjonBrukt: Boolean = false,
        forholdstallUttak: Double = 0.0,
        spt: Sluttpoengtall? = null,
        opt: Sluttpoengtall? = null,
        ypt: Sluttpoengtall? = null,
        grunnpensjonAvkortet: Boolean = false,
        gpAvkortingsArsakList: MutableList<Merknad> = mutableListOf(),
        mottarMinstePensjonsniva: Boolean = false,
        minstepensjonArsak: String? = null,
        rettPaGjenlevenderett: Boolean = false,
        gjenlevenderettAnvendt: Boolean = false,
        avdodesTrygdetidBrukt: Boolean = false,
        ungUfor: Boolean = false,
        ungUforAnvendt: Boolean = false,
        yrkesskadeRegistrert: Boolean = false,
        yrkesskadeAnvendt: Boolean = false,
        yrkesskadegrad: Int = 0,
        /** interface IBeregningsInformasjon */
        penPerson: PenPerson? = null,
        beregningsMetode: BeregningMetodeTypeCti? = null,
        ensligPensjonInstOpph: Boolean = false,
        instOppholdType: JustertPeriodeCti? = null,
        instOpphAnvendt: Boolean = false,
        resultatType: ResultatTypeCti? = null,
        tapendeBeregningsmetodeListe: MutableList<TapendeBeregningsmetode> = mutableListOf(),
        trygdetid: Int = 0,
        tt_anv: Int = 0,
        vurdertBosattland: LandCti? = null,
        eksport: Boolean = false
    ) {
        this.forholdstallUttak = forholdstallUttak
        this.forholdstall67 = forholdstall67
        this.delingstallUttak = delingstallUttak
        this.delingstall67 = delingstall67
        this.spt = spt
        this.opt = opt
        this.ypt = ypt
        this.grunnpensjonAvkortet = grunnpensjonAvkortet
        this.gpAvkortingsArsakList = gpAvkortingsArsakList
        this.mottarMinstePensjonsniva = mottarMinstePensjonsniva
        this.minstepensjonArsak = minstepensjonArsak
        this.rettPaGjenlevenderett = rettPaGjenlevenderett
        this.gjenlevenderettAnvendt = gjenlevenderettAnvendt
        this.avdodesTilleggspensjonBrukt = avdodesTilleggspensjonBrukt
        this.avdodesTrygdetidBrukt = avdodesTrygdetidBrukt
        this.ungUfor = ungUfor
        this.ungUforAnvendt = ungUforAnvendt
        this.yrkesskadeRegistrert = yrkesskadeRegistrert
        this.yrkesskadeAnvendt = yrkesskadeAnvendt
        this.yrkesskadegrad = yrkesskadegrad
        this.penPerson = penPerson
        this.beregningsMetode = beregningsMetode
        this.eksport = eksport
        this.resultatType = resultatType
        this.tapendeBeregningsmetodeListe = tapendeBeregningsmetodeListe
        this.trygdetid = trygdetid
        this.tt_anv = tt_anv
        this.vurdertBosattland = vurdertBosattland
        this.ensligPensjonInstOpph = ensligPensjonInstOpph
        this.instOppholdType = instOppholdType
        this.instOpphAnvendt = instOpphAnvendt
        this.tp = tp
        this.ttBeregnetForGrunnlagsrolle = ttBeregnetForGrunnlagsrolle
        this.ungUforGarantiFrafalt = ungUforGarantiFrafalt
    }

    // SIMDOM-ADD
    @JsonIgnore private var unclearedDelingstallUttak: Double? = null
    @JsonIgnore private var unclearedDelingstall67: Double? = null

    val internDelingstallUttak: Double
        @JsonIgnore get() = unclearedDelingstallUttak ?: delingstallUttak

    val internDelingstall67: Double
        @JsonIgnore get() = unclearedDelingstall67 ?: delingstall67

    // delingstallUttak, delingstall67 are not mapped in legacy simulering, hence set to zero:
    fun clearDelingstall() {
        unclearedDelingstallUttak = delingstallUttak
        delingstallUttak = 0.0
        unclearedDelingstall67 = delingstall67
        delingstall67 = 0.0
    }
}
