package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable
import java.util.*

abstract class TypeCti : Serializable {
    open var kode: String = ""
    var dekode: String? = null
    var dato_fom: Date? = null
    var dato_tom: Date? = null
    var er_gyldig: Boolean = false
    var kommentar: String? = null

    constructor(typeCti: TypeCti) {
        this.kode = typeCti.kode
        this.dekode = typeCti.dekode
        this.dato_fom = typeCti.dato_fom?.clone() as Date?
        this.dato_tom = typeCti.dato_tom?.clone() as Date?
        this.er_gyldig = typeCti.er_gyldig
        this.kommentar = typeCti.kommentar
    }

    constructor(kode: String) {
        this.kode = kode
        er_gyldig = true
    }

    fun among(vararg enumNames: String): Boolean = enumNames.any { it == this.kode }


    fun notAmong(vararg enums: String): Boolean {
        return !among(*enums)
    }

    override fun hashCode(): Int {
        var result = kode.hashCode()
        result = 31 * result + (dekode?.hashCode() ?: 0)
        result = 31 * result + (dato_fom?.hashCode() ?: 0)
        result = 31 * result + (dato_tom?.hashCode() ?: 0)
        result = 31 * result + er_gyldig.hashCode()
        result = 31 * result + (kommentar?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Enum<*>) return this.kode.equals(other.name)
        if (other is TypeCti) return this.kode == other.kode
        if (javaClass != other?.javaClass) return false

        return super.equals(other)
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append(this.javaClass.simpleName)
        result.append(" : $kode")
        return result.toString()
    }
}

fun TypeCti?.betterAmong(vararg enums: Enum<*>) = this?.let { type -> enums.any { it.name == type.kode } } ?: false

fun TypeCti?.betterNotAmong(vararg enums: Enum<*>) = this?.let { type -> enums.none { it.name == type.kode } } ?: false

/**
 * Returns `true` if enum T contains an entry with the specified name.
 */
inline fun <reified T : Enum<T>> enumContains(name: String): Boolean {
    return enumValues<T>().any { it.name == name }
}
/* SIMDOM-MOD
fun TypeCti?.valid(): Boolean {
    return this?.let {
        val enumValueAsString = this.kode
        return when (this) {
            is AfpOrdningTypeCti -> enumContains<AFPtypeEnum>(enumValueAsString)
            is AvtaleDatoCti -> enumContains<AvtaleDatoEnum>(enumValueAsString)
            is AvtaleTypeCti -> enumContains<AvtaletypeEnum>(enumValueAsString)
            is AvtalelandCti -> enumContains<AvtaleLandEnum>(enumValueAsString)
            is BarnepensjonEOSKapittelCti -> enumContains<BarnepensjonEosKapEnum>(enumValueAsString)
            is BeslutningsstotteTypeCti -> enumContains<BeslutningsstotteTypeEnum>(enumValueAsString)
            is BorMedTypeCti -> enumContains<BorMedTypeEnum>(enumValueAsString)
            is EksportUnntakCti -> enumContains<EksportUnntakEnum>(enumValueAsString)
            is EksportlandCti -> enumContains<LandkodeEnum>(enumValueAsString)
            is FormelKodeCti -> enumContains<FormelKodeEnum>(enumValueAsString)
            is ForstegangstjenesteperiodeTypeCti -> enumContains<ForstegangstjenestetypeEnum>(enumValueAsString)
            is FppGarantiKodeCti -> enumContains<FppGarantiKodeEnum>(enumValueAsString)
            is Fravik_19_3Cti -> enumContains<Fravik_19_3_Enum>(enumValueAsString)
            is GrunnlagKildeCti -> enumContains<GrunnlagkildeEnum>(enumValueAsString)
            is GrunnlagsrolleCti -> enumContains<GrunnlagsrolleEnum>(enumValueAsString)
            is InngangUnntakCti -> enumContains<InngangUnntakEnum>(enumValueAsString)
            is InntektTypeCti -> enumContains<InntekttypeEnum>(enumValueAsString)
            is JustertPeriodeCti -> enumContains<JustertPeriodeEnum>(enumValueAsString)
            is KravVelgTypeCti -> enumContains<KravVelgtypeEnum>(enumValueAsString)
            is KravlinjeTypeCti -> enumContains<YtelsetypeEnum>(enumValueAsString)
            is LandCti -> enumContains<LandkodeEnum>(enumValueAsString)
            is OpptjeningTypeCti -> enumContains<OpptjeningtypeEnum>(enumValueAsString)
            is ProRataBeregningTypeCti -> enumContains<ProRataBeregningTypeEnum>(enumValueAsString)
            is RegelverkTypeCti -> enumContains<RegelverkTypeEnum>(enumValueAsString)
            is SatsTypeCti -> enumContains<SatsTypeEnum>(enumValueAsString)
            is SivilstandTypeCti -> enumContains<SivilstandEnum>(enumValueAsString)
            is UforeTypeCti -> enumContains<UforetypeEnum>(enumValueAsString)
            is VilkarsvedtakResultatCti -> enumContains<VedtakResultatEnum>(enumValueAsString)
            is YrkeCti -> enumContains<YrkeEnum>(enumValueAsString)
            is YrkeYrkesskadeCti -> enumContains<YrkeYrkesskadeEnum>(enumValueAsString)
            is YtelsekomponentTypeCti -> enumContains<YtelseskomponentTypeEnum>(enumValueAsString)
            else -> false
        }
    } ?: false
}*/

/* SIMDOM-MOD
fun main() {
    val cti = AP.cti()
    val bool = cti.betterAmong(UP, AFP, UT)
    println(bool)
}
*/
