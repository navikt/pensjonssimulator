package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.FppGarantiKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.ProRataBeregningTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.UforetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode
import java.time.LocalDate
import java.util.*

// 2026-04-23
class BeregningUforeperiode {
    /**
     * Uføregraden, heltall 0-100.
     */
    @JvmField
    var ufg: Int? = null

    /**
     * Dato for uføretidspunktet.
     */
    @JvmField
    var uftLd: LocalDate? = null

    /**
     * Angir om Uføregraden er ren Uføre,inneholder delvis yrke eller bare yrke.
     */
    @JvmField
    var uforeTypeEnum: UforetypeEnum? = null

    /**
     * Framtidige pensjonspoengtall garanti, f.eks ung ufør har i dag en garanti på 3.3.
     */
    @JvmField
    var fppGaranti: Double? = null

    /**
     * Kode for fpp_garanti<br></br>
     * `A = UNG Ufør SOM VENTER på RETT TIL 3.3 PENSJONSPOENG`<br></br>
     * `B = UNG Ufør MED RETT TIL 3.3 PENSJONSPOENG`<br></br>
     * `C = ung ufør som venter, og som ble ufør 20 år gammel`<br></br>
     * `D = Ung ufør med rett til 3.3 poeng fra mai 1992`<br></br>
     * `E = unge Uføre før 1967`
     */
    @JvmField
    var fppGarantiKodeEnum: FppGarantiKodeEnum? = null

    /**
     * Antall godskrevet framtidig poengtall, ikke full framtidig godskriving.
     */
    @JvmField
    var redusertAntFppAr: Int? = null

    /**
     * Antall godskrevet framtidig poengtall, ikke full framtidig godskriving. EØS eller annen pro-rata beregning.
     */
    @JvmField
    var redusertAntFppAr_proRata: Int? = null

    /**
     * Angir hva utfallet av pro-rata beregningen var. Hvis satt er EØS eneste alternativ eller bedre enn alternativet (Folketrygd).
     */
    @JvmField
    var proRataBeregningTypeEnum: ProRataBeregningTypeEnum? = null

    /**
     * Dato for virkningsåret for denne Uføreperioden.
     */
    @JvmField
    var virkLd: LocalDate? = null

    /**
     * Dato for når Uføreperioden avsluttes.
     */
    @JvmField
    var uftTomLd: LocalDate? = null

    /**
     * Dato for når Uføregraden starter.
     */
    @JvmField
    var ufgFomLd: LocalDate? = null

    /**
     * Dato for når Uføregraden avsluttes.
     */
    @JvmField
    var ufgTomLd: LocalDate? = null

    /**
     * Fødselsår for yngste barn.
     */
    @JvmField
    var fodselsArYngsteBarn: Int? = null

    /**
     * Sluttpoengtall på tilleggspensjonen.
     */
    @JvmField
    var spt: Double? = null

    /**
     * Sluttpoengtall på tilleggspensjonen. Pro-rata beregning variant.
     */
    @JvmField
    var spt_proRata: Double? = null

    /**
     * Sluttpoengtall på tilleggspensjonen ved overkomp.
     */
    @JvmField
    var opt: Double? = null

    /**
     * Sluttpoengtall på tilleggspensjonen ved yrkesskade.
     */
    @JvmField
    var ypt: Double? = null

    /**
     * Antall poengår før 1992 på sluttpoengtallet.
     */
    @JvmField
    var spt_pa_f92: Int? = null

    /**
     * Antall poengår etter 1991 på sluttpoengtallet
     */
    @JvmField
    var spt_pa_e91: Int? = null

    /**
     * Antall poengår før 1992 på sluttpoengtallet.
     */
    @JvmField
    var proRata_teller: Int? = null

    /**
     * Antall poengår etter 1991 på sluttpoengtallet
     */
    @JvmField
    var proRata_nevner: Int? = null

    /**
     * Antall poengår før 1992 på sluttpoengtallet med overkomp
     */
    @JvmField
    var opt_pa_f92: Int? = null

