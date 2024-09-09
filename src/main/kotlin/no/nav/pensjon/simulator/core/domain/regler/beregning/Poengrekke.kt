package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import java.io.Serializable
import java.util.*
import java.util.Comparator.comparing

class Poengrekke : Serializable {

    /**
     * Antall poengår totalt. Kan være ikke være over 40.
     */
    var pa: Int = 0

    /**
     * Antall poengår før 1992.
     */
    var pa_f92: Int = 0

    /**
     * Antall poengår etter 1991.
     */
    var pa_e91: Int = 0

    /**
     * Faktiske poengår i Norge.
     */
    var pa_fa_norge: Int = 0

    /**
     * Tidligere pensjonsgivende inntekt.
     */
    var tpi: Int = 0

    /**
     * Liste av Poengtall
     */
    var poengtallListe: MutableList<Poengtall> = mutableListOf()

    /**
     * Samlet antall poengår i Norge.
     */
    var pa_no: Int = 0

    /**
     * Framtidig pensjonspoengtall. Brukes ved beregning av
     * uførepensjon,gjenlevendepensjon og AFP.
     */
    var fpp: FramtidigPensjonspoengtall? = null

    /**
     * Framtidig pensjonspoengtall, basert på omregnet poengrekke. Brukes ved
     * beregning av uførepensjon og gjenlevendepensjon.
     */
    var fpp_omregnet: FramtidigPensjonspoengtall? = null

    /**
     * Siste poengår med framtidig pensjonspoeng.
     */
    var siste_fpp_aar: Int = 0

    /**
     * Tidligere pensjonsgivende inntekt, beregnet som en faktor.
     */
    var tpi_faktor: Double = 0.0

    /**
     * Brutto antall framtidige poengår i norden.
     */
    var pa_nordisk_framt_brutto: Int = 0

    /**
     * Netto antall framtidige poengår i norden.
     */
    var pa_nordisk_framt_netto: Int = 0

    /**
     * Poengtall ut fra antatt årlig inntekt.
     */
    var paa: Double = 0.0

    /**
     * Faktiske poengår i Norden.
     */
    var pa_fa_norden: Int = 0

    /**
     * Teoretiske poengår EØS. Dette begrepet brukes ved beregning av
     * tilleggspensjon etter EØS-reglene eller ved beregning etter de bilaterale
     * avtalene. Ved pro rata beregningen fastsettes det teoretiske antallet
     * poengår som det antall år vedkommende ville fått dersom all opptjening i
     * EØS-land hadde vært opptjent i Norge
     */
    var pa_eos_teoretisk: Int = 0

    /**
     * Pro-rata poengår EØS. Poengår som ikke skal inngå i pro-rata beregning er
     * utelatt.
     */
    var pa_eos_pro_rata: Int = 0

    /**
     * Teller i pro-rata brøk.
     */
    var pa_pro_rata_teller: Int = 0

    /**
     * Nevner i pro-rata brøk.
     */
    var pa_pro_rata_nevner: Int = 0

    /**
     * Antall fremtidige poengår.
     */
    var fpa: Int = 0

    /**
     * Liste av merknader.
     */
    var merknadListe: MutableList<Merknad> = mutableListOf()

    /**
     * Angir om omregnet FPP skal benyttes i godskrivning av framtidige år.
     * Intern PREG variabel.
     */
    @JsonIgnore
    var bevarFPPgrunnlag: Boolean = false

    /**
     * Angir hvilket år poengrekken regnes fra. Intern PREG variabel.
     */
    @JsonIgnore
    var startar: Int = 0

    /**
     * Angir hvilket år ordinære poeng regnes til. Intern PREG variabel.
     */
    @JsonIgnore
    var tilar: Int = 0

    /**
     * Angir hvilken dato ordinære poeng regnes til. Intern PREG variabel.
     */
    @JsonIgnore
    var tildato: Date? = null

