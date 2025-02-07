package no.nav.pensjon.simulator.sak.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class PenVirkningsdatoResult(
    val forsteVirkningsdatoGrunnlagListe: List<PenVirkningsdatoGrunnlag>
)

/**
 * Pen<entity> corresponds to <entity>DtoForSimulator in PEN
 */
data class PenVirkningsdatoGrunnlag(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val virkningsdato: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val kravFremsattDato: LocalDate? = null,
    val bruker: PenPenPerson? = null,
    val annenPerson: PenPenPerson? = null,
    val kravlinjeType: String? = null
)

data class PenPenPerson(
    val penPersonId: Long = 0,
    val pid: Pid? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fodselsdato: LocalDate? = null,
    val afpHistorikkListe: MutableList<PenAfpHistorikk>? = null,
    val uforehistorikk: PenUfoerehistorikk? = null,
    val generellHistorikk: PenGenerellHistorikk? = null
)

data class PenAfpHistorikk(
    val afpFpp: Double = 0.0, // Fremtidig pensjonspoeng
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val virkFom: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val virkTom: LocalDate? = null,
    val afpPensjonsgrad: Int = 0,
    val afpOrdning: String? = null
)

data class PenUfoerehistorikk(
    val uforeperiodeListe: List<PenUfoereperiode> = emptyList(),
    val garantigrad: Int = 0, // Uføregraden pensjonen er blitt fryst fra
    val garantigradYrke: Int = 0, // Yrkesskadegraden pensjonen er blitt fryst fra
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val sistMedlITrygden: LocalDate? = null // Dato for sist innmeldt i Folketrygden- for fremtidig trygdetid
)

data class PenGenerellHistorikk(
    val generellHistorikkId: Long = 0,
    val fravik_19_3: String? = null,
    val fpp_eos: Double = 0.0,
    val ventetilleggsgrunnlag: PenVentetilleggsgrunnlag? = null,
    val poengtillegg: String? = null,
    val eosEkstra: PenEosEkstra? = null, // Info om tidligere EØS-beregninger, brukes ved konvertering til AP
    val garantiTrygdetid: PenGarantiTrygdetid? = null, // trygdetidsgarantien for ektefeller som går under gammel lov før 1.1.1991
    val sertillegg1943kull: PenSaertillegg? = null,
    val giftFor2011: Boolean = false // Gift eller tilsvarende med samme person siden 31.12.2010
)

data class PenVentetilleggsgrunnlag(
    val ventetilleggprosent: Double = 0.0,
    val vt_spt: Double = 0.0,
    val vt_opt: Double = 0.0,
    val vt_pa: Int = 0,
    val tt_vent: Int = 0
)

data class PenEosEkstra(
    val proRataBeregningType: String? = null,
    val redusertAntFppAr: Int = 0,
    val spt_eos: Double = 0.0,
    val spt_pa_f92_eos: Int = 0,
    val spt_pa_e91_eos: Int = 0,
    val vilkar3_17Aok: Boolean = false
)

data class PenGarantiTrygdetid(
    var trygdetid_garanti: Int = 0,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fomDato: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val tomDato: LocalDate? = null
)

data class PenSaertillegg(
    val pSats_st: Double = 0.0,
    // Ytelseskomponent:
    val brutto: Int = 0,
    val netto: Int = 0,
    val fradrag: Int = 0,
    val bruttoPerAr: Double = 0.0,
    val nettoPerAr: Double = 0.0,
    val fradragPerAr: Double = 0.0,
    val ytelsekomponentType: String = "ST", // særtillegg
    val merknadListe: List<PenMerknad> = emptyList(),
    val fradragsTransaksjon: Boolean = false,
    val opphort: Boolean = false,
    val sakType: String? = null,
    val formelKode: String? = null,
    val reguleringsInformasjon: PenReguleringsInformasjon? = null
)

data class PenMerknad(
    val kode: String = "",
    val argumentListe: List<String> = emptyList()
)

data class PenReguleringsInformasjon(
    val lonnsvekst: Double = 0.0,
    val fratrekksfaktor: Double = 0.0,
    val gammelG: Int = 0,
    val nyG: Int = 0,
    val reguleringsfaktor: Double = 0.0,
    val gjennomsnittligUttaksgradSisteAr: Double = 0.0,
    val reguleringsbelop: Double = 0.0,
    val prisOgLonnsvekst: Double = 0.0
)

data class PenUfoereperiode(
    val ufg: Int = 0,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val uft: LocalDate? = null,
    val uforeType: String? = null,
    val fppGaranti: Double = 0.0,
    val fppGarantiKode: String? = null,
    val redusertAntFppAr: Int = 0,
    val redusertAntFppAr_proRata: Int = 0,
    val proRataBeregningType: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val virk: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val uftTom: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val ufgFom: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val ufgTom: LocalDate? = null,
    val fodselsArYngsteBarn: Int = 0,
    val spt: Double = 0.0,
    val spt_proRata: Double = 0.0,
    val opt: Double = 0.0,
    val ypt: Double = 0.0,
    val spt_pa_f92: Int = 0,
    val spt_pa_e91: Int = 0,
    val proRata_teller: Int = 0,
    val proRata_nevner: Int = 0,
    val opt_pa_f92: Int = 0,
    val opt_pa_e91: Int = 0,
    val ypt_pa_f92: Int = 0,
    val ypt_pa_e91: Int = 0,
    val paa: Double = 0.0,
    val fpp: Double = 0.0,
    val fpp_omregnet: Double = 0.0,
    val spt_eos: Double = 0.0,
    val spt_pa_e91_eos: Int = 0,
    val spt_pa_f92_eos: Int = 0,
    val beregningsgrunnlag: Int = 0,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val angittUforetidspunkt: LocalDate? = null,
    val antattInntektFaktorKap19: Double = 0.0,
    val antattInntektFaktorKap20: Double = 0.0
)
