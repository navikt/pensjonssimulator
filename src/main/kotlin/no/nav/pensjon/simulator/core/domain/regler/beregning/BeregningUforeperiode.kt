package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.FppGarantiKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.ProRataBeregningTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.UforetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode
import java.util.*

// Checked 2025-02-28
class BeregningUforeperiode {
    /**
     * Uføregraden, heltall 0-100.
     */
    var ufg: Int? = null

    /**
     * Dato for uføretidspunktet.
     */
    var uft: Date? = null

    /**
     * Angir om uføregraden er ren uføre,inneholder delvis yrke eller bare yrke.
     */
    var uforeTypeEnum: UforetypeEnum? = null

    /**
     * Framtidige pensjonspoengtall garanti, f.eks ung ufør har i dag en garanti på 3.3.
     */
    var fppGaranti: Double? = null

    /**
     * Kode for fpp_garanti<br></br>
     * `A = UNG Ufør SOM VENTER på RETT TIL 3.3 PENSJONSPOENG`<br></br>
     * `B = UNG Ufør MED RETT TIL 3.3 PENSJONSPOENG`<br></br>
     * `C = ung ufør som venter, og som ble ufør 20 år gammel`<br></br>
     * `D = Ung ufør med rett til 3.3 poeng fra mai 1992`<br></br>
     * `E = unge uføre før 1967`
     */
    var fppGarantiKodeEnum: FppGarantiKodeEnum? = null

    /**
     * Antall godskrevet framtidig poengtall, ikke full framtidig godskriving.
     */
    var redusertAntFppAr: Int? = null

    /**
     * Antall godskrevet framtidig poengtall, ikke full framtidig godskriving. EØS eller annen pro-rata beregning.
     */
    var redusertAntFppAr_proRata: Int? = null

    /**
     * Angir hva utfallet av pro-rata beregningen var. Hvis satt er EØS eneste alternativ eller bedre enn alternativet (Folketrygd).
     */
    var proRataBeregningTypeEnum: ProRataBeregningTypeEnum? = null

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
    var spt_eos: Double? = null

    /**
     * Antall poengår etter 1991 etter EØS-alternativet for sluttpoengtall
     */
    var spt_pa_e91_eos: Int? = null

    /**
     * Antall poengår før 1992 etter EØS-alternativet for sluttpoengtall
     */
    var spt_pa_f92_eos: Int? = null

    /**
     * Det beregningsgrunnlag (årsbeløp) som ble gjeldende i perioden.
     * Dette er beregningsgrunnlagOrdinårt når uforeType er UFORE eller UF_M_YRKE
     * og beregningsgrunnlagYrkesskade når type er YRKE
     */
    var beregningsgrunnlag = 0

    /**
     * Det uføretidspunkt som er angitt for perioden, men ikke nødvendigvis anvendt.
     */
    var angittUforetidspunkt: Date? = null

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 19).
     */
    var antattInntektFaktorKap19 = 0.0

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 20).
     */
    var antattInntektFaktorKap20 = 0.0

    constructor()

    constructor(source: BeregningUforeperiode) : this() {
        fodselsArYngsteBarn = source.fodselsArYngsteBarn
        fpp = source.fpp
        fpp_omregnet = source.fpp_omregnet
        fppGaranti = source.fppGaranti
        fppGarantiKodeEnum = source.fppGarantiKodeEnum
        opt = source.opt
        opt_pa_e91 = source.opt_pa_e91
        opt_pa_f92 = source.opt_pa_f92
        paa = source.paa
        proRata_nevner = source.proRata_nevner
        proRata_teller = source.proRata_teller
        proRataBeregningTypeEnum = source.proRataBeregningTypeEnum
        redusertAntFppAr = source.redusertAntFppAr
        redusertAntFppAr_proRata = source.redusertAntFppAr_proRata
        spt = source.spt
        spt_pa_e91 = source.spt_pa_e91
        spt_pa_e91_eos = source.spt_pa_e91_eos
        spt_pa_f92 = source.spt_pa_f92
        spt_pa_f92_eos = source.spt_pa_f92_eos
        spt_proRata = source.spt_proRata
        ufg = source.ufg
        ufgFom = source.ufgFom?.clone() as? Date
        ufgTom = source.ufgTom?.clone() as? Date
        uforeTypeEnum = source.uforeTypeEnum
        uft = source.uft?.clone() as? Date
        uftTom = source.uftTom?.clone() as? Date
        virk = source.virk?.clone() as? Date
        ypt = source.ypt
        ypt_pa_e91 = source.ypt_pa_e91
        ypt_pa_f92 = source.ypt_pa_f92
        beregningsgrunnlag = source.beregningsgrunnlag
        angittUforetidspunkt = source.angittUforetidspunkt?.clone() as? Date
        antattInntektFaktorKap19 = source.antattInntektFaktorKap19
        antattInntektFaktorKap20 = source.antattInntektFaktorKap20
    }

    constructor(source: Uforeperiode) : super() {
        ufg = source.ufg
        uft = source.uft?.clone() as? Date
        uforeTypeEnum = source.uforeTypeEnum
        fppGaranti = source.fppGaranti
        fppGarantiKodeEnum = source.fppGarantiKodeEnum
        redusertAntFppAr = source.redusertAntFppAr
        redusertAntFppAr_proRata = source.redusertAntFppAr_proRata
        proRataBeregningTypeEnum = source.proRataBeregningTypeEnum
        virk = source.virk?.clone() as? Date
        uftTom = source.uftTom?.clone() as? Date
        ufgFom = source.ufgFom?.clone() as? Date
        ufgTom = source.ufgTom?.clone() as? Date
        fodselsArYngsteBarn = source.fodselsArYngsteBarn
        spt = source.spt
        spt_proRata = source.spt_proRata
        opt = source.opt
        ypt = source.ypt
        spt_pa_f92 = source.spt_pa_f92
        spt_pa_e91 = source.spt_pa_e91
        proRata_teller = source.proRata_teller
        proRata_nevner = source.proRata_nevner
        opt_pa_f92 = source.opt_pa_f92
        opt_pa_e91 = source.opt_pa_e91
        ypt_pa_f92 = source.ypt_pa_f92
        ypt_pa_e91 = source.ypt_pa_e91
        paa = source.paa
        fpp = source.fpp
        fpp_omregnet = source.fpp_omregnet
        spt_eos = source.spt_eos
        spt_pa_e91_eos = source.spt_pa_e91_eos
        spt_pa_f92_eos = source.spt_pa_f92_eos
        beregningsgrunnlag = source.beregningsgrunnlag
        angittUforetidspunkt = source.angittUforetidspunkt?.clone() as? Date
        antattInntektFaktorKap19 = source.antattInntektFaktorKap19
        antattInntektFaktorKap20 = source.antattInntektFaktorKap20
    }
}
