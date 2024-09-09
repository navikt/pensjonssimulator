package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningUforeperiode
import no.nav.pensjon.simulator.core.domain.regler.kode.FppGarantiKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ProRataBeregningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.UforeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import no.nav.pensjon.simulator.core.result.UfoereType
import java.io.Serializable
import java.util.*

class Uforeperiode(
    /**
     * Uføregraden, heltall 0-100.
     */
    var ufg: Int = 0,
    /**
     * Dato for uføretidspunktet.
     */
    var uft: Date? = null,
    /**
     * Angir om uføregraden er ren uføre,inneholder delvis yrke eller bare yrke.
     */
    var uforeType: UforeTypeCti? = null,

    /**
     * Framtidige pensjonspoengtall garanti, f.eks ung ufør har i dag en garanti på 3.3.
     */
    var fppGaranti: Double = 0.0,
    /**
     * Kode for fpp_garanti<br></br>
     * `A = UNG UFØR SOM VENTER PÅ RETT TIL 3.3 PENSJONSPOENG`<br></br>
     * `B = UNG UFØR MED RETT TIL 3.3 PENSJONSPOENG`<br></br>
     * `C = ung ufør som venter, og som ble ufør 20 år gammel`<br></br>
     * `D = Ung ufør med rett til 3.3 poeng fra mai 1992`<br></br>
     * `E = unge uføre før 1967`
     */
    var fppGarantiKode: FppGarantiKodeCti? = null,

    /**
     * Antall godskrevet framtidig poengtall, ikke full framtidig godskriving.
     */
    var redusertAntFppAr: Int = 0,
    /**
     * Antall godskrevet framtidig poengtall, ikke full framtidig godskriving. EØS eller annen pro-rata beregning.
     */
    var redusertAntFppAr_proRata: Int = 0,
    /**
     * Angir hva utfallet av pro-rata beregningen var. Hvis satt er EØS eneste alternativ eller bedre enn alternativet (Folketrygd).
     */
    var proRataBeregningType: ProRataBeregningTypeCti? = null,
    /**
     * Dato for virkningsåret for denne uføreperioden.
     */
    var virk: Date? = null,

    /**
     * Dato for når uføreperioden avsluttes.
     */
    var uftTom: Date? = null,

    /**
     * Dato for når uføregraden starter.
     */
    var ufgFom: Date? = null,

    /**
     * Dato for når uføregraden avsluttes.
     */
    var ufgTom: Date? = null,

    /**
     * Fødselsår for yngste barn.
     */
    var fodselsArYngsteBarn: Int = 0,

    /**
     * Sluttpoengtall på tilleggspensjonen.
     */
    var spt: Double = 0.0,
    /**
     * Sluttpoengtall på tilleggspensjonen. Pro-rata beregning variant.
     */
    var spt_proRata: Double = 0.0,
    /**
     * Sluttpoengtall på tilleggspensjonen ved overkomp.
     */
    var opt: Double = 0.0,
    /**
     * Sluttpoengtall på tilleggspensjonen ved yrkesskade.
     */
    var ypt: Double = 0.0,
    /**
     * Antall poengår før 1992 på sluttpoengtallet.
     */
    var spt_pa_f92: Int = 0,
    /**
     * Antall poengår etter 1991 på sluttpoengtallet
     */
    var spt_pa_e91: Int = 0,
    /**
     * Antall poengår før 1992 på sluttpoengtallet.
     */
    var proRata_teller: Int = 0,
    /**
     * Antall poengår etter 1991 på sluttpoengtallet
     */
    var proRata_nevner: Int = 0,
    /**
     * Antall poengår før 1992 på sluttpoengtallet med overkomp
     */
    var opt_pa_f92: Int = 0,
    /**
     * Antall poengår etter 1992 på sluttpoengtallet med overkomp
     */
    var opt_pa_e91: Int = 0,
    /**
     * Antall poengår før 1992 på sluttpoengtallet ved yrkesskade
     */
    var ypt_pa_f92: Int = 0,
    /**
     * Antall poengår etter 1992 på sluttpoengtallet ved yrkesskade
     */
    var ypt_pa_e91: Int = 0,
    /**
     * Poengtall ut fra antatt årlig inntekt på skadetidspunktet
     */
    var paa: Double = 0.0,
    /**
     * Fremtidige pensjonspoeng
     */
    var fpp: Double = 0.0,
    /**
     * Fremtidige omregnete pensjonspoeng
     */
    var fpp_omregnet: Double = 0.0,

    /**
     * Sluttpoengtall i EØS
     */
    var spt_eos: Double = 0.0,
    /**
     * Antall poengår etter 1991 etter EØS-alternativet for sluttpoengtall
     */
    var spt_pa_e91_eos: Int = 0,
    /**
     * Antall poengår før 1992 etter EØS-alternativet for sluttpoengtall
     */
    var spt_pa_f92_eos: Int = 0,
    /**
     * Flag som angir om perioden angir en nedsettelse av grad. Intern PREG variabel.
     */

    /**
     * Det beregningsgrunnlag (årsbeløp) som ble gjeldende i perioden.
     * Dette er beregningsgrunnlagOrdinært når uforeType er UFORE eller UF_M_YRKE,
     * og beregningsgrunnlagYrkesskade når type er YRKE
     */
    var beregningsgrunnlag: Int = 0,

    /**
     * Det uføretidspunkt som er angitt for perioden, men ikke nødvendigvis anvendt.
     */
    var angittUforetidspunkt: Date? = null,

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 19).
     */
    var antattInntektFaktorKap19: Double = 0.0,

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 20).
     */
    var antattInntektFaktorKap20: Double = 0.0,

    @JsonIgnore var nedsattUfg: Boolean = false,

    /**
     * Referanse til påfølgende Uføreperiode. Intern PREG variabel.
     */
    @JsonIgnore var nesteUFP: Uforeperiode? = null,

    /**
     * Referanse til forrige Uføreperiode. Intern PREG variabel.
     */
    @JsonIgnore var forrigeUFP: Uforeperiode? = null,

    /**
     * Referanse til sammenfallende Uføreperiode for yrkesskade. Intern PREG variabel.
     */
    @JsonIgnore var YP_UFP: Uforeperiode? = null,
    /**
     * Angir om perioden er den første uføreperioden (ikke yrkesskade) i historikken.
     * Intern PREG variabel.
     */
    @JsonIgnore var forsteUFP: Boolean = false,
    /**
     * Angir om perioden er den sisste uføreperioden (ikke yrkesskade) i historikken.
     * Intern PREG variabel.
     */
    @JsonIgnore var sissteUFP: Boolean = false,
    /**
     * Angir forrige periode som ga opphav til nytt UFT.
     * Intern PREG variabel.
     */
    @JsonIgnore var forrigeUFT: Uforeperiode? = null,
    /**
     * Neste periode som gir opphav til nytt UFT.
     * Intern PREG variabel.
     */
    @JsonIgnore var nesteUFT: Uforeperiode? = null,
    @JsonIgnore var tilhorendeUFT: Uforeperiode? = null,
    @JsonIgnore var opphor: Boolean = false,
    @JsonIgnore var erUFT: Boolean = false,
    @JsonIgnore var ufgTom_intern: Date? = null,
    @JsonIgnore var beregnSomMellomliggende: Boolean = false,
    @JsonIgnore var beregnSomOpphor: Boolean = false,
    @JsonIgnore var pafolgendeUforear: Int = 0,
    @JsonIgnore var sisteUFP_UP: Uforeperiode? = null
) : Comparable<Uforeperiode>, Serializable {

    constructor(uforeperiode: BeregningUforeperiode) : this() {
        this.ufg = Integer.valueOf(uforeperiode.ufg)
        if (uforeperiode.uft != null) {
            this.uft = uforeperiode.uft!!.clone() as Date
        }
        if (uforeperiode.uforeType != null) {
            this.uforeType = UforeTypeCti(uforeperiode.uforeType)
        }
        this.fppGaranti = uforeperiode.fppGaranti!!
        if (uforeperiode.fppGarantiKode != null) {
            this.fppGarantiKode = FppGarantiKodeCti(uforeperiode.fppGarantiKode)
        }
        this.redusertAntFppAr = Integer.valueOf(uforeperiode.redusertAntFppAr)
        if (uforeperiode.virk != null) {
            this.virk = uforeperiode.virk!!.clone() as Date
        }
        if (uforeperiode.uftTom != null) {
            this.uftTom = uforeperiode.uftTom!!.clone() as Date
        }
        if (uforeperiode.ufgFom != null) {
            this.ufgFom = uforeperiode.ufgFom!!.clone() as Date
        }
        if (uforeperiode.ufgTom != null) {
            this.ufgTom = uforeperiode.ufgTom!!.clone() as Date
        }

        this.fodselsArYngsteBarn = uforeperiode.fodselsArYngsteBarn ?: 0
        this.spt = uforeperiode.spt ?: 0.0
        this.opt = uforeperiode.opt ?: 0.0
        this.ypt = uforeperiode.ypt ?: 0.0
        this.spt_pa_f92 = uforeperiode.spt_pa_f92 ?: 0
        this.spt_pa_e91 = uforeperiode.spt_pa_e91 ?: 0
        this.opt_pa_f92 = uforeperiode.opt_pa_f92 ?: 0
        this.opt_pa_e91 = uforeperiode.opt_pa_e91 ?: 0
        this.ypt_pa_f92 = uforeperiode.ypt_pa_f92 ?: 0
        this.ypt_pa_e91 = uforeperiode.ypt_pa_e91 ?: 0
        this.paa = uforeperiode.paa ?: 0.0
        this.fpp = uforeperiode.fpp ?: 0.0
        this.fpp_omregnet = uforeperiode.fpp_omregnet ?: 0.0
        this.spt_eos = uforeperiode.spt_eos
        this.spt_pa_e91_eos = uforeperiode.spt_pa_e91_eos
        this.spt_pa_f92_eos = uforeperiode.spt_pa_f92_eos
        this.redusertAntFppAr_proRata = uforeperiode.redusertAntFppAr_proRata
        if (uforeperiode.proRataBeregningType != null) {
            this.proRataBeregningType = ProRataBeregningTypeCti(uforeperiode.proRataBeregningType!!.kode)
        }
        this.proRata_teller = uforeperiode.proRata_teller ?: 0
        this.proRata_nevner = uforeperiode.proRata_nevner ?: 0
        this.spt_proRata = uforeperiode.spt_proRata ?: 0.0
        if (uforeperiode.angittUforetidspunkt != null) {
            this.angittUforetidspunkt = uforeperiode.angittUforetidspunkt!!.clone() as Date
        }
        this.beregningsgrunnlag = uforeperiode.beregningsgrunnlag
        this.antattInntektFaktorKap19 = uforeperiode.antattInntektFaktorKap19
        this.antattInntektFaktorKap20 = uforeperiode.antattInntektFaktorKap20
    }

    constructor(uforeperiode: Uforeperiode) : this() {
        this.ufg = Integer.valueOf(uforeperiode.ufg)
        if (uforeperiode.uft != null) {
            this.uft = uforeperiode.uft!!.clone() as Date
        }
        if (uforeperiode.uforeType != null) {
            this.uforeType = UforeTypeCti(uforeperiode.uforeType)
        }
        this.fppGaranti = uforeperiode.fppGaranti
        if (uforeperiode.fppGarantiKode != null) {
            this.fppGarantiKode = FppGarantiKodeCti(uforeperiode.fppGarantiKode)
        }
        this.redusertAntFppAr = Integer.valueOf(uforeperiode.redusertAntFppAr)
        if (uforeperiode.virk != null) {
            this.virk = uforeperiode.virk!!.clone() as Date
        }
        if (uforeperiode.uftTom != null) {
            this.uftTom = uforeperiode.uftTom!!.clone() as Date
        }
        if (uforeperiode.ufgFom != null) {
            this.ufgFom = uforeperiode.ufgFom!!.clone() as Date
        }
        if (uforeperiode.ufgTom != null) {
            this.ufgTom = uforeperiode.ufgTom!!.clone() as Date
        }
        this.fodselsArYngsteBarn = Integer.valueOf(uforeperiode.fodselsArYngsteBarn)
        this.spt = uforeperiode.spt
        this.opt = uforeperiode.opt
        this.ypt = uforeperiode.ypt
        this.spt_pa_f92 = Integer.valueOf(uforeperiode.spt_pa_f92)
        this.spt_pa_e91 = Integer.valueOf(uforeperiode.spt_pa_e91)
        this.opt_pa_f92 = Integer.valueOf(uforeperiode.opt_pa_f92)
        this.opt_pa_e91 = Integer.valueOf(uforeperiode.opt_pa_e91)
        this.ypt_pa_f92 = Integer.valueOf(uforeperiode.ypt_pa_f92)
        this.ypt_pa_e91 = Integer.valueOf(uforeperiode.ypt_pa_e91)
        this.paa = uforeperiode.paa
        this.fpp = uforeperiode.fpp
        this.fpp_omregnet = uforeperiode.fpp_omregnet
        this.spt_eos = uforeperiode.spt_eos
        this.spt_pa_e91_eos = Integer.valueOf(uforeperiode.spt_pa_e91_eos)
        this.spt_pa_f92_eos = Integer.valueOf(uforeperiode.spt_pa_f92_eos)
        this.redusertAntFppAr_proRata = Integer.valueOf(uforeperiode.redusertAntFppAr_proRata)
        if (uforeperiode.proRataBeregningType != null) {
            this.proRataBeregningType = ProRataBeregningTypeCti(uforeperiode.proRataBeregningType)
        }
        this.proRata_teller = Integer.valueOf(uforeperiode.proRata_teller)
        this.proRata_nevner = Integer.valueOf(uforeperiode.proRata_nevner)
        this.spt_proRata = uforeperiode.spt_proRata
        if (uforeperiode.angittUforetidspunkt != null) {
            this.angittUforetidspunkt = uforeperiode.angittUforetidspunkt!!.clone() as Date
        }
        this.beregningsgrunnlag = uforeperiode.beregningsgrunnlag
        this.antattInntektFaktorKap19 = uforeperiode.antattInntektFaktorKap19
        this.antattInntektFaktorKap20 = uforeperiode.antattInntektFaktorKap20
        this.ufgTom_intern = uforeperiode.ufgTom_intern
        this.sisteUFP_UP = uforeperiode.sisteUFP_UP
    }

    override fun compareTo(other: Uforeperiode): Int =
        DateCompareUtil.compareTo(virk, other.virk)

    //SIMDOM-ADD:
    fun isRealUforeperiode(): Boolean {
        val kode = uforeType?.kode ?: return false

        return (kode == UfoereType.UF_M_YRKE.name
                || kode == UfoereType.UFORE.name
                || kode == UfoereType.YRKE.name)
    }
}
