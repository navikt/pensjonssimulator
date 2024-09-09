package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode
import no.nav.pensjon.simulator.core.domain.regler.kode.FppGarantiKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ProRataBeregningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.UforeTypeCti
import java.io.Serializable
import java.util.*

class BeregningUforeperiode : Serializable {

    /**
     * Uføregraden, heltall 0-100.
     */
    var ufg: Int = 0

    /**
     * Dato for uføretidspunktet.
     */
    var uft: Date? = null

    /**
     * Angir om uføregraden er ren uføre,inneholder delvis yrke eller bare yrke.
     */
    var uforeType: UforeTypeCti? = null

    /**
     * Framtidige pensjonspoengtall garanti, f.eks ung ufør har i dag en garanti på 3.3.
     */
    var fppGaranti: Double? = null

    /**
     * Kode for fpp_garanti<br></br>
     * `A = UNG UFØR SOM VENTER PÅ RETT TIL 3.3 PENSJONSPOENG`<br></br>
     * `B = UNG UFØR MED RETT TIL 3.3 PENSJONSPOENG`<br></br>
     * `C = ung ufør som venter, og som ble ufør 20 år gammel`<br></br>
     * `D = Ung ufør med rett til 3.3 poeng fra mai 1992`<br></br>
     * `E = unge uføre før 1967`
     */
    var fppGarantiKode: FppGarantiKodeCti? = null

    /**
     * Antall godskrevet framtidig poengtall, ikke full framtidig godskriving.
     */
    var redusertAntFppAr: Int = 0

    /**
     * Antall godskrevet framtidig poengtall, ikke full framtidig godskriving. EØS eller annen pro-rata beregning.
     */
    var redusertAntFppAr_proRata: Int = 0

    /**
     * Angir hva utfallet av pro-rata beregningen var. Hvis satt er EØS eneste alternativ eller bedre enn alternativet (Folketrygd).
     */
    var proRataBeregningType: ProRataBeregningTypeCti? = null

    /**
     * Dato for virkningsåret for denne uføreperioden.
     */
    var virk: Date? = null

    /**
     * Dato for når uføreperioden avsluttes.
     */
    var uftTom: Date? = null

    /**
     * Dato for når uføregraden starter.
     */
    var ufgFom: Date? = null

    /**
     * Dato for når uføregraden avsluttes.
     */
    var ufgTom: Date? = null

    /**
     * Fødselsår for yngste barn.
     */
    var fodselsArYngsteBarn: Int? = null

    /**
     * Sluttpoengtall på tilleggspensjonen.
     */
    var spt: Double? = null

    /**
     * Sluttpoengtall på tilleggspensjonen. Pro-rata beregning variant.
     */
    var spt_proRata: Double? = null

    /**
     * Sluttpoengtall på tilleggspensjonen ved overkomp.
     */
    var opt: Double? = null

    /**
     * Sluttpoengtall på tilleggspensjonen ved yrkesskade.
     */
    var ypt: Double? = null

    /**
     * Antall poengår før 1992 på sluttpoengtallet.
     */
    var spt_pa_f92: Int? = null

    /**
     * Antall poengår etter 1991 på sluttpoengtallet
     */
    var spt_pa_e91: Int? = null

    /**
     * Antall poengår før 1992 på sluttpoengtallet.
     */
    var proRata_teller: Int? = null

    /**
     * Antall poengår etter 1991 på sluttpoengtallet
     */
    var proRata_nevner: Int? = null

    /**
     * Antall poengår før 1992 på sluttpoengtallet med overkomp
     */
    var opt_pa_f92: Int? = null

    /**
     * Antall poengår etter 1992 på sluttpoengtallet med overkomp
     */
    var opt_pa_e91: Int? = null

    /**
     * Antall poengår før 1992 på sluttpoengtallet ved yrkesskade
     */
    var ypt_pa_f92: Int? = null

    /**
     * Antall poengår etter 1992 på sluttpoengtallet ved yrkesskade
     */
    var ypt_pa_e91: Int? = null

    /**
     * Poengtall ut fra antatt årlig inntekt på skadetidspunktet
     */
    var paa: Double? = null

