package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.*
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti
import java.io.Serializable
import java.math.RoundingMode

/**
 * Superklasse for alle ytelser, Grunnpensjon, Sertillegg, AfpTillegg osv. For
 * alle ytelser gjelder at brutto - netto = fradrag.
 */
@JsonSubTypes(
    JsonSubTypes.Type(value = AfpKompensasjonstillegg::class),
    JsonSubTypes.Type(value = AfpKronetillegg::class),
    JsonSubTypes.Type(value = AfpLivsvarig::class),
    JsonSubTypes.Type(value = AfpTillegg::class),
    JsonSubTypes.Type(value = BeregningYtelseskomponent::class),
    JsonSubTypes.Type(value = Ektefelletillegg::class),
    JsonSubTypes.Type(value = EktefelletilleggUT::class),
    JsonSubTypes.Type(value = Familietillegg::class),
    JsonSubTypes.Type(value = FasteUtgifterTillegg::class),
    JsonSubTypes.Type(value = FasteUtgifterTilleggUT::class),
    JsonSubTypes.Type(value = Garantipensjon::class),
    JsonSubTypes.Type(value = Garantitillegg::class),
    JsonSubTypes.Type(value = Garantitillegg_Art_27::class),
    JsonSubTypes.Type(value = Garantitillegg_Art_27_UT::class),
    JsonSubTypes.Type(value = Garantitillegg_Art_50::class),
    JsonSubTypes.Type(value = Gjenlevendetillegg::class),
    JsonSubTypes.Type(value = GjenlevendetilleggAP::class),
    JsonSubTypes.Type(value = Grunnpensjon::class),
    JsonSubTypes.Type(value = Hjelpeloshetsbidrag::class),
    JsonSubTypes.Type(value = Inntektspensjon::class),
    JsonSubTypes.Type(value = KrigOgGammelYrkesskade::class),
    JsonSubTypes.Type(value = Mendel::class),
    JsonSubTypes.Type(value = MinstenivatilleggIndividuelt::class),
    JsonSubTypes.Type(value = MinstenivatilleggPensjonistpar::class),
    JsonSubTypes.Type(value = Paragraf_8_5_1_tillegg::class),
    JsonSubTypes.Type(value = Pensjonstillegg::class),
    JsonSubTypes.Type(value = Sertillegg::class),
    JsonSubTypes.Type(value = Skjermingstillegg::class),
    JsonSubTypes.Type(value = TemporarYtelseskomponent::class),
    JsonSubTypes.Type(value = Tilleggspensjon::class),
    JsonSubTypes.Type(value = TilleggTilHjelpIHuset::class),
    JsonSubTypes.Type(value = UforetilleggTilAlderspensjon::class),
    JsonSubTypes.Type(value = UforetrygdOrdiner::class),
    JsonSubTypes.Type(value = Ventetillegg::class),
    JsonSubTypes.Type(value = AbstraktBarnetillegg::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class Ytelseskomponent(
    var brutto: Int = 0,
    var netto: Int = 0,
    var fradrag: Int = 0,
    var bruttoPerAr: Double = 0.0,
    var nettoPerAr: Double = 0.0,
    var fradragPerAr: Double = 0.0,
    var ytelsekomponentType: YtelsekomponentTypeCti,
    var merknadListe: MutableList<Merknad> = mutableListOf(),
    /**
     * Angir om ytelseskomponenten går til utbetaling eller tilbakekreving.
     * Settes ikke i PREG, men mappes slik at vi ikke mister den ved kall til regeltjenester som returnerer kopier av innsendt ytelseskomponent (f.eks. faktoromregning).
     */
    var fradragsTransaksjon: Boolean = false,
    /**
     * Angir om ytelseskomponenten er opphørt.
     * Settes ikke i PREG, men mappes slik at vi ikke mister den ved kall til regeltjenester som returnerer kopier av innsendt ytelseskomponent (f.eks. faktoromregning).
     */
    var opphort: Boolean = false,
    /**
     * Angir sakentypen ytelseskomponenten er knyttet til.
     * Settes ikke i PREG, men mappes slik at vi ikke mister den ved kall til regeltjenester som returnerer kopier av innsendt ytelseskomponent (f.eks. faktoromregning).
     */
    var sakType: SakTypeCti? = null,
    /**
     * Indikerer hvilken beregningsformel som ble brukt.
     */
    var formelKode: FormelKodeCti? = null,
    var reguleringsInformasjon: ReguleringsInformasjon? = null
) : Serializable {

    constructor(ytelseskomponent: Ytelseskomponent) : this(
        ytelsekomponentType = YtelsekomponentTypeCti(ytelseskomponent.ytelsekomponentType),
        formelKode = if (ytelseskomponent.formelKode != null) FormelKodeCti(ytelseskomponent.formelKode!!) else null
    ) {
        this.brutto = ytelseskomponent.brutto
        this.netto = ytelseskomponent.netto
        this.fradrag = ytelseskomponent.fradrag
        this.bruttoPerAr = ytelseskomponent.bruttoPerAr
        this.nettoPerAr = ytelseskomponent.nettoPerAr
        this.fradragPerAr = ytelseskomponent.fradragPerAr
        for (merknad in ytelseskomponent.merknadListe) {
            this.merknadListe.add(Merknad(merknad))
        }
        if (ytelseskomponent.reguleringsInformasjon != null) {
            this.reguleringsInformasjon = ReguleringsInformasjon(ytelseskomponent.reguleringsInformasjon!!)
        }
        this.fradragsTransaksjon = ytelseskomponent.fradragsTransaksjon
        this.opphort = ytelseskomponent.opphort
        if (ytelseskomponent.sakType != null) {
            this.sakType = SakTypeCti(ytelseskomponent.sakType!!)
        }
        this.brukt = ytelseskomponent.brukt // SIMDOM-ADD
        this.unroundedNettoPerAr = ytelseskomponent.unroundedNettoPerAr // SIMDOM-ADD
    }

    // SIMDOM-ADD
    @JsonIgnore var brukt: Boolean = true
    @JsonIgnore private var unroundedNettoPerAr: Double? = null

    val internNettoPerAr: Double
        @JsonIgnore get() = unroundedNettoPerAr ?: nettoPerAr

    // nettoPerAr in kjerne/PEN is integer
    fun roundNettoPerAr() {
        unroundedNettoPerAr = nettoPerAr
        nettoPerAr = nettoPerAr.toBigDecimal().setScale(0, RoundingMode.UP).toDouble()
    }
}