    /**
     * Angir hvilket år fremtidige poeng regnes til. Intern PREG variabel.
     */
    // SIMDOM-EDIT var sluttar: Int = 0

    /**
     * Angir om FPP skal beregnes. Intern PREG variabel.
     */
    @JsonIgnore
    var vilkar3_17: Boolean = false

    /**
     * Angir om FPP ikke skal omregnes til under 5. Intern PREG variabel.
     */
    @JsonIgnore
    var FPPomregnetGaranti: Boolean = false

    /**
     * Antall poengår etter 1991 og etter det 66. året. Intern PREG variabel.
     */
    @JsonIgnore
    var pa_67_70_e91: Int = 0

    /**
     * Antall reelle poengår totalt. Kan være over 40.
     */
    @JsonIgnore
    var pa_fa: Int = 0

    /**
     * Angir om avdøde er død før avgang AP.
     */
    @JsonIgnore
    var dodForAP: Boolean? = null

    /**
     * Flagg som viser om oppustert grunnlag fra pensjonsberegning fra TP-ordning er benyttet
     */
    var AfpTpoUpGrunnlagAnvendt: Boolean = false

    /**
     * Det oppjusterte uførepensjonsgrunnlaget fra TP-ordningen som ble brukt i beregning av TPI
     */
    var AfpTpoUpGrunnlagOppjustert: Int = 0

    /**
     * TPI beregnet etter hovedregelen
     */
    var tpiEtterHovedregel: Int = 0

    constructor(poengrekke: Poengrekke) {
        pa = poengrekke.pa
        pa_f92 = poengrekke.pa_f92
        pa_e91 = poengrekke.pa_e91
        pa_fa_norge = poengrekke.pa_fa_norge
        tpi = poengrekke.tpi
        poengtallListe.clear()
        for (poengtall in poengrekke.poengtallListe) {
            poengtallListe.add(Poengtall(poengtall))
        }
        pa_no = poengrekke.pa_no
        if (poengrekke.fpp != null) {
            fpp = FramtidigPensjonspoengtall(poengrekke.fpp!!)
        }
        if (poengrekke.fpp_omregnet != null) {
            fpp_omregnet = FramtidigPensjonspoengtall(poengrekke.fpp_omregnet!!)
        }
        siste_fpp_aar = poengrekke.siste_fpp_aar
        tpi_faktor = poengrekke.tpi_faktor
        pa_nordisk_framt_brutto = poengrekke.pa_nordisk_framt_brutto
        pa_nordisk_framt_netto = poengrekke.pa_nordisk_framt_netto
        paa = poengrekke.paa
        pa_fa_norden = poengrekke.pa_fa_norden
        pa_eos_teoretisk = poengrekke.pa_eos_teoretisk
        pa_eos_pro_rata = poengrekke.pa_eos_pro_rata
        pa_pro_rata_teller = poengrekke.pa_pro_rata_teller
        pa_pro_rata_nevner = poengrekke.pa_pro_rata_nevner
        fpa = poengrekke.fpa
        AfpTpoUpGrunnlagAnvendt = poengrekke.AfpTpoUpGrunnlagAnvendt
        AfpTpoUpGrunnlagOppjustert = poengrekke.AfpTpoUpGrunnlagOppjustert
        tpiEtterHovedregel = poengrekke.tpiEtterHovedregel
        merknadListe.clear()
        for (merknad in poengrekke.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }
        bevarFPPgrunnlag = poengrekke.bevarFPPgrunnlag
        startar = poengrekke.startar
        tilar = poengrekke.tilar
        if (poengrekke.tildato != null) {
            tildato = poengrekke.tildato!!.clone() as Date
        }
        vilkar3_17 = poengrekke.vilkar3_17
        FPPomregnetGaranti = poengrekke.FPPomregnetGaranti
        pa_67_70_e91 = poengrekke.pa_67_70_e91
        dodForAP = poengrekke.dodForAP
        pa_fa = poengrekke.pa_fa
    }

