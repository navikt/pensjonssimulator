package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Omsorgsopptjening
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.OpptjeningTypeMapping
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti

class OpptjeningUT() : Comparable<OpptjeningUT>, Omsorgsopptjening {
    /**
     * Angir om avkortet mot 6*grunnbeløp ved virk.
     */
    var avkortetBelop: Double = 0.0

    /**
     * Angir om et av de tre årene som er brukt.
     */
    var brukt: Boolean = false

    /**
     * Angir om det er inntekt i avtaleland som angår den konvensjon som beregningsgrunnlaget inngår i.
     */
    override var inntektIAvtaleland: Boolean = false

    /**
     * Inntekten for et år ganget med forholdet mellom grunnbeløpet ved virk
     * og gjennomsnittlig grunnbeløp for inntektsåret.
     */
    override var justertBelop: Double = 0.0

    /**
     * Pensjonsgivende inntekt.
     */
    var pgi: Int = 0

    /**
     * Gjennomsnittlig G for inntektsåret.
     */
    var veietG: Int = 0

    /**
     * Hvilket årstall.
     */
    var ar: Int = 0

    var merknadListe: MutableList<Merknad> = mutableListOf()

    var formelkode: FormelKodeCti? = null

    /**
     * Opptjeningsgaranti ved førstegangstjeneste.
     */
    var forstegangstjeneste: Int = 0

    /**
     * Beregnet inntekt for året.
     */
    var belop: Int = 0

    /**
     * Minste beløp for fastsettelse av justert PGI.
     */
    var garantiBelop: Int = 0

    /**
     * Angir om det finnes omsorgsopptjening for året.
     */

    var omsorgsar: Boolean = false

    /**
     * Pensjonsgivende inntekt justert i henhold til
     * gjennomsnittlig stillingsprosent for året.
     */
    var justertPGI: Int = 0

    /**
     * Opptjening fra uførepensjon eller uføretrygd
     */
    var uforeopptjening: Double = 0.0

    /**
     * Inneholder alle inntektstyper for dette året
     */
    @JsonIgnore
    var opptjeningTypeListe: List<OpptjeningTypeMapping> = mutableListOf()

    /**
     * Maks uføregrad for dette året.
     */
    @JsonIgnore
    var maksUforegrad: Int = 0

    /**
     * Maks yrkesskadegrad for dette året.
     */
    @JsonIgnore
    var maksYrkesskadegrad: Int = 0

    /**
     * Peker på den uføreperiode som er relevant for året, hvis noen.
     */
    @JsonIgnore
    var uforeperiode: Uforeperiode? = null

    /**
     * {@inheritDoc}
     */
    override val opptjeningsar: Int
        get() = ar

    /**
     * {@inheritDoc}
     */
    override val verdi: Double
        get() = avkortetBelop

    /**
     * {@inheritDoc}
     */
    override val omsorg: Boolean
        get() = omsorgsar

    constructor(opptjeningUT: OpptjeningUT) : this() {
        avkortetBelop = opptjeningUT.avkortetBelop
        brukt = opptjeningUT.brukt
        justertBelop = opptjeningUT.justertBelop
        pgi = opptjeningUT.pgi
        veietG = opptjeningUT.veietG
        ar = opptjeningUT.ar
        forstegangstjeneste = opptjeningUT.forstegangstjeneste
        belop = opptjeningUT.belop
        garantiBelop = opptjeningUT.garantiBelop
        omsorgsar = opptjeningUT.omsorgsar
        inntektIAvtaleland = opptjeningUT.inntektIAvtaleland
        for (merknad in opptjeningUT.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }
        opptjeningTypeListe = opptjeningUT.opptjeningTypeListe.map { OpptjeningTypeMapping(it) }

        if (opptjeningUT.formelkode != null) {
            formelkode = FormelKodeCti(opptjeningUT.formelkode!!)
        }
        justertPGI = opptjeningUT.justertPGI
        uforeopptjening = opptjeningUT.uforeopptjening
        maksUforegrad = opptjeningUT.maksUforegrad
        maksYrkesskadegrad = opptjeningUT.maksYrkesskadegrad
    }

    /**
     * Constructor for å initialisere felter som brukes av interface Omsorgsopptjening.
     * Opprettet av hensyn til testbarhet.
     */
    constructor(opptjeningsar: Int, verdi: Double, omsorg: Boolean, inntektIAvtaleland: Boolean) : this() {
        ar = opptjeningsar
        avkortetBelop = verdi
        omsorgsar = omsorg
        this.inntektIAvtaleland = inntektIAvtaleland
    }

    /**
     * Constructor for å initialisere felter som brukes av interface Omsorgsopptjening.
     * Opprettet av hensyn til testbarhet.
     */
    constructor(
        opptjeningsar: Int,
        verdi: Double,
        omsorg: Boolean,
        inntektIAvtaleland: Boolean,
        justertBelop: Double
    ) : this() {
        ar = opptjeningsar
        avkortetBelop = verdi
        omsorgsar = omsorg
        this.inntektIAvtaleland = inntektIAvtaleland
        this.justertBelop = justertBelop
    }

    constructor(
        avkortetBelop: Double = 0.0,
        brukt: Boolean = false,
        inntektIAvtaleland: Boolean = false,
        justertBelop: Double = 0.0,
        pgi: Int = 0,
        veietG: Int = 0,
        ar: Int = 0,
        merknadListe: MutableList<Merknad> = mutableListOf(),
        formelkode: FormelKodeCti? = null,
        forstegangstjeneste: Int = 0,
        belop: Int = 0,
        garantiBelop: Int = 0,
        omsorgsar: Boolean = false,
        justertPGI: Int = 0,
        uforeopptjening: Double = 0.0,
        opptjeningTypeListe: List<OpptjeningTypeMapping> = mutableListOf(),
        maksUforegrad: Int = 0,
        maksYrkesskadegrad: Int = 0
    ) : this() {
        this.avkortetBelop = avkortetBelop
        this.brukt = brukt
        this.inntektIAvtaleland = inntektIAvtaleland
        this.justertBelop = justertBelop
        this.pgi = pgi
        this.veietG = veietG
        this.ar = ar
        this.merknadListe = merknadListe
        this.formelkode = formelkode
        this.forstegangstjeneste = forstegangstjeneste
        this.belop = belop
        this.garantiBelop = garantiBelop
        this.omsorgsar = omsorgsar
        this.justertPGI = justertPGI
        this.uforeopptjening = uforeopptjening
        this.opptjeningTypeListe = opptjeningTypeListe
        this.maksUforegrad = maksUforegrad
        this.maksYrkesskadegrad = maksYrkesskadegrad
    }

    /**
     * Sorter med største avkortetBelop først. Dersom avkortetBelop
     * er lik sorteres største år først.
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    override fun compareTo(other: OpptjeningUT): Int {
        return if (other.avkortetBelop.compareTo(avkortetBelop) == 0) {
            when {
                other.justertBelop == justertBelop -> when {
                    other.ar == ar -> 0
                    other.ar < ar -> -1
                    else -> 1
                }
                other.justertBelop < justertBelop -> -1
                else -> 1
            }
        } else {
            other.avkortetBelop.compareTo(avkortetBelop)
        }
    }
}