    /**
     * Fremtidige pensjonspoeng
     */
    var fpp: Double? = null

    /**
     * Fremtidige omregnete pensjonspoeng
     */
    var fpp_omregnet: Double? = null

    /**
     * Sluttpoengtall i EØS
     */
    var spt_eos: Double = 0.0

    /**
     * Antall poengår etter 1991 etter EØS-alternativet for sluttpoengtall
     */
    var spt_pa_e91_eos: Int = 0

    /**
     * Antall poengår før 1992 etter EØS-alternativet for sluttpoengtall
     */
    var spt_pa_f92_eos: Int = 0

    /**
     * Det beregningsgrunnlag (årsbeløp) som ble gjeldende i perioden.
     * Dette er beregningsgrunnlagOrdinært når uforeType er UFORE eller UF_M_YRKE
     * og beregningsgrunnlagYrkesskade når type er YRKE
     */
    var beregningsgrunnlag: Int = 0

    /**
     * Det uføretidspunkt som er angitt for perioden, men ikke nødvendigvis anvendt.
     */
    var angittUforetidspunkt: Date? = null

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 19).
     */

    var antattInntektFaktorKap19: Double = 0.0

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 20).
     */

    var antattInntektFaktorKap20: Double = 0.0

    constructor(b: BeregningUforeperiode) : this() {
        fodselsArYngsteBarn = b.fodselsArYngsteBarn
        fpp = b.fpp
        fpp_omregnet = b.fpp_omregnet
        fppGaranti = b.fppGaranti
        if (b.fppGarantiKode != null) {
            fppGarantiKode = FppGarantiKodeCti(b.fppGarantiKode)
        }
        opt = b.opt
        opt_pa_e91 = b.opt_pa_e91
        opt_pa_f92 = b.opt_pa_f92
        paa = b.paa
        proRata_nevner = b.proRata_nevner
        proRata_teller = b.proRata_teller
        if (b.proRataBeregningType != null) {
            proRataBeregningType = ProRataBeregningTypeCti(b.proRataBeregningType)
        }
        redusertAntFppAr = b.redusertAntFppAr
        redusertAntFppAr_proRata = b.redusertAntFppAr_proRata
        spt = b.spt
        spt_pa_e91 = b.spt_pa_e91
        spt_pa_e91_eos = b.spt_pa_e91_eos
        spt_pa_f92 = b.spt_pa_f92
        spt_pa_f92_eos = b.spt_pa_f92_eos
        spt_proRata = b.spt_proRata
        ufg = b.ufg
        ufgFom = b.ufgFom?.clone() as Date?
        ufgTom = b.ufgTom?.clone() as Date?
        if (b.uforeType != null) {
            uforeType = UforeTypeCti(b.uforeType)
        }
        uft = b.uft?.clone() as Date?
        uftTom = b.uftTom?.clone() as Date?
        virk = b.virk?.clone() as Date?
        ypt = b.ypt
        ypt_pa_e91 = b.ypt_pa_e91
        ypt_pa_f92 = b.ypt_pa_f92
        beregningsgrunnlag = b.beregningsgrunnlag
        angittUforetidspunkt = b.angittUforetidspunkt?.clone() as Date?
        antattInntektFaktorKap19 = b.antattInntektFaktorKap19
        antattInntektFaktorKap20 = b.antattInntektFaktorKap20
    }

    @JvmOverloads
    constructor(ufg: Int = 0,
                uft: Date? = null,
                uforeType: UforeTypeCti? = null,
                fppGaranti: Double? = null,
                fppGarantiKode: FppGarantiKodeCti? = null,
                redusertAntFppAr: Int = 0,
                redusertAntFppAr_proRata: Int = 0,
                proRataBeregningType: ProRataBeregningTypeCti? = null,
                virk: Date? = null,
                uftTom: Date? = null,
                ufgFom: Date? = null,
                ufgTom: Date? = null,
                fodselsArYngsteBarn: Int? = null,
                spt: Double? = null,
                spt_proRata: Double? = null,
                opt: Double? = null,
                ypt: Double? = null,
                spt_pa_f92: Int? = null,
                spt_pa_e91: Int? = null,
                proRata_teller: Int? = null,
                proRata_nevner: Int? = null,
                opt_pa_f92: Int? = null,
                opt_pa_e91: Int? = null,
                ypt_pa_f92: Int? = null,
                ypt_pa_e91: Int? = null,
                paa: Double? = null,
                fpp: Double? = null,
                fpp_omregnet: Double? = null,
                spt_eos: Double = 0.0,
                spt_pa_e91_eos: Int = 0,
                spt_pa_f92_eos: Int = 0,
                beregningsgrunnlag: Int = 0,
                angittUforetidspunkt: Date? = null,
                antattInntektFaktorKap19: Double = 0.0,
                antattInntektFaktorKap20: Double = 0.0) : super() {
        this.ufg = ufg
        this.uft = uft
        this.uforeType = uforeType
        this.fppGaranti = fppGaranti
        this.fppGarantiKode = fppGarantiKode
        this.redusertAntFppAr = redusertAntFppAr
        this.redusertAntFppAr_proRata = redusertAntFppAr_proRata
        this.proRataBeregningType = proRataBeregningType
        this.virk = virk
        this.uftTom = uftTom
        this.ufgFom = ufgFom
        this.ufgTom = ufgTom
        this.fodselsArYngsteBarn = fodselsArYngsteBarn
        this.spt = spt
        this.spt_proRata = spt_proRata
        this.opt = opt
        this.ypt = ypt
        this.spt_pa_f92 = spt_pa_f92
        this.spt_pa_e91 = spt_pa_e91
        this.proRata_teller = proRata_teller
        this.proRata_nevner = proRata_nevner
        this.opt_pa_f92 = opt_pa_f92
        this.opt_pa_e91 = opt_pa_e91
        this.ypt_pa_f92 = ypt_pa_f92
        this.ypt_pa_e91 = ypt_pa_e91
        this.paa = paa
        this.fpp = fpp
        this.fpp_omregnet = fpp_omregnet
        this.spt_eos = spt_eos
        this.spt_pa_e91_eos = spt_pa_e91_eos
        this.spt_pa_f92_eos = spt_pa_f92_eos
        this.beregningsgrunnlag = beregningsgrunnlag
        this.angittUforetidspunkt = angittUforetidspunkt
        this.antattInntektFaktorKap19 = antattInntektFaktorKap19
        this.antattInntektFaktorKap20 = antattInntektFaktorKap20

    }

    constructor(up: Uforeperiode) : super() {
        ufg = up.ufg
        uft = up.uft
        uforeType = up.uforeType
        fppGaranti = up.fppGaranti
        fppGarantiKode = up.fppGarantiKode
        redusertAntFppAr = up.redusertAntFppAr
        redusertAntFppAr_proRata = up.redusertAntFppAr_proRata
        proRataBeregningType = up.proRataBeregningType
        virk = up.virk
        uftTom = up.uftTom
        ufgFom = up.ufgFom
        ufgTom = up.ufgTom
        fodselsArYngsteBarn = up.fodselsArYngsteBarn
        spt = up.spt
        spt_proRata = up.spt_proRata
        opt = up.opt
        ypt = up.ypt
        spt_pa_f92 = up.spt_pa_f92
        spt_pa_e91 = up.spt_pa_e91
        proRata_teller = up.proRata_teller
        proRata_nevner = up.proRata_nevner
        opt_pa_f92 = up.opt_pa_f92
        opt_pa_e91 = up.opt_pa_e91
        ypt_pa_f92 = up.ypt_pa_f92
        ypt_pa_e91 = up.ypt_pa_e91
        paa = up.paa
        fpp = up.fpp
        fpp_omregnet = up.fpp_omregnet
        spt_eos = up.spt_eos
        spt_pa_e91_eos = up.spt_pa_e91_eos
        spt_pa_f92_eos = up.spt_pa_f92_eos
        beregningsgrunnlag = up.beregningsgrunnlag
        angittUforetidspunkt = up.angittUforetidspunkt
        antattInntektFaktorKap19 = up.antattInntektFaktorKap19
        antattInntektFaktorKap20 = up.antattInntektFaktorKap20
    }

}
