package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

/**
 * Ytelseskomponent for uføretrygd.
 *
 * @author Steinar Hjellvik (Decisive) PK-6172
 * @author Aasmund Nordstoga (Accenture) PK-9029
 * @author Tatyana Lochehina PK-13673
 */
class UforetrygdOrdiner : Ytelseskomponent, UforetrygdYtelseskomponent {

    /**
     * Brukers minsteytelse.
     */
    var minsteytelse: Minsteytelse? = null

    /**
     * Brukers uføretrygd før inntektsavkorting.
     */
    var egenopptjentUforetrygd: EgenopptjentUforetrygd? = null

    /**
     * Angir om egenopptjentUforetrygd er best.
     */
    var egenopptjentUforetrygdBest: Boolean = false

    /**
     * Angir detaljer rundt inntektsavkortingen.
     */
    var avkortingsinformasjon: AvkortingsinformasjonUT? = null

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
     * Utrykker avviket mellom lignet og forventet beløp ved etteroppgjør.
     */
    var periodisertAvvikEtteroppgjor: Double = 0.0

    /**
     * Angir fradragPerAr dersom det ikke hadde vært arbeidsforsøk i perioden.
     */
    var fradragPerArUtenArbeidsforsok: Double = 0.0

    /**
     * Årsbeløp for delytelsen fra tidligere vedtak (fra tilsvarende beregningsperiode)
     */
    override var tidligereBelopAr: Int = 0

    constructor(ut: UforetrygdOrdiner) : super(ut) {
        ytelsekomponentType = YtelsekomponentTypeCti("UT_ORDINER")
        egenopptjentUforetrygdBest = ut.egenopptjentUforetrygdBest
        nettoAkk = ut.nettoAkk
        nettoRestAr = ut.nettoRestAr
        avkortningsbelopPerAr = ut.avkortningsbelopPerAr

        if (ut.minsteytelse != null) {
            minsteytelse = Minsteytelse(ut.minsteytelse!!)
        }
        if (ut.egenopptjentUforetrygd != null) {
            egenopptjentUforetrygd = EgenopptjentUforetrygd(ut.egenopptjentUforetrygd!!)
        }
        if (ut.avkortingsinformasjon != null) {
            avkortingsinformasjon = AvkortingsinformasjonUT(ut.avkortingsinformasjon!!)
        }
        periodisertAvvikEtteroppgjor = ut.periodisertAvvikEtteroppgjor
        fradragPerArUtenArbeidsforsok = ut.fradragPerArUtenArbeidsforsok
        tidligereBelopAr = ut.tidligereBelopAr
    }

    @JvmOverloads
    constructor(
            minsteytelse: Minsteytelse? = null,
            egenopptjentUforetrygd: EgenopptjentUforetrygd? = null,
            egenopptjentUforetrygdBest: Boolean = false,
            avkortingsinformasjon: AvkortingsinformasjonUT? = null,
            nettoAkk: Int = 0,
            nettoRestAr: Int = 0,
            avkortningsbelopPerAr: Int = 0,
            periodisertAvvikEtteroppgjor: Double = 0.0,
            fradragPerArUtenArbeidsforsok: Double = 0.0,
            tidligereBelopAr: Int = 0,
            /** super Ytelseskomponent*/
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("UT_ORDINER"),
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
    )  {
        this.minsteytelse = minsteytelse
        this.egenopptjentUforetrygd = egenopptjentUforetrygd
        this.egenopptjentUforetrygdBest = egenopptjentUforetrygdBest
        this.avkortingsinformasjon = avkortingsinformasjon
        this.nettoAkk = nettoAkk
        this.nettoRestAr = nettoRestAr
        this.avkortningsbelopPerAr = avkortningsbelopPerAr
        this.periodisertAvvikEtteroppgjor = periodisertAvvikEtteroppgjor
        this.fradragPerArUtenArbeidsforsok = fradragPerArUtenArbeidsforsok
        this.tidligereBelopAr = tidligereBelopAr
        this.ytelsekomponentType = YtelsekomponentTypeCti("UT_ORDINER")
    }
}
