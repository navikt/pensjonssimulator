package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.pensjon.simulator.core.domain.regler.beregning.*
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.*
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import java.io.Serializable
import java.util.function.Predicate
import kotlin.reflect.KClass

/**
 * Objektet inneholder den faktiske pensjonen under utbetaling, samt en liste
 * over delytelsene som denne består av.
 */
class PensjonUnderUtbetaling : Serializable {
    /**
     * Månedsbeløp netto, summen av alle delytelsene i ytelseskomponenter. Disse er
     * avrundet hver for seg til nærmeste krone. Dette medfører at
     * avrund(totalbelopNettoAr/12) vil kunne være forskjellig fra totalbelopNetto.
     */
    var totalbelopNetto: Int = 0

    /**
     * Årlig netto beløp under utbetaling
     */
    var totalbelopNettoAr: Double = 0.0

    /**
     * Angir sum brutto per måned.
     */
    var totalbelopBrutto: Int = 0

    /**
     * Angir sum brutto per år.
     */
    var totalbelopBruttoAr: Double = 0.0

    /**
     * Indikerer hvilken beregningsformel som ble brukt.
     */
    var formelKode: FormelKodeCti? = null

    var pubReguleringFratrekk: Double = 0.0

    @JsonProperty("ytelseskomponenter")
    private val internalYtelseskomponenter: MutableList<Ytelseskomponent> = mutableListOf()

    var ytelseskomponenter: List<Ytelseskomponent>
        get() = internalYtelseskomponenter.toList()
        set(value) {
            internalYtelseskomponenter.clear()
            internalYtelseskomponenter.addAll(value)
        }

    fun addYtelseskomponent(yk: Ytelseskomponent) {
        removeYtelseskomponent(yk::class)
        internalYtelseskomponenter.add(yk)
    }

    fun addYtelseskomponentList(ykList: List<Ytelseskomponent>) {
        ykList.forEach { addYtelseskomponent(it) }
    }

    fun removeYtelseskomponent(yk: KClass<out Ytelseskomponent>) {
    }

    // SIMDOM-ADD
    fun removeYtelseskomponent(filter: Predicate<in Ytelseskomponent>) {
        internalYtelseskomponenter.removeIf(filter) // { filter }
    }

    fun clearYtelseskomponentList() {
        internalYtelseskomponenter.clear()
    }

    private inline fun <reified T> internalAddOrRemoveIfNull(yk: Ytelseskomponent?) {
        internalYtelseskomponenter.removeIf { it is T }
        yk?.let { addYtelseskomponent(it) }
    }

    // SIMDOM-MOD
    // Copied from https://github.com/JetBrains/kotlin/blob/master/core/util.runtime/src/org/jetbrains/kotlin/utils/addToStdlib.kt#L19
    inline fun <reified T : Any> Iterable<*>.firstIsInstanceOrNull(): T? {
        for (element in this) if (element is T) return element
        return null
    }