    constructor() : super()

    constructor(
        pa: Int = 0,
        pa_f92: Int = 0,
        pa_e91: Int = 0,
        pa_fa_norge: Int = 0,
        tpi: Int = 0,
        poengtallListe: MutableList<Poengtall> = mutableListOf(),
        pa_no: Int = 0,
        fpp: FramtidigPensjonspoengtall? = null,
        fpp_omregnet: FramtidigPensjonspoengtall? = null,
        siste_fpp_aar: Int = 0,
        tpi_faktor: Double = 0.0,
        pa_nordisk_framt_brutto: Int = 0,
        pa_nordisk_framt_netto: Int = 0,
        paa: Double = 0.0,
        pa_fa_norden: Int = 0,
        pa_eos_teoretisk: Int = 0,
        pa_eos_pro_rata: Int = 0,
        pa_pro_rata_teller: Int = 0,
        pa_pro_rata_nevner: Int = 0,
        fpa: Int = 0,
        merknadListe: MutableList<Merknad> = mutableListOf(),
        bevarFPPgrunnlag: Boolean = false,
        startar: Int = 0,
        tilar: Int = 0,
        tildato: Date? = null,
        sluttar: Int = 0,
        vilkar3_17: Boolean = false,
        fppOomregnetGaranti: Boolean = false,
        pa_67_70_e91: Int = 0,
        pa_fa: Int = 0,
        dodForAP: Boolean? = null,
        afpTpoUpGrunnlagAnvendt: Boolean = false,
        afpTpoUpGrunnlagOppjustert: Int = 0,
        tpiEtterHovedregel: Int = 0
    ) {
        this.pa = pa
        this.pa_f92 = pa_f92
        this.pa_e91 = pa_e91
        this.pa_fa_norge = pa_fa_norge
        this.tpi = tpi
        this.poengtallListe = poengtallListe
        this.pa_no = pa_no
        this.fpp = fpp
        this.fpp_omregnet = fpp_omregnet
        this.siste_fpp_aar = siste_fpp_aar
        this.tpi_faktor = tpi_faktor
        this.pa_nordisk_framt_brutto = pa_nordisk_framt_brutto
        this.pa_nordisk_framt_netto = pa_nordisk_framt_netto
        this.paa = paa
        this.pa_fa_norden = pa_fa_norden
        this.pa_eos_teoretisk = pa_eos_teoretisk
        this.pa_eos_pro_rata = pa_eos_pro_rata
        this.pa_pro_rata_teller = pa_pro_rata_teller
        this.pa_pro_rata_nevner = pa_pro_rata_nevner
        this.fpa = fpa
        this.merknadListe = merknadListe
        this.bevarFPPgrunnlag = bevarFPPgrunnlag
        this.startar = startar
        this.tilar = tilar
        this.tildato = tildato
        this.vilkar3_17 = vilkar3_17
        this.FPPomregnetGaranti = fppOomregnetGaranti
        this.pa_67_70_e91 = pa_67_70_e91
        this.pa_fa = pa_fa
        this.dodForAP = dodForAP
        this.AfpTpoUpGrunnlagAnvendt = afpTpoUpGrunnlagAnvendt
        this.AfpTpoUpGrunnlagOppjustert = afpTpoUpGrunnlagOppjustert
        this.tpiEtterHovedregel = tpiEtterHovedregel
    }

    fun sortertPoengtallListe(): MutableList<Poengtall> {
        val sortedPt = ArrayList(poengtallListe)
        Collections.sort(sortedPt, Collections.reverseOrder())
        return sortedPt
    }

    fun sortertPoengtallListeByBRPFA(): MutableList<Poengtall> {
        val poengtallList = ArrayList(poengtallListe)
        poengtallList.sortWith(comparing(Poengtall::brp_fa, reverseOrder()))
        return poengtallList
    }
}
