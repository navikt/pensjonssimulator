package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Omsorgsopptjening
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningMetodeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.PoengtallTypeCti

class Poengtall : Comparable<Poengtall>, Omsorgsopptjening {

    /**
     * Pensjonspoeng,
     */
    var pp: Double = 0.0

    /**
     * Anvendt pensjonsgivende inntekt.
     */
    var pia: Int = 0

    /**
     * Pensjonsgivende inntekt.
     */
    var pi: Int = 0

    /**
     * Året for dette poengtallet
     */
    var ar: Int = 0

    /**
     * Angir om poengtallet er brukt i beregningen av sluttpoengtall.
     */
    var bruktIBeregning: Boolean = false

    /**
     * Veiet grunnbeløp
     */
    var gv: Int = 0

    /**
     * Poengtalltype.
     */
    var poengtallType: PoengtallTypeCti? = null

    /**
     * Maks uføregrad for dette året
     */
    var maksUforegrad: Int = 0

    /**
     * Året regnet som poengår.
     */
    @JsonIgnore
    var poengar: Boolean = false

    /**
     * Året regnet som poengår iht. trygdeavtale.
     */
    @JsonIgnore
    var poengarUtland: Boolean = false

    /**
     * Poengtall uten garanti. Internt bruk i PREG.
     */
    @JsonIgnore
    var pp_fa: Double = 0.0

    /**
     * Gradert poengtall. Internt bruk i PREG.
     */
    @JsonIgnore
    var pp_gradert: Double = 0.0

    /**
     * Omregnet poengtall. Internt bruk i PREG.
     */
    @JsonIgnore
    var pp_omregnet: Double = 0.0

    /**
     * Uførepensjon faktor (grad). Internt bruk i PREG.
     */
    @JsonIgnore
    var up_faktor: Double = 0.0

    /**
     * Yrkesskadepensjon faktor (grad). Internt bruk i PREG.
     */
    @JsonIgnore
    var ysk_faktor: Double = 0.0

    /**
     * Angir om året er et uføreår.
     */
    var uforear: Boolean = false

    var merknadListe: MutableList<Merknad> = mutableListOf() // SIMDOM-MOVE

    /**
     * Angir om poengtallet er blitt avkortet i henhold til no.nav.preg.domain.regler.regler for f92.
     * Benyttes i sammenheng med beregning av FPP. Internt bruk i PREG.
     */
    @JsonIgnore
    var avkortet: Boolean = false

    /**
     * Angir om poengtallet er i et omsorgsår
     */
    override var omsorg: Boolean = false

    /**
     * Angir hvilken beregningsmetode (folketrygd, eøs, nordisk, ...) det
     * opptjente poenget forbindes med. Internt bruk i PREG.
     */
    @JsonIgnore
    var beregningsmetode: BeregningMetodeTypeCti? = null

    @JsonIgnore
    var omregnet: Boolean = false

    @JsonIgnore
    var ikkeProrata: Boolean = false

    @JsonIgnore
    var ikkeAlternativProrata: Boolean = false

    /**
     * Brutto pensjonspoeng.
     */
    @JsonIgnore
    var brp: Double = 0.0

    /**
     * Faktiske brutto pensjonspoeng. Ikke oppjustert av omsorgspoeng.
     */
    @JsonIgnore
    var brp_fa: Double = 0.0

    /**
     * Angir om året beregnes tilsvarende uførestartår. Gjelder historiske uføreperioder.
     */
    @JsonIgnore
    var tilsvarerUforear: Boolean = false

    /**
     * Angir den effektive FPP som blir godskrevet i året.
     */
    @JsonIgnore
    var effektivFPP: Double = 0.0

    /**
     * Angir den effektive PAA som blir godskrevet i året.
     */
    @JsonIgnore
    var effektivPAA: Double = 0.0

    /**
     * {@inheritDoc}
     */
    override val opptjeningsar: Int
        get() = ar

    /**
     * Trengs for å implementere Omsorgsopptjening
     */
    @Suppress("UNUSED_PARAMETER")
    //Skal ikke gjøre noe.
    override var inntektIAvtaleland: Boolean
        get() = false
        set(inntektIAvtaleland) {}
    override val verdi: Double
        get() = pp

    @Suppress("UNUSED_PARAMETER")
    //Her skal intet skje.
    override var justertBelop: Double
        get() = 0.0
        set(justertBelop) {}

