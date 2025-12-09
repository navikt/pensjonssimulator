package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.beregning.*
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.*
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning.penobjekter.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import java.util.function.Predicate

/**
 * Objektet inneholder den faktiske pensjonen under utbetaling, samt en liste
 * over delytelsene som denne består av.
 */
class PensjonUnderUtbetaling {
    /**
     * månedsbeløp netto, summen av alle delytelsene i ytelseskomponenter. Disse er
     * avrundet hver for seg til nårmeste krone. Dette medfører at
     * avrund(totalbelopNettoAr/12) vil kunne være forskjellig fra totalbelopNetto.
     */
    var totalbelopNetto = 0

    /**
     * årlig netto beløp under utbetaling
     */
    var totalbelopNettoAr = 0.0

    /**
     * Angir sum brutto per måned.
     */
    var totalbelopBrutto = 0

    /**
     * Angir sum brutto per år.
     */
    var totalbelopBruttoAr = 0.0

    /**
     * Indikerer hvilken beregningsformel som ble brukt.
     */
    var formelKodeEnum: FormelKodeEnum = FormelKodeEnum.BPUx

    var pubReguleringFratrekk = 0.0
    var ytelseskomponenter: MutableList<Ytelseskomponent> = mutableListOf()

    // SIMDOM-ADD
    fun removeYtelseskomponent(filter: Predicate<in Ytelseskomponent>) {
        ytelseskomponenter.removeIf(filter)
    }

    fun addYtelseskomponent(yk: Ytelseskomponent) {
        //removeYtelseskomponent(yk::class)
        ytelseskomponenter.add(yk)
    }

    fun clearYtelseskomponentList() {
        ytelseskomponenter.clear()
    }

    private inline fun <reified T> internalAddOrRemoveIfNull(yk: Ytelseskomponent?) {
        ytelseskomponenter.removeIf { it is T }
        yk?.let { addYtelseskomponent(it) }
    }

    // Copied from https://github.com/JetBrains/kotlin/blob/master/core/util.runtime/src/org/jetbrains/kotlin/utils/addToStdlib.kt#L19
    inline fun <reified T : Any> Iterable<*>.firstIsInstanceOrNull(): T? {
        for (element in this) if (element is T) return element
        return null
    }

    @get:JsonIgnore
    @set:JsonIgnore
    var afpKompensasjonstillegg: AfpKompensasjonstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpKompensasjonstillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var afpKronetillegg: AfpKronetillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpKronetillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var afpTillegg: AfpTillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpTillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var ektefelletillegg: Ektefelletillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Ektefelletillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var ektefelletilleggUT: EktefelletilleggUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<EktefelletilleggUT>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var familietillegg: Familietillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Familietillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var fasteUtgifterTillegg: FasteUtgifterTillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<FasteUtgifterTillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var fasteUtgifterTilleggUT: FasteUtgifterTilleggUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<FasteUtgifterTilleggUT>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var garantipensjon: Garantipensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantipensjon>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var garantitillegg: Garantitillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var garantitillegg_Art_27: Garantitillegg_Art_27?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg_Art_27>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var garantitillegg_Art_27_UT: Garantitillegg_Art_27_UT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg_Art_27_UT>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var garantitillegg_Art_50: Garantitillegg_Art_50?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg_Art_50>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var gjenlevendetillegg: Gjenlevendetillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Gjenlevendetillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var gjenlevendetilleggAP: GjenlevendetilleggAP?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<GjenlevendetilleggAP>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var gjenlevendetilleggAPKap19: GjenlevendetilleggAPKap19?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<GjenlevendetilleggAPKap19>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var grunnpensjon: Grunnpensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Grunnpensjon>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var hjelpeloshetsbidrag: Hjelpeloshetsbidrag?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Hjelpeloshetsbidrag>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var inntektspensjon: Inntektspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Inntektspensjon>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var krigOgGammelYrkesskade: KrigOgGammelYrkesskade?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<KrigOgGammelYrkesskade>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var mendel: Mendel?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Mendel>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var minstenivatilleggIndividuelt: MinstenivatilleggIndividuelt?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<MinstenivatilleggIndividuelt>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var minstenivatilleggPensjonistpar: MinstenivatilleggPensjonistpar?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<MinstenivatilleggPensjonistpar>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var paragraf_8_5_1_tillegg: Paragraf_8_5_1_tillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Paragraf_8_5_1_tillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var pensjonstillegg: Pensjonstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Pensjonstillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var sertillegg: Sertillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Sertillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var skjermingstillegg: Skjermingstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Skjermingstillegg>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var temporarYtelseskomponent: TemporarYtelseskomponent?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<TemporarYtelseskomponent>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var tilleggspensjon: Tilleggspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Tilleggspensjon>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var tilleggTilHjelpIHuset: TilleggTilHjelpIHuset?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<TilleggTilHjelpIHuset>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var uforetilleggTilAlderspensjon: UforetilleggTilAlderspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<UforetilleggTilAlderspensjon>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var uforetrygdOrdiner: UforetrygdOrdiner?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<UforetrygdOrdiner>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var ventetillegg: Ventetillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Ventetillegg>(yk)

