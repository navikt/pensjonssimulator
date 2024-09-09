package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.EosEkstra
import no.nav.pensjon.simulator.core.domain.regler.kode.FppGarantiKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektKode1Cti
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektKode2Cti
import java.io.Serializable

/**
 * Denne klassen inneholder spesielle data fra en beregning av uførepensjon.
 */
class UforeEkstra : Serializable {

    /**
     * Beskriver hvordan inntektstaket (tak) er beregnet. Se K_INNTEKT1_T
     * LONN_TILSK
     * YRKE_30_50
     * GRADERT_95_50
     * KOMB_VURD_11_12
     * INT_NIV_UF - default
     * UFOR_100
     * REAK_UF_50
     * UNT_VENT
     * UFGRAD_50
     * UFGRAD_50_REAK
     * YRKODE_18_HJEMMV
     */
    var inntektkode1: InntektKode1Cti? = InntektKode1Cti("INT_NIV_UF")

    /**
     * Beskriver om inntektstaket (tak) inneholder et fribeløp eller ikke.
     * INT_IKKE_BER - default
     * OPPJ_INNT_U_FRI_U_TK
     * OPPJ_INNT_M_FRI_U_TK
     * OPPJ_INNT_U_FRI_M_TK
     * OPPJ_INNT_M_FRI_M_TK
     * INT_U_FRI_GML_REGL
     * INT_M_FRI_GML_REGL
     */
    var inntektkode2: InntektKode2Cti? = InntektKode2Cti("INT_IKKE_BER")

    /**
     * Inntektstak ved uførepensjon.Angir den høyeste pensjonsgivende inntekt uførepensjonisten
     * kan ha uten at uføregraden skal revurderes.
     */
    var tak: Int = 0

    /**
     * Inntektsgrense før friinntektsdato ved uførepensjon.
     * Angir den høyeste pensjonsgivende inntekt uførepensjonisten kan ha før friinntektsdato uten at uføregraden skal revurderes.
     */
    var inntektsgrenseForFriinntektsdato: Int = 0

    /**
     * Fremtidig pensjonspoengtall.
     */
    var fpp: Double = 0.0

    /**
     * Framtidige pensjonspoengtall garanti, f.eks ung ufør har i dag en garanti på 3.3. i FPP.
     */
    var fppGaranti: Double = 0.0

    /**
     * Kode for fpp_garanti.<br></br>
     * `UNG_UF_VEN_R_T_33_PP = UNG UFØR SOM VENTER PÅ RETT TIL 3.3 PENSJONSPOENG`<br></br>
     * `UNG_UF_MR_33_PO = UNG UFØR MED RETT TIL 3.3 PENSJONSPOENG`<br></br>
     * `UNG_UF_VEN = ung ufør som venter, og som ble ufør 20 år gammel`<br></br>
     * `UNG_UF_MR_33_FR_92 = Ung ufør med rett til 3.3 poeng fra 0592`<br></br>
     * `UNG_UF_FOR_67 = unge uføre før 1967`
     */
    var fppGarantiKode: FppGarantiKodeCti? = null

    /**
     * Antall godskrevet framtidig poengtall, ikke full framtidig godskriving.
     */
    var redusertAntFppAr: Int = 0

    /**
     * Uforeperioden som skal benyttes i historikken hvis/når Uføregrunnlaget blir historisk.
     */
    var uforeperiode: BeregningUforeperiode? = null

    /**
     * Uforeperioden for ysk som skal benyttes i historikken hvis/når Uføregrunnlaget blir historisk.
     */
    var uforeperiodeYSK: BeregningUforeperiode? = null

    /**
     * Informasjon ang EØS beregning. Objektet sparer på data for bruk ved konvertering til AP.
     * På UforeEkstra benyttes feltet kun som transport til toppnoden.
     */
    @JsonIgnore
    var eosEkstra: EosEkstra? = null

    /**
     * Angir om bruker fyller vilkårende for å kunne få ung ufør garanti.
     * Brukes i BeregningsInformasjon.
     * Settes av "Ung Ufør Ruleset Template".OpprettUforeEkstra.
     */
    @JsonIgnore
    var vurdertUngUfor: Boolean = false

    constructor(uforeEkstra: UforeEkstra) {
        if (uforeEkstra.inntektkode1 != null) {
            inntektkode1 = InntektKode1Cti(uforeEkstra.inntektkode1)
        }
        if (uforeEkstra.inntektkode2 != null) {
            inntektkode2 = InntektKode2Cti(uforeEkstra.inntektkode2)
        }
        tak = uforeEkstra.tak
        inntektsgrenseForFriinntektsdato = uforeEkstra.inntektsgrenseForFriinntektsdato
        fpp = uforeEkstra.fpp
        fppGaranti = uforeEkstra.fppGaranti
        if (uforeEkstra.fppGarantiKode != null) {
            fppGarantiKode = FppGarantiKodeCti(uforeEkstra.fppGarantiKode)
        }
        redusertAntFppAr = uforeEkstra.redusertAntFppAr
        if (uforeEkstra.uforeperiode != null) {
            uforeperiode = BeregningUforeperiode(uforeEkstra.uforeperiode!!)
        }
        if (uforeEkstra.uforeperiodeYSK != null) {
            uforeperiodeYSK = BeregningUforeperiode(uforeEkstra.uforeperiodeYSK!!)
        }
        if (uforeEkstra.eosEkstra != null) {
            eosEkstra = EosEkstra(uforeEkstra.eosEkstra!!)
        }
        vurdertUngUfor = uforeEkstra.vurdertUngUfor
    }

    @JvmOverloads
    constructor(
            inntektkode1: InntektKode1Cti? = null,
            inntektkode2: InntektKode2Cti? = null,
            tak: Int = 0,
            inntektsgrenseForFriinntektsdato: Int = 0,
            fpp: Double = 0.0,
            fppGaranti: Double = 0.0,
            fppGarantiKode: FppGarantiKodeCti? = null,
            redusertAntFppAr: Int = 0,
            uforeperiode: BeregningUforeperiode? = null,
            uforeperiodeYSK: BeregningUforeperiode? = null,
            eosEkstra: EosEkstra? = null,
            vurdertUngUfor: Boolean = false
    ) : super() {
        this.inntektkode1 = inntektkode1
        this.inntektkode2 = inntektkode2
        this.tak = tak
        this.inntektsgrenseForFriinntektsdato = inntektsgrenseForFriinntektsdato
        this.fpp = fpp
        this.fppGaranti = fppGaranti
        this.fppGarantiKode = fppGarantiKode
        this.redusertAntFppAr = redusertAntFppAr
        this.uforeperiode = uforeperiode
        this.uforeperiodeYSK = uforeperiodeYSK
        this.eosEkstra = eosEkstra
        this.vurdertUngUfor = vurdertUngUfor
    }

    /**
     * Constructs a `String` with all attributes
     * in name = value format.
     *
     * @return a `String` representation
     * of this object.
     */
    override fun toString(): String {
        val TAB = "    "

        val retValue = StringBuilder()

        retValue.append("UforeEkstra ( ").append(super.toString()).append(TAB).append("inntektkode1 = ").append(inntektkode1).append(TAB).append("inntektkode2 = ")
                .append(inntektkode2).append(TAB).append("tak = ").append(tak).append(TAB).append(" )")

        return retValue.toString()
    }
}
