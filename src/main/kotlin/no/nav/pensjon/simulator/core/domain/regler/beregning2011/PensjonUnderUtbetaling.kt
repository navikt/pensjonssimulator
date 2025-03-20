package no.nav.pensjon.simulator.core.domain.regler.beregning2011

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

    var afpKompensasjonstillegg: AfpKompensasjonstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpKompensasjonstillegg>(yk)
    var afpKronetillegg: AfpKronetillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpKronetillegg>(yk)
    var afpTillegg: AfpTillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpTillegg>(yk)
    var ektefelletillegg: Ektefelletillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Ektefelletillegg>(yk)
    var ektefelletilleggUT: EktefelletilleggUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<EktefelletilleggUT>(yk)
    var familietillegg: Familietillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Familietillegg>(yk)
    var fasteUtgifterTillegg: FasteUtgifterTillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<FasteUtgifterTillegg>(yk)
    var fasteUtgifterTilleggUT: FasteUtgifterTilleggUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<FasteUtgifterTilleggUT>(yk)
    var garantipensjon: Garantipensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantipensjon>(yk)
    var garantitillegg: Garantitillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg>(yk)
    var garantitillegg_Art_27: Garantitillegg_Art_27?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg_Art_27>(yk)
    var garantitillegg_Art_27_UT: Garantitillegg_Art_27_UT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg_Art_27_UT>(yk)
    var garantitillegg_Art_50: Garantitillegg_Art_50?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg_Art_50>(yk)
    var gjenlevendetillegg: Gjenlevendetillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Gjenlevendetillegg>(yk)
    var gjenlevendetilleggAP: GjenlevendetilleggAP?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<GjenlevendetilleggAP>(yk)
    var gjenlevendetilleggAPKap19: GjenlevendetilleggAPKap19?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<GjenlevendetilleggAPKap19>(yk)
    var grunnpensjon: Grunnpensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Grunnpensjon>(yk)
    var hjelpeloshetsbidrag: Hjelpeloshetsbidrag?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Hjelpeloshetsbidrag>(yk)
    var inntektspensjon: Inntektspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Inntektspensjon>(yk)
    var krigOgGammelYrkesskade: KrigOgGammelYrkesskade?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<KrigOgGammelYrkesskade>(yk)
    var mendel: Mendel?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Mendel>(yk)
    var minstenivatilleggIndividuelt: MinstenivatilleggIndividuelt?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<MinstenivatilleggIndividuelt>(yk)
    var minstenivatilleggPensjonistpar: MinstenivatilleggPensjonistpar?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<MinstenivatilleggPensjonistpar>(yk)
    var paragraf_8_5_1_tillegg: Paragraf_8_5_1_tillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Paragraf_8_5_1_tillegg>(yk)
    var pensjonstillegg: Pensjonstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Pensjonstillegg>(yk)
    var sertillegg: Sertillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Sertillegg>(yk)
    var skjermingstillegg: Skjermingstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Skjermingstillegg>(yk)
    var temporarYtelseskomponent: TemporarYtelseskomponent?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<TemporarYtelseskomponent>(yk)
    var tilleggspensjon: Tilleggspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Tilleggspensjon>(yk)
    var tilleggTilHjelpIHuset: TilleggTilHjelpIHuset?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<TilleggTilHjelpIHuset>(yk)
    var uforetilleggTilAlderspensjon: UforetilleggTilAlderspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<UforetilleggTilAlderspensjon>(yk)
    var uforetrygdOrdiner: UforetrygdOrdiner?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<UforetrygdOrdiner>(yk)
    var ventetillegg: Ventetillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Ventetillegg>(yk)

    // AbstraktBarnetillegg:
    var barnetilleggSerkullsbarn: BarnetilleggSerkullsbarn?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggSerkullsbarn>(yk)
    var barnetilleggFellesbarn: BarnetilleggFellesbarn?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggFellesbarn>(yk)

    // AbstraktBarnetillegg.AbstraktBarnetilleggUT:
    var barnetilleggFellesbarnUT: BarnetilleggFellesbarnUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggFellesbarnUT>(yk)
    var barnetilleggSerkullsbarnUT: BarnetilleggSerkullsbarnUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggSerkullsbarnUT>(yk)

    // AfpLivsvarig:
    var afpLivsvarig: AfpLivsvarig?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpLivsvarig>(yk)
    var afpOffentligLivsvarig: AfpOffentligLivsvarig?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpOffentligLivsvarig>(yk)
    var afpPrivatLivsvarig: AfpPrivatLivsvarig?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpPrivatLivsvarig>(yk)
    var fremskrevetAfpLivsvarig: FremskrevetAfpLivsvarig?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<FremskrevetAfpLivsvarig>(yk)

    // Grunnpensjon:
    var basisGrunnpensjon: BasisGrunnpensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BasisGrunnpensjon>(yk)

    // Tilleggspensjon:
    var basisTilleggspensjon: BasisTilleggspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BasisTilleggspensjon>(yk)

    // Pensjonstillegg:
    var basisPensjonstillegg: BasisPensjonstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BasisPensjonstillegg>(yk)

    // BeregningYtelseskomponent:
    var skattefriGrunnpensjon: SkattefriGrunnpensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<SkattefriGrunnpensjon>(yk)
    var skattefriUforetrygdOrdiner: SkattefriUforetrygdOrdiner?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<SkattefriUforetrygdOrdiner>(yk)

    // BeregningYtelseskomponent.MotregningYtelseskomponent:
    var arbeidsavklaringspenger: Arbeidsavklaringspenger?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Arbeidsavklaringspenger>(yk)
    var arbeidsavklaringspengerUT: ArbeidsavklaringspengerUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<ArbeidsavklaringspengerUT>(yk)
    var sykepenger: Sykepenger?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Sykepenger>(yk)
    var sykepengerUT: SykepengerUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<SykepengerUT>(yk)

    /**
     * Avoids the com.fasterxml.jackson.databind.exc.InvalidDefinitionException:
     * Cannot construct instance of `no.nav.pensjon.simulator.core.domain.regler.beregning2011.PensjonUnderUtbetaling` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)
     */
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
                is AfpLivsvarig -> addYtelseskomponent(komponent.copy())
                //--- AbstraktAfpLivsvarig:
                is AfpOffentligLivsvarig -> addYtelseskomponent(komponent.copy())
                is AfpPrivatLivsvarig -> addYtelseskomponent(komponent.copy())
                is FremskrevetAfpLivsvarig -> addYtelseskomponent(komponent.copy())
                // end AbstraktAfpLivsvarig ---
                is Ektefelletillegg -> addYtelseskomponent(Ektefelletillegg(komponent))
                is Familietillegg -> addYtelseskomponent(komponent.copy())
                //--- BeregningYtelseskomponent:
                is Arbeidsavklaringspenger -> addYtelseskomponent(komponent.copy())
                is ArbeidsavklaringspengerUT -> addYtelseskomponent(komponent.copy())
                is SkattefriGrunnpensjon -> addYtelseskomponent(komponent.copy())
                is SkattefriUforetrygdOrdiner -> addYtelseskomponent(komponent.copy())
                is Sykepenger -> addYtelseskomponent(komponent.copy())
                is SykepengerUT -> addYtelseskomponent(komponent.copy())
                // end BeregningYtelseskomponent ---
                //TODO the other ytelseskomponent subclasses
                else -> {
                    val constructor = komponent.javaClass.let { it.getConstructor(it) }
                    addYtelseskomponent(constructor.newInstance(komponent))
                }
            }
        }
    }
    // end SIMDOM-MOD
}