    // AbstraktBarnetillegg:
    @get:JsonIgnore
    @set:JsonIgnore
    var barnetilleggSerkullsbarn: BarnetilleggSerkullsbarn?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggSerkullsbarn>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var barnetilleggFellesbarn: BarnetilleggFellesbarn?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggFellesbarn>(yk)

    // AbstraktBarnetillegg.AbstraktBarnetilleggUT:
    @get:JsonIgnore
    @set:JsonIgnore
    var barnetilleggFellesbarnUT: BarnetilleggFellesbarnUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggFellesbarnUT>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var barnetilleggSerkullsbarnUT: BarnetilleggSerkullsbarnUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggSerkullsbarnUT>(yk)

    // AfpLivsvarig:
    @get:JsonIgnore
    @set:JsonIgnore
    var afpOffentligLivsvarig: AfpOffentligLivsvarig?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpOffentligLivsvarig>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var afpPrivatLivsvarig: AfpPrivatLivsvarig?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpPrivatLivsvarig>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var fremskrevetAfpLivsvarig: FremskrevetAfpLivsvarig?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<FremskrevetAfpLivsvarig>(yk)

    // Grunnpensjon:
    @get:JsonIgnore
    @set:JsonIgnore
    var basisGrunnpensjon: BasisGrunnpensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BasisGrunnpensjon>(yk)

    // Tilleggspensjon:
    @get:JsonIgnore
    @set:JsonIgnore
    var basisTilleggspensjon: BasisTilleggspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BasisTilleggspensjon>(yk)

    // Pensjonstillegg:
    @get:JsonIgnore
    @set:JsonIgnore
    var basisPensjonstillegg: BasisPensjonstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BasisPensjonstillegg>(yk)

    // BeregningYtelseskomponent:
    @get:JsonIgnore
    @set:JsonIgnore
    var skattefriGrunnpensjon: SkattefriGrunnpensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<SkattefriGrunnpensjon>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var skattefriUforetrygdOrdiner: SkattefriUforetrygdOrdiner?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<SkattefriUforetrygdOrdiner>(yk)

    // BeregningYtelseskomponent.MotregningYtelseskomponent:
    @get:JsonIgnore
    @set:JsonIgnore
    var arbeidsavklaringspenger: Arbeidsavklaringspenger?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Arbeidsavklaringspenger>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var arbeidsavklaringspengerUT: ArbeidsavklaringspengerUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<ArbeidsavklaringspengerUT>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var sykepenger: Sykepenger?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Sykepenger>(yk)

    @get:JsonIgnore
    @set:JsonIgnore
    var sykepengerUT: SykepengerUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<SykepengerUT>(yk)

    /**
     * Avoids the com.fasterxml.jackson.databind.exc.InvalidDefinitionException:
     * Cannot construct instance of `no.nav.pensjon.simulator.core.domain.regler.beregning2011.PensjonUnderUtbetaling` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)
     */
    @JsonCreator
    constructor()

