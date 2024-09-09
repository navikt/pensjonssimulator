package no.nav.pensjon.simulator.core.domain.regler.krav

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti
import java.io.Serializable
import java.util.*

/**
 * En kravlinje er en del av et kravhode.
 */
class Kravlinje : Serializable {

    @JsonIgnore
    var kravlinjeStatus: KravlinjeStatus? = null
    @JsonIgnore
    var land: Land? = null

    /**
     * Hvilken type kravlinjen gjelder, spesifisert som VilkarsvedtakType.
     */
    var kravlinjeType: KravlinjeTypeCti? = null

    /**
     * Personen kravet relaterer seg til.
     */
    var relatertPerson: PenPerson? = null

    /**
     * Copy Constructor
     */
    constructor(kravlinje: Kravlinje) {
        if (kravlinje.kravlinjeType != null) {
            kravlinjeType = KravlinjeTypeCti(kravlinje.kravlinjeType!!)
        }
        if (kravlinje.relatertPerson != null) {
            relatertPerson = PenPerson(kravlinje.relatertPerson!!)
        }
        // SIMDOM-ADD:
        kravlinjeStatus = kravlinje.kravlinjeStatus
        land = kravlinje.land
        // end SIMDOM-ADD
    }

    constructor(kravlinjeType: KravlinjeTypeCti? = null, relatertPerson: PenPerson? = null) : super() {
        if (kravlinjeType != null) {
            this.kravlinjeType = KravlinjeTypeCti(kravlinjeType)
        }
        this.relatertPerson = relatertPerson
    }

    constructor() : super()

    override fun toString(): String {
        val result = StringBuilder()
        val newLine = System.getProperty("line.separator")
        result.append(this.javaClass.name)
        result.append(" Object {")
        result.append(newLine)

        // determine fields declared in this class only (no fields of
        // superclass)
        val fields = this.javaClass.declaredFields

        // print field names paired with their values
        for (field in fields) {
            val fieldName = field.name
            if (fieldName.compareTo("serialVersionUID") != 0) {
                result.append("  ")
                result.append(fieldName)
                result.append(": ")
                try {
                    result.append(field[this])
                } catch (ex: IllegalAccessException) {
                    println(ex)
                }
                result.append(newLine)
            }
        }
        result.append("}")
        return result.toString()
    }

    // SIMDOM-ADD:

    fun distinguisher(): String =
        (kravlinjeStatus?.name ?: "?") +
                (kravlinjeType?.kode ?: "?") +
                (land?.name ?: "?") +
                (relatertPerson?.penPersonId ?: "?")

    fun erHovedkravlinje(): Boolean = kravlinjeType?.let { it.er_gyldig && it.hovedKravlinje } ?: false

    fun isKravlinjeAvbrutt(): Boolean =
        EnumSet.of(
            KravlinjeStatus.HENLAGT,
            KravlinjeStatus.FEILREGISTRERT,
            KravlinjeStatus.TRUKKET
        ).contains(kravlinjeStatus)
}
