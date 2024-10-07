package no.nav.pensjon.simulator.core.domain.regler.krav

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
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
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null

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
        this.relatertPerson = relatertPerson
    }

    // SIMDOM-ADD:

    @JsonIgnore
    var kravlinjeStatus: KravlinjeStatus? = null

    @JsonIgnore
    var land: Land? = null

    constructor(kravlinje: Kravlinje) {
        if (kravlinje.kravlinjeTypeEnum != null) {
            kravlinjeTypeEnum = kravlinje.kravlinjeTypeEnum
        }

        if (kravlinje.relatertPerson != null) {
            relatertPerson = PenPerson(kravlinje.relatertPerson!!)
        }

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