    // SIMDOM-ADD excludeBrutto
    // since no.nav.domain.pensjon.kjerne.beregning2011.PensjonUnderUtbetaling excludes brutto
    constructor(source: PensjonUnderUtbetaling, excludeBrutto: Boolean = false) : super() {
        totalbelopNetto = source.totalbelopNetto
        totalbelopNettoAr = source.totalbelopNettoAr

        if (!excludeBrutto) {
            totalbelopBrutto = source.totalbelopBrutto
            totalbelopBruttoAr = source.totalbelopBruttoAr
        }

        formelKodeEnum = source.formelKodeEnum
        pubReguleringFratrekk = source.pubReguleringFratrekk

        for (komponent in source.ytelseskomponenter) {
            when (komponent) {
                is AfpKompensasjonstillegg -> addYtelseskomponent(komponent.copy())
                is AfpKronetillegg -> addYtelseskomponent(komponent.copy())
                is AfpTillegg -> addYtelseskomponent(komponent.copy())
                is Ektefelletillegg -> addYtelseskomponent(Ektefelletillegg(komponent))
                is Familietillegg -> addYtelseskomponent(komponent.copy())
                is FasteUtgifterTillegg -> addYtelseskomponent(komponent.copy())
                is FasteUtgifterTilleggUT -> addYtelseskomponent(komponent.copy())
                is Garantipensjon -> addYtelseskomponent(komponent.copy())
                is Garantitillegg -> addYtelseskomponent(komponent.copy())
                is Garantitillegg_Art_27 -> addYtelseskomponent(komponent.copy())
                is Garantitillegg_Art_27_UT -> addYtelseskomponent(komponent.copy())
                is Garantitillegg_Art_50 -> addYtelseskomponent(komponent.copy())
                is GjenlevendetilleggAP -> addYtelseskomponent(komponent.copy())
                is GjenlevendetilleggAPKap19 -> addYtelseskomponent(komponent.copy())
                is Hjelpeloshetsbidrag -> addYtelseskomponent(komponent.copy())
                is Inntektspensjon -> addYtelseskomponent(komponent.copy())
                is KrigOgGammelYrkesskade -> addYtelseskomponent(komponent.copy())
                is Mendel -> addYtelseskomponent(komponent.copy())
                is MinstenivatilleggIndividuelt -> addYtelseskomponent(komponent.copy())
                is MinstenivatilleggPensjonistpar -> addYtelseskomponent(komponent.copy())
                is Paragraf_8_5_1_tillegg -> addYtelseskomponent(komponent.copy())
                is Sertillegg -> addYtelseskomponent(komponent.copy())
                is Skjermingstillegg -> addYtelseskomponent(komponent.copy())
                is TemporarYtelseskomponent -> addYtelseskomponent(komponent.copy())
                is TilleggTilHjelpIHuset -> addYtelseskomponent(komponent.copy())
                is UforetilleggTilAlderspensjon -> addYtelseskomponent(komponent.copy())
                //--- Grunnpensjon:
                is BasisGrunnpensjon -> addYtelseskomponent(BasisGrunnpensjon(komponent))
                is Grunnpensjon -> addYtelseskomponent(Grunnpensjon(komponent))
                //--- Pensjonstillegg:
                is BasisPensjonstillegg -> addYtelseskomponent(BasisPensjonstillegg(komponent))
                is Pensjonstillegg -> addYtelseskomponent(Pensjonstillegg(komponent))
                //--- Tilleggspensjon:
                is BasisTilleggspensjon -> addYtelseskomponent(BasisTilleggspensjon(komponent))
                is Tilleggspensjon -> addYtelseskomponent(Tilleggspensjon(komponent))
                //--- AbstraktBarnetillegg + UforetrygdYtelseskomponent:
                is EktefelletilleggUT -> addYtelseskomponent(komponent.copy())
                is UforetrygdOrdiner -> addYtelseskomponent(komponent.copy())
                is Gjenlevendetillegg -> addYtelseskomponent(komponent.copy())
                is BarnetilleggFellesbarn -> addYtelseskomponent(komponent.copy())
                is BarnetilleggFellesbarnUT -> addYtelseskomponent(komponent.copy())
                is BarnetilleggSerkullsbarn -> addYtelseskomponent(komponent.copy())
                is BarnetilleggSerkullsbarnUT -> addYtelseskomponent(komponent.copy())
                //--- AbstraktAfpLivsvarig:
                is AfpOffentligLivsvarig -> addYtelseskomponent(komponent.copy())
                is AfpPrivatLivsvarig -> addYtelseskomponent(komponent.copy())
                is FremskrevetAfpLivsvarig -> addYtelseskomponent(komponent.copy())
                //--- BeregningYtelseskomponent:
                is Arbeidsavklaringspenger -> addYtelseskomponent(komponent.copy())
                is ArbeidsavklaringspengerUT -> addYtelseskomponent(komponent.copy())
                is SkattefriGrunnpensjon -> addYtelseskomponent(komponent.copy())
                is SkattefriUforetrygdOrdiner -> addYtelseskomponent(komponent.copy())
                is Sykepenger -> addYtelseskomponent(komponent.copy())
                is SykepengerUT -> addYtelseskomponent(komponent.copy())
                // end BeregningYtelseskomponent ---
                else -> {
                    val constructor = komponent.javaClass.let { it.getConstructor(it) }
                    addYtelseskomponent(constructor.newInstance(komponent))
                }
            }
        }
    }

    override fun toString(): String =
        "totalbelopNetto: $totalbelopNetto, totalbelopBruttoAr: $totalbelopBruttoAr"
    // end SIMDOM-MOD
}