    var grunnpensjon: Grunnpensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Grunnpensjon>(yk)
    var tilleggspensjon: Tilleggspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Tilleggspensjon>(yk)
    var pensjonstillegg: Pensjonstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Pensjonstillegg>(yk)
    var uforetrygdOrdiner: UforetrygdOrdiner?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<UforetrygdOrdiner>(yk)
    var minstenivatilleggPensjonistpar: MinstenivatilleggPensjonistpar?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<MinstenivatilleggPensjonistpar>(yk)
    var minstenivatilleggIndividuelt: MinstenivatilleggIndividuelt?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<MinstenivatilleggIndividuelt>(yk)
    var garantipensjon: Garantipensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantipensjon>(yk)
    var inntektspensjon: Inntektspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Inntektspensjon>(yk)
    var garantitillegg: Garantitillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg>(yk)
    var gjenlevendetillegg: Gjenlevendetillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Gjenlevendetillegg>(yk)
    var afpLivsvarig: AfpLivsvarig?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpLivsvarig>(yk)
    var afpKronetillegg: AfpKronetillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpKronetillegg>(yk)
    var afpKompensasjonstillegg: AfpKompensasjonstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpKompensasjonstillegg>(yk)
    var skjermingstillegg: Skjermingstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Skjermingstillegg>(yk)
    var sertillegg: Sertillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Sertillegg>(yk)
    var ventetillegg: Ventetillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Ventetillegg>(yk)
    var familietillegg: Familietillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Familietillegg>(yk)
    var fasteUtgifterTillegg: FasteUtgifterTillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<FasteUtgifterTillegg>(yk)
    var barnetilleggSerkullsbarn: BarnetilleggSerkullsbarn?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggSerkullsbarn>(yk)
    var barnetilleggSerkullsbarnUT: BarnetilleggSerkullsbarnUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggSerkullsbarnUT>(yk)
    var barnetilleggFellesbarn: BarnetilleggFellesbarn?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggFellesbarn>(yk)
    var barnetilleggFellesbarnUT: BarnetilleggFellesbarnUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BarnetilleggFellesbarnUT>(yk)
    var ektefelletillegg: Ektefelletillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Ektefelletillegg>(yk)
    var ektefelletilleggUT: EktefelletilleggUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<EktefelletilleggUT>(yk)
    var fremskrevetAfpLivsvarig: FremskrevetAfpLivsvarig?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<FremskrevetAfpLivsvarig>(yk)
    var paragraf_8_5_1_tillegg: Paragraf_8_5_1_tillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Paragraf_8_5_1_tillegg>(yk)
    var afpTillegg: AfpTillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<AfpTillegg>(yk)
    var garantitillegg_Art_27: Garantitillegg_Art_27?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg_Art_27>(yk)
    var garantitillegg_Art_50: Garantitillegg_Art_50?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg_Art_50>(yk)
    var hjelpeloshetsbidrag: Hjelpeloshetsbidrag?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Hjelpeloshetsbidrag>(yk)
    var krigOgGammelYrkesskade: KrigOgGammelYrkesskade?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<KrigOgGammelYrkesskade>(yk)
    var mendel: Mendel?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Mendel>(yk)
    var tilleggTilHjelpIHuset: TilleggTilHjelpIHuset?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<TilleggTilHjelpIHuset>(yk)
    var basisGrunnpensjon: BasisGrunnpensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BasisGrunnpensjon>(yk)
    var basisTilleggspensjon: BasisTilleggspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BasisTilleggspensjon>(yk)
    var basisPensjonstillegg: BasisPensjonstillegg?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<BasisPensjonstillegg>(yk)
    var arbeidsavklaringspenger: Arbeidsavklaringspenger?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Arbeidsavklaringspenger>(yk)
    var arbeidsavklaringspengerUT: ArbeidsavklaringspengerUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<ArbeidsavklaringspengerUT>(yk)
    var fasteUtgifterTilleggUT: FasteUtgifterTilleggUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<FasteUtgifterTilleggUT>(yk)
    var garantitillegg_Art_27_UT: Garantitillegg_Art_27_UT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Garantitillegg_Art_27_UT>(yk)
    var skattefriGrunnpensjon: SkattefriGrunnpensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<SkattefriGrunnpensjon>(yk)
    var skattefriUforetrygdOrdiner: SkattefriUforetrygdOrdiner?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<SkattefriUforetrygdOrdiner>(yk)
    var sykepenger: Sykepenger?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<Sykepenger>(yk)
    var sykepengerUT: SykepengerUT?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<SykepengerUT>(yk)
    var uforetilleggTilAlderspensjon: UforetilleggTilAlderspensjon?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<UforetilleggTilAlderspensjon>(yk)
    var gjenlevendetilleggAP: GjenlevendetilleggAP?
        get() = ytelseskomponenter.firstIsInstanceOrNull()
        set(yk) = internalAddOrRemoveIfNull<GjenlevendetilleggAP>(yk)

    constructor() : super() {
        formelKode = FormelKodeCti("BPUx")
    }

    // SIMDOM-ADD excludeBrutto
    // since no.nav.domain.pensjon.kjerne.beregning2011.PensjonUnderUtbetaling excludes brutto
    constructor(pub: PensjonUnderUtbetaling, excludeBrutto: Boolean = false) : super() {
        totalbelopNetto = pub.totalbelopNetto
        totalbelopNettoAr = pub.totalbelopNettoAr

        if (!excludeBrutto) {
            totalbelopBrutto = pub.totalbelopBrutto
            totalbelopBruttoAr = pub.totalbelopBruttoAr
        }

        if (pub.formelKode != null) {
            formelKode = FormelKodeCti(pub.formelKode!!)
        }
        pubReguleringFratrekk = pub.pubReguleringFratrekk

        for (yk in pub.internalYtelseskomponenter) {
            val clazz = yk.javaClass
            val constructor = clazz.getConstructor(clazz)
            addYtelseskomponent(constructor.newInstance(yk))
        }
    }

    override fun toString() =
        "PensjonUnderUtbetaling(formelKode=$formelKode, totalbelopNetto=$totalbelopNetto, totalbelopNettoAr=$totalbelopNettoAr)"
}