    constructor(poengtall: Poengtall) {
        pp = poengtall.pp
        pia = poengtall.pia
        pi = poengtall.pi
        ar = poengtall.ar
        bruktIBeregning = poengtall.bruktIBeregning
        gv = poengtall.gv
        if (poengtall.poengtallType != null) {
            poengtallType = PoengtallTypeCti(poengtall.poengtallType)
        }
        maksUforegrad = poengtall.maksUforegrad
        pp_fa = poengtall.pp_fa
        pp_gradert = poengtall.pp_gradert
        pp_omregnet = poengtall.pp_omregnet
        up_faktor = poengtall.up_faktor
        ysk_faktor = poengtall.ysk_faktor
        uforear = poengtall.uforear
        avkortet = poengtall.avkortet
        this.omsorg = poengtall.omsorg
        if (poengtall.beregningsmetode != null) {
            beregningsmetode = BeregningMetodeTypeCti(poengtall.beregningsmetode)
        }
        omregnet = poengtall.omregnet
        ikkeProrata = poengtall.ikkeProrata
        ikkeAlternativProrata = poengtall.ikkeAlternativProrata
        brp = poengtall.brp
        brp_fa = poengtall.brp_fa
        tilsvarerUforear = poengtall.tilsvarerUforear
        effektivFPP = poengtall.effektivFPP
        effektivPAA = poengtall.effektivPAA
        merknadListe.clear()
        for (merknad in poengtall.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }
    }

    constructor(
        pp: Double,
        pia: Int,
        pi: Int,
        ar: Int,
        bruktIBeregning: Boolean,
        gv: Int,
        poengtallType: PoengtallTypeCti?,
        maksUforegrad: Int
    ) : this() {
        this.pp = pp
        this.pia = pia
        this.pi = pi
        this.ar = ar
        this.bruktIBeregning = bruktIBeregning
        this.gv = gv
        this.poengtallType = poengtallType
        if (poengtallType != null) {
            this.omsorg = poengtallType.kode == "J" || poengtallType.kode == "K" || poengtallType.kode == "L"
        }
        this.maksUforegrad = maksUforegrad
    }

    constructor() : super()

    constructor(
        pp: Double = 0.0,
        pia: Int = 0,
        pi: Int = 0,
        ar: Int = 0,
        bruktIBeregning: Boolean = false,
        gv: Int = 0,
        poengtallType: PoengtallTypeCti? = null,
        maksUforegrad: Int = 0,
        poengar: Boolean = false,
        poengarUtland: Boolean = false,
        pp_fa: Double = 0.0,
        pp_gradert: Double = 0.0,
        pp_omregnet: Double = 0.0,
        up_faktor: Double = 0.0,
        ysk_faktor: Double = 0.0,
        uforear: Boolean = false,
        avkortet: Boolean = false,
        omsorg: Boolean = false,
        beregningsmetode: BeregningMetodeTypeCti? = null,
        omregnet: Boolean = false,
        ikkeProrata: Boolean = false,
        ikkeAlternativProrata: Boolean = false,
        brp: Double = 0.0,
        brp_fa: Double = 0.0,
        tilsvarerUforear: Boolean = false,
        effektivFPP: Double = 0.0,
        effektivPAA: Double = 0.0,
        merknadListe: MutableList<Merknad> = mutableListOf()
    ) {
        this.pp = pp
        this.pia = pia
        this.pi = pi
        this.ar = ar
        this.bruktIBeregning = bruktIBeregning
        this.gv = gv
        this.poengtallType = poengtallType
        this.maksUforegrad = maksUforegrad
        this.poengar = poengar
        this.poengarUtland = poengarUtland
        this.pp_fa = pp_fa
        this.pp_gradert = pp_gradert
        this.pp_omregnet = pp_omregnet
        this.up_faktor = up_faktor
        this.ysk_faktor = ysk_faktor
        this.uforear = uforear
        this.avkortet = avkortet
        this.omsorg = omsorg
        this.beregningsmetode = beregningsmetode
        this.omregnet = omregnet
        this.ikkeProrata = ikkeProrata
        this.ikkeAlternativProrata = ikkeAlternativProrata
        this.brp = brp
        this.brp_fa = brp_fa
        this.tilsvarerUforear = tilsvarerUforear
        this.effektivFPP = effektivFPP
        this.effektivPAA = effektivPAA
        this.merknadListe = merknadListe
        this.inntektIAvtaleland = inntektIAvtaleland
        this.justertBelop = justertBelop
    }

    /**
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    override fun compareTo(other: Poengtall): Int {
        return pp.compareTo(other.pp)
    }
}
