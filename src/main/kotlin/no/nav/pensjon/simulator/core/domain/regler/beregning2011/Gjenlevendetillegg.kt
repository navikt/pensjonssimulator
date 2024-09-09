package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

/**
 * @author Steinar Hjellvik (Decisive) PK-7113
 * @author Swiddy de Louw (Capgemini) PK-7113
 * @author Steinar Hjellvik (Decisive) - PK-11391
 * @author Swiddy de Louw (Capgemini) - PK-11041
 * @author Tatyana Lochehina PK-13673
 */
class Gjenlevendetillegg : Ytelseskomponent, UforetrygdYtelseskomponent {

    /**
     * Årsbeløp for delytelsen fra tidligere vedtak (fra tilsvarende beregningsperiode)
     */
    override var tidligereBelopAr: Int = 0

    /**
     * Årsbeløpet fra knvertert beregningsgrunnlag.
     */
    var bgKonvertert: Double = 0.0

    /**
     * Årsbeløpet fra konvertertberegningsgrunnlagGJT
     */
    var bgGjenlevendetillegg: Double = 0.0

    /**
     * Akkumulert netto hittil i året eksklusiv måned for beregningsperiodens fomDato.
     */
    var nettoAkk: Int = 0

    /**
     * gjenstående beløp brukeren har rett på for året som beregningsperioden starter,
     * og inkluderer måneden det beregnes fra.
     */
    var nettoRestAr: Int = 0

    /**
     * Inntektsavkortningsbeløp per år, før justering med differansebeløp
     */
    var avkortningsbelopPerAr: Int = 0

    /**
     * Angir om gjenlevendetillegget er beregnet som konvertert
     * eller iht. nye no.nav.preg.domain.regler.regler for gjenlevendetillegg innvilget fom. 01.01.2015.
     */
    var nyttGjenlevendetillegg: Boolean = false

    /**
     * Hvilken faktor gjenlevendetillegget er avkortet med uten hensyn til justering for tidligere avkortet/justert beløp
     */
    /**
     * @return the avkortingsfaktorGJT
     */
    /**
     */
    var avkortingsfaktorGJT: Double = 0.0

    /**
     * Oppsummering av sentrale felt brukt i utregning av nytt gjenlevendetillegg.
     * Kun satt dersom nyttGjenlevendetillegg er true.
     */
    var gjenlevendetilleggInformasjon: GjenlevendetilleggInformasjon? = null

    /**
     * Utrykker avviket mellom lignet og forventet beløp ved etteroppgjør.
     */
    var periodisertAvvikEtteroppgjor: Double = 0.0

    /**
     * Representerer reduksjon av UFI (brutto uføretrygd) pga eksport.
     */
    var eksportFaktor: Double = 0.0

    constructor(gjenlevendetillegg: Gjenlevendetillegg) : super(gjenlevendetillegg) {
        bgKonvertert = gjenlevendetillegg.bgKonvertert
        bgGjenlevendetillegg = gjenlevendetillegg.bgGjenlevendetillegg
        nettoAkk = gjenlevendetillegg.nettoAkk
        nettoRestAr = gjenlevendetillegg.nettoRestAr
        avkortningsbelopPerAr = gjenlevendetillegg.avkortningsbelopPerAr
        nyttGjenlevendetillegg = gjenlevendetillegg.nyttGjenlevendetillegg
        avkortingsfaktorGJT = gjenlevendetillegg.avkortingsfaktorGJT
        if (gjenlevendetillegg.gjenlevendetilleggInformasjon != null) {
            gjenlevendetilleggInformasjon = GjenlevendetilleggInformasjon(gjenlevendetillegg.gjenlevendetilleggInformasjon!!)
        }
        periodisertAvvikEtteroppgjor = gjenlevendetillegg.periodisertAvvikEtteroppgjor
        eksportFaktor = gjenlevendetillegg.eksportFaktor
        tidligereBelopAr = gjenlevendetillegg.tidligereBelopAr
    }

    @JvmOverloads
    constructor(
        tidligereBelopAr: Int = 0,
        bgKonvertert: Double = 0.0,
        bgGjenlevendetillegg: Double = 0.0,
        nettoAkk: Int = 0,
        nettoRestAr: Int = 0,
        avkortningsbelopPerAr: Int = 0,
        nyttGjenlevendetillegg: Boolean = false,
        avkortingsfaktorGJT: Double = 0.0,
        gjenlevendetilleggInformasjon: GjenlevendetilleggInformasjon? = null,
        periodisertAvvikEtteroppgjor: Double = 0.0,
        eksportFaktor: Double = 0.0,
        /** super Ytelseskomponent*/
            brutto: Int = 0,
        netto: Int = 0,
        fradrag: Int = 0,
        bruttoPerAr: Double = 0.0,
        nettoPerAr: Double = 0.0,
        fradragPerAr: Double = 0.0,
        ytelsekomponentType: YtelsekomponentTypeCti= YtelsekomponentTypeCti("UT_GJT"),
        merknadListe: MutableList<Merknad> = mutableListOf(),
        fradragsTransaksjon: Boolean = false,
        opphort: Boolean = false,
        sakType: SakTypeCti? = null,
        formelKode: FormelKodeCti? = null,
        reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
            brutto = brutto,
            netto = netto,
            fradrag = fradrag,
            bruttoPerAr = bruttoPerAr,
            nettoPerAr = nettoPerAr,
            fradragPerAr = fradragPerAr,
            ytelsekomponentType = ytelsekomponentType,
            merknadListe = merknadListe,
            fradragsTransaksjon = fradragsTransaksjon,
            opphort = opphort,
            sakType = sakType,
            formelKode = formelKode,
            reguleringsInformasjon = reguleringsInformasjon
    ) {
        this.tidligereBelopAr = tidligereBelopAr
        this.bgKonvertert = bgKonvertert
        this.bgGjenlevendetillegg = bgGjenlevendetillegg
        this.nettoAkk = nettoAkk
        this.nettoRestAr = nettoRestAr
        this.avkortningsbelopPerAr = avkortningsbelopPerAr
        this.nyttGjenlevendetillegg = nyttGjenlevendetillegg
        this.avkortingsfaktorGJT = avkortingsfaktorGJT
        this.gjenlevendetilleggInformasjon = gjenlevendetilleggInformasjon
        this.periodisertAvvikEtteroppgjor = periodisertAvvikEtteroppgjor
        this.eksportFaktor = eksportFaktor
    }
}