    /**
     * Antall poengår etter 1992 på sluttpoengtallet med overkomp
     */
    @JvmField
    var opt_pa_e91: Int? = null

    /**
     * Antall poengår før 1992 på sluttpoengtallet ved yrkesskade
     */
    @JvmField
    var ypt_pa_f92: Int? = null

    /**
     * Antall poengår etter 1992 på sluttpoengtallet ved yrkesskade
     */
    @JvmField
    var ypt_pa_e91: Int? = null

    /**
     * Poengtall ut fra antatt årlig inntekt på skadetidspunktet
     */
    @JvmField
    var paa: Double? = null

    /**
     * Fremtidige pensjonspoeng
     */
    @JvmField
    var fpp: Double? = null

    /**
     * Fremtidige omregnete pensjonspoeng
     */
    @JvmField
    var fpp_omregnet: Double? = null

    /**
     * Sluttpoengtall i EØS
     */
    @JvmField
    var spt_eos: Double? = null

    /**
     * Antall poengår etter 1991 etter EØS-alternativet for sluttpoengtall
     */
    @JvmField
    var spt_pa_e91_eos: Int? = null

    /**
     * Antall poengår før 1992 etter EØS-alternativet for sluttpoengtall
     */
    @JvmField
    var spt_pa_f92_eos: Int? = null
    /**
     * @return the beregningsgrunnlag
     */
    /**
     * @param beregningsgrunnlag the beregningsgrunnlag to set
     */
    /*
            * Det beregningsgrunnlag (årsbeløp) som ble gjeldende i perioden.
            * Dette er beregningsgrunnlagOrdinårt når uforeType er UFORE eller UF_M_YRKE
            * og beregningsgrunnlagYrkesskade når type er YRKE
            */
    @JvmField
    var beregningsgrunnlag = 0
    /**
     * @return the angittUforetidspunkt
     */
    /**
     * @param angittUforetidspunkt the angittUforetidspunkt to set
     * Det uføretidspunkt som er angitt for perioden, men ikke nødvendigvis anvendt.
     * **/
    @JvmField
    var angittUforetidspunktLd: LocalDate? = null

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 19).
     */
    @JvmField
    var antattInntektFaktorKap19 = 0.0

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 20).
     */
    @JvmField
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
        ufgFomLd = source.ufgFomLd
        ufgTomLd = source.ufgTomLd
        uforeTypeEnum = source.uforeTypeEnum
        uftLd = source.uftLd
        uftTomLd = source.uftTomLd
        virkLd = source.virkLd
        ypt = source.ypt
        ypt_pa_e91 = source.ypt_pa_e91
        ypt_pa_f92 = source.ypt_pa_f92
        beregningsgrunnlag = source.beregningsgrunnlag
        angittUforetidspunktLd = source.angittUforetidspunktLd
        antattInntektFaktorKap19 = source.antattInntektFaktorKap19
        antattInntektFaktorKap20 = source.antattInntektFaktorKap20
    }

    constructor(source: Uforeperiode) : super() {
        ufg = source.ufg
        uftLd = source.uftLd
        uforeTypeEnum = source.uforeTypeEnum
        fppGaranti = source.fppGaranti
        fppGarantiKodeEnum = source.fppGarantiKodeEnum
        redusertAntFppAr = source.redusertAntFppAr
        redusertAntFppAr_proRata = source.redusertAntFppAr_proRata
        proRataBeregningTypeEnum = source.proRataBeregningTypeEnum
        virkLd = source.virkLd
        uftTomLd = source.uftTomLd
        ufgFomLd = source.ufgFomLd
        ufgTomLd = source.ufgTomLd
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
        angittUforetidspunktLd = source.angittUforetidspunktLd
        antattInntektFaktorKap19 = source.antattInntektFaktorKap19
        antattInntektFaktorKap20 = source.antattInntektFaktorKap20
    }
}
