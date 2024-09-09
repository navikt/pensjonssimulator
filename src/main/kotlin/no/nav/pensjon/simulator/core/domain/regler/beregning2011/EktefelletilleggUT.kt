package no.nav.pensjon.simulator.core.domain.regler.beregning2011

/** PREG domeneklasse for ytelseskomponent Ektefelletillegg Uføretrygd
 *
 * @author Magnus Bakken - PKFEIL-3285: Feilretting av kopikonstruktør.
 * @author Swiddy de Louw - PKYTON-369 PK-6582 / PK-7106
 */

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ektefelletillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti
import java.io.Serializable

class EktefelletilleggUT : Ytelseskomponent, UforetrygdYtelseskomponent, Serializable {

    /**
     * Årsbeløp for delytelsen fra tidligere vedtak (fra tilsvarende beregningsperiode)
     */
    override var tidligereBelopAr: Int = 0

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
     * netto ektefelletillegg per måned før konvertering * 12
     */
    var etForSkattekomp: Double = 0.0

    /**
     * brukers oppjusterte uførepensjon før skattekompensasjon
     */
    var upForSkattekomp: Double = 0.0

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("UT_ET"))
    constructor(ektefelletilleggUT: EktefelletilleggUT) : super(ektefelletilleggUT) {
        nettoAkk = ektefelletilleggUT.nettoAkk
        nettoRestAr = ektefelletilleggUT.nettoRestAr
        avkortningsbelopPerAr = ektefelletilleggUT.avkortningsbelopPerAr
        ytelsekomponentType = YtelsekomponentTypeCti("UT_ET")
        etForSkattekomp = ektefelletilleggUT.etForSkattekomp
        upForSkattekomp = ektefelletilleggUT.upForSkattekomp
        tidligereBelopAr = ektefelletilleggUT.tidligereBelopAr
    }

    constructor(ektefelletillegg: Ektefelletillegg) : super(ektefelletillegg) {
        ytelsekomponentType = YtelsekomponentTypeCti("UT_ET")
    }

    constructor(
            tidligereBelopAr: Int = 0,
            nettoAkk: Int = 0,
            nettoRestAr: Int = 0,
            avkortningsbelopPerAr: Int = 0,
            etForSkattekomp: Double = 0.0,
            upForSkattekomp: Double = 0.0,
            /** super Ytelseskomponent*/
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("UT_ET"),
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
        this.nettoAkk = nettoAkk
        this.nettoRestAr = nettoRestAr
        this.avkortningsbelopPerAr = avkortningsbelopPerAr
        this.etForSkattekomp = etForSkattekomp
        this.upForSkattekomp = upForSkattekomp
    }
}
