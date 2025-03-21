package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Omsorgsopptjening
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

// 2025-03-10
/**
 * NB: Modified to avoid UnrecognizedPropertyException.
 */
class OpptjeningUT : Omsorgsopptjening {
    /**
     * Angir om avkortet mot 6*grunnbeløp ved virk.
     */
    var avkortetBelop = 0.0

    /**
     * Angir om et av de tre årene som er brukt.
     */
    var brukt = false

    var inntektIAvtaleland: Boolean = false

    val opptjeningsar: Int = 0 // = ar

    val verdi: Double = 0.0 // = avkortetBelop

    /**
     * Inntekten for et år ganget med forholdet mellom grunnbeløpet ved virk
     * og gjennomsnittlig grunnbeløp for inntektsåret.
     */
    var justertBelop: Double = 0.0

    val omsorg: Boolean = false // = omsorgsar

    /**
     * Pensjonsgivende inntekt.
     */
    var pgi = 0

    /**
     * Gjennomsnittlig G for inntektsåret.
     */
    var veietG = 0

    /**
     * Hvilket årstall.
     */
    var ar = 0

    var merknadListe: List<Merknad> = mutableListOf()
    var formelkodeEnum: FormelKodeEnum? = null

    /**
     * Opptjeningsgaranti ved Førstegangstjeneste.
     */
    var forstegangstjeneste = 0

    /**
     * Beregnet inntekt for året.
     */
    var belop = 0

    /**
     * Minste beløp for fastsettelse av justert PGI.
     */
    var garantiBelop = 0

    /**
     * Angir om det finnes omsorgsopptjening for året.
     */
    var omsorgsar = false

    /**
     * Pensjonsgivende inntekt justert i henhold til
     * gjennomsnittlig stillingsprosent for året.
     */
    var justertPGI = 0

    /**
     * Opptjening fra uførepensjon eller uføretrygd
     */
    var uforeopptjening = 0.0

    constructor()

    constructor(source: OpptjeningUT) : this() {
        avkortetBelop = source.avkortetBelop
        brukt = source.brukt
        inntektIAvtaleland = source.inntektIAvtaleland
        justertBelop = source.justertBelop
        pgi = source.pgi
        veietG = source.veietG
        ar = source.ar
        merknadListe = source.merknadListe.map { it.copy() }
        formelkodeEnum = source.formelkodeEnum
        forstegangstjeneste = source.forstegangstjeneste
        belop = source.belop
        garantiBelop = source.garantiBelop
        omsorgsar = source.omsorgsar
        justertPGI = source.justertPGI
        uforeopptjening = source.uforeopptjening
    }
}
