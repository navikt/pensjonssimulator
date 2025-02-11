package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.*
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
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
    JsonSubTypes.Type(value = GjenlevendetilleggAPKap19::class),
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
abstract class Ytelseskomponent {
    /**
     * Brutto beløp.
     */
    open var brutto = 0

    /**
     * Netto beløp.
     */
    open var netto = 0

    /**
     * Fradraget: brutto - netto
     */
    var fradrag = 0

    /**
     * Ikke avrundet beløp, gjelder for hele året.
     */
    var bruttoPerAr = 0.0

    /**
     * årlig netto utbetalt sum.
     */
    var nettoPerAr = 0.0

    /**
     * Ytelsens fradrag per år.
     */
    var fradragPerAr = 0.0

    /**
     * Type ytelse, verdi fra kodeverk.
     */
    abstract var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum

    /**
     * Liste av merknader.
     */
    var merknadListe: MutableList<Merknad> = mutableListOf()

    /**
     * Indikerer hvilken beregningsformel som ble brukt.
     */
    var formelKodeEnum: FormelKodeEnum? = null

    /**
     * Informasjon om regulering av ytelsen.
     */
    var reguleringsInformasjon: ReguleringsInformasjon? = null

    /**
     * Angir om ytelseskomponenten går til utbetaling eller tilbakekreving.
     * Settes ikke i pensjon-regler, men mappes slik at vi ikke mister den ved kall til regeltjenester som returnerer kopier av innsendt ytelseskomponent (f.eks. faktoromregning).
     */
    var fradragsTransaksjon = false

    /**
     * Angir om ytelseskomponenten er opphørt.
     * Settes ikke i pensjon-regler, men mappes slik at vi ikke mister den ved kall til regeltjenester som returnerer kopier av innsendt ytelseskomponent (f.eks. faktoromregning).
     */
    var opphort = false

    /**
     * Angir sakentypen ytelseskomponenten er knyttet til.
     * Settes ikke i pensjon-regler, men mappes slik at vi ikke mister den ved kall til regeltjenester som returnerer kopier av innsendt ytelseskomponent (f.eks. faktoromregning).
     */
    var sakTypeEnum: SakTypeEnum? = null

    constructor()

    constructor(typeEnum: YtelseskomponentTypeEnum) {
        ytelsekomponentTypeEnum = typeEnum
    }

    constructor(source: Ytelseskomponent) {
        brutto = source.brutto
        netto = source.netto
        fradrag = source.fradrag
        bruttoPerAr = source.bruttoPerAr
        nettoPerAr = source.nettoPerAr
        fradragPerAr = source.fradragPerAr
        ytelsekomponentTypeEnum = source.ytelsekomponentTypeEnum

        if (source.formelKodeEnum != null) {
            formelKodeEnum = source.formelKodeEnum
        }

        for (merknad in source.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }

        if (source.reguleringsInformasjon != null) {
            reguleringsInformasjon = ReguleringsInformasjon(source.reguleringsInformasjon!!)
        }

        fradragsTransaksjon = source.fradragsTransaksjon
        opphort = source.opphort

        if (source.sakTypeEnum != null) {
            sakTypeEnum = source.sakTypeEnum
        }

        brukt = source.brukt // SIMDOM-ADD
        unroundedNettoPerAr = source.unroundedNettoPerAr // SIMDOM-ADD
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
