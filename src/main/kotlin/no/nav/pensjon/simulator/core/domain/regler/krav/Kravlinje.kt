package no.nav.pensjon.simulator.core.domain.regler.krav

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import java.io.Serializable
import java.util.*

/**
 * En Kravlinje er en del av et KravHode. Eksempler på Kravlinje er GP, ET, UP osv.
 */
open class Kravlinje : Serializable {
    /**
     * Hvilken type kravlinjen gjelder, spesifisert som VilkarsvedtakType.
     */
    var kravlinjeType: KravlinjeTypeCti? = null //TODO remove
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null
        get() {
            return field ?: kravlinjeType?.let { KravlinjeTypeEnum.valueOf(it.kode) }
        }
        set(value) {
            field = value
            kravlinjeType = value?.let { KravlinjeTypeCti(it.name).apply { hovedKravlinje = it.erHovedkravlinje } }
        }

    /**
     * Personen kravet relaterer seg til.
     */
    var relatertPerson: PenPerson? = null

    constructor()

    constructor(
        kravlinjeTypeEnum: KravlinjeTypeEnum,
        relatertPerson: PenPerson?
    ) {
        this.kravlinjeTypeEnum = kravlinjeTypeEnum
        this.kravlinjeType = KravlinjeTypeCti(kravlinjeTypeEnum.name).apply { hovedKravlinje = kravlinjeTypeEnum.erHovedkravlinje }
        this.relatertPerson = relatertPerson
    }

    // SIMDOM-ADD:

    @JsonIgnore
    var kravlinjeStatus: KravlinjeStatus? = null

    @JsonIgnore
    var land: LandkodeEnum? = null

    constructor(kravlinje: Kravlinje) {
        kravlinje.kravlinjeTypeEnum?.let { kravlinjeTypeEnum = it }
        kravlinje.kravlinjeType?.let { kravlinjeType = it }
        kravlinje.relatertPerson?.let { relatertPerson = PenPerson(it) }

        // SIMDOM-ADD:
        kravlinjeStatus = kravlinje.kravlinjeStatus
        land = kravlinje.land
        // end SIMDOM-ADD
    }

    fun distinguisher(): String =
        (kravlinjeStatus?.name ?: "?") +
                (kravlinjeTypeEnum ?: "?") +
                (land?.name ?: "?") +
                (relatertPerson?.penPersonId ?: "?")

    fun erHovedkravlinje(): Boolean = kravlinjeTypeEnum?.erHovedkravlinje == true

    fun isKravlinjeAvbrutt(): Boolean =
        EnumSet.of(
            KravlinjeStatus.HENLAGT,
            KravlinjeStatus.FEILREGISTRERT,
            KravlinjeStatus.TRUKKET
        ).contains(kravlinjeStatus)
}
