package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.PoengtalltypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// PSELV: no.nav.pensjon.pen.domain.api.beregning.Ytelseskomponent
@JsonInclude(NON_NULL)
data class NavYtelseKomponentV2 (
    val ytelseskomponentType: YtelseskomponentTypeEnum? = null,
    val merknader: List<MerknadV2> = emptyList(),
    val bruttoPerAr: Double? = 0.0,
    val netto: Int? = 0,
    val erBrukt: Boolean = false,
    val opphort: Boolean? = false,
    val formelKode: FormelKodeEnum? = null,
    // Tilleggspensjon:
    val spt: SluttpoengtallV2? = null,
    val ypt: SluttpoengtallV2? = null
    // (end tilleggspensjon)
    // Not used in PSELV:
    // ytelseskomponentId
    // brutto
    // nettoPerAr
    // fradrag
    // fradragPerAr
    // samletInntektAvkort
    // resultatKilde
    // sakType
    // fradragsTransaksjon
    // tt_anv
    // antallBarn
    // btDiff_eos
    // avkortingsArsak
    // mpnSatsFT
    // proratanevner
    // proratateller
    // reguleringsInformasjon
    // formelMap
    // changeStamp
    // versjon
    // ----- Motregning: -----
    // dagsats
    // antallDager
    // ----- Tilleggspensjon: -----
    // opt
    // skiltesDelAvAvdodesTP
    // ----- Pensjonstillegg: -----
    // minstepensjonsnivaSatsType
    // minstepensjonsnivaSats
    // forholdstall67
    // justertMinstePensjonsniva
    // ----- Kun uføretrygd: -----
    // nettoAkk
    // nettoRestAr
    // avkortningsbelopPerAr
    // brukersUforetrygdForJustering
    // reduksjonsinformasjon
    // avkortingsinformasjon
    // periodisertAvvikEtteroppgjor
    // tidligereBelopAr
    // brukersInntektTilAvkortning
    // inntektAnnenForelder
    // belopFratrukketAnnenForeldersInntekt
    // annenForelderUforetrygdForJustering
    // brukersGjenlevendetilleggForJustering
    // ----- Kun UT_ORDINER: -----
    // egenopptjentUforetrygdBest
    // pensjonsgrad
    // fradragPerArUtenArbeidsforsok
    // ----- Kun ektefelletillegg uføretrygd: -----
    // fribelop
    // skattefritak
    // etForSkattekomp
    // upForSkattekomp
    // ----- Kun ektefelletillegg og barnetillegg: -----
    // forsorgingstilleggNiva
    // ----- Kun Gjenlevendetillegg UT: -----
    // bgKonvertert
    // bgAvdod
    // bgGjenlevendetillegg
    // nyttGjenlevendetillegg
    // gjenlevendetilleggInformasjon
    // avkortingsfaktorGJT
    // eksportFaktor
    // ----- MinstenivatilleggPensjonistpar: -----
    // minstepensjonsnivaOrdiner
    // ektefellesPensjon
    // ektefellesUttaksgrad
    // ektefellesTt_anv
    // ordinerSatsST
    // brukersBeregningsinformasjonMinstenivatilleggPensjonistpar
    // epsBeregningsinformasjonMinstenivatilleggPensjonistpar
    // ----- MinstenivatilleggIndividuelt: -----
    // minstePensjonsniva
    // garantipensjonsniva
    // samletPensjonForMNT
    // ----- GjenlevendetilleggAP: -----
    // apKap19MedGJR
    // apKap19UtenGJR
    // ----- KrigOgGammelYrkesskade: -----
    // grunnlagForUtbetaling
    // kapitalUtlosning
    // ps
    // yg
    // mendelFordeling
    // belopTilUtbet
    // ----- Grunnpensjon: -----
    // anvendtTrygdetid
    // ----- GjenlevendetilleggAP og AfpKompensasjonstillegg: -----
    // referansebelop
)

// PSELV: Sluttpoengtall
@JsonInclude(NON_NULL)
data class SluttpoengtallV2 (
    val pt: Double = 0.0,
    val poengrekke: PoengrekkeV2? = null,
    val poengtillegg: Double? = 0.0
    // Not used in PSELV:
    // sluttpoengtallId
    // pt_eos
    // pt_a10
    // fpp_grad_eos
    // merknader
)

// PSELV: Poengrekke
@JsonInclude(NON_NULL)
data class PoengrekkeV2 (
    val pa: Int? = 0,
    val paF92: Int? = 0,
    val paE91: Int? = 0,
    val tpi: Int? = 0,
    val paa: Double? = 0.0,
    val poengtallListe: List<PoengtallV2> = emptyList()
    // Not used in PSELV:
    // poengrekkeId
    // merknader
    // pa_fa_norge
    // tpiEtterHovedregel
    // tpiFaktor
    // pa_no
    // fpa
    // siste_fpp_aar
    // siste_fpp_aar_norden
    // siste_fpp_aar_eos
    // pa_eos_f92
    // pa_eos_e91
    // pa_nordisk_framt_brutto
    // pa_nordisk_framt_netto
    // fpp_eos_snitt
    // pa_fa_norden
    // pa_eos_teoretisk
    // pa_eos_pro_rata
    // pa_pro_rata_teller
    // pa_pro_rata_nevner
    // afpTpoUpGrunnlagAnvendt
    // afpTpoUpGrunnlagOppjustert
)

// PSELV: Poengtall
@JsonInclude(NON_NULL)
data class PoengtallV2 (
    val pp: Double? = 0.0,
    val pia: Int? = 0,
    val pi: Int? = 0,
    val ar: Int? = 0,
    val bruktIBeregning: Boolean? = false,
    val gv: Int? = 0,
    val poengtallType: PoengtalltypeEnum? = null,
    val maksUforegrad: Int? = 0,
    val merknadListe: List<MerknadV2> = emptyList()
    // Not used in PSELV:
    // uforear
)
