package no.nav.pensjon.simulator.core.domain.regler.krav

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import java.util.*

/**
 * En Kravlinje er en del av et KravHode. Eksempler på Kravlinje er GP, ET, UP
 * osv.
 */
open class Kravlinje {
    /**
     * Hvilken type kravlinjen gjelder, spesifisert som VilkarsvedtakType.
     */
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null

    /**
     * Personen kravet relaterer seg til.
     */
    var relatertPerson: PenPerson? = null

    /**
     * Angir om det er hovedKravlinje
     * Erstatter hovedKravlinje på KravlinjeTypeCti
     */
    var hovedKravlinje: Boolean = false

    constructor()
    /*
    constructor(
        relatertPerson: PenPerson?,
        hovedKravlinje: Boolean = false
    ) {
        this.relatertPerson = relatertPerson
        this.hovedKravlinje = hovedKravlinje
    }

    constructor(
        kravlinjeTypeEnum: KravlinjeTypeEnum,
        relatertPerson: PenPerson?,
        hovedKravlinje: Boolean = false
    ) {
        this.relatertPerson = relatertPerson
        this.kravlinjeTypeEnum = kravlinjeTypeEnum
        this.hovedKravlinje = hovedKravlinje
    }*/

    // SIMDOM-ADD:

    @JsonIgnore
    var kravlinjeStatus: KravlinjeStatus? = null

    @JsonIgnore
    var land: LandkodeEnum? = null

    constructor(source: Kravlinje) {
        kravlinjeTypeEnum = source.kravlinjeTypeEnum
        hovedKravlinje = source.kravlinjeTypeEnum?.erHovedkravlinje == true
        relatertPerson = source.relatertPerson?.let(::PenPerson)
        hovedKravlinje = source.hovedKravlinje

        // SIMDOM-ADD:
        kravlinjeStatus = source.kravlinjeStatus
        land = source.land
        // end SIMDOM-ADD
    }

    fun distinguisher(): String =
        (kravlinjeStatus?.name ?: "s?") +
                (kravlinjeTypeEnum ?: "t?") +
                hovedKravlinje +
                (land?.name ?: "l?") +
                (relatertPerson?.penPersonId ?: "p?")

    fun erHovedkravlinje(): Boolean = kravlinjeTypeEnum?.erHovedkravlinje == true

    fun isKravlinjeAvbrutt(): Boolean =
        EnumSet.of(
            KravlinjeStatus.HENLAGT,
            KravlinjeStatus.FEILREGISTRERT,
            KravlinjeStatus.TRUKKET
        ).contains(kravlinjeStatus)
}
