package no.nav.pensjon.simulator.vedtak

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus

/**
 * Del av et krav, relatert til et vilkårsvedtak.
 * Denne klassen brukes kun internt i simulatordomenet, og ikke til eksterne kall.
 * Klassen ble innført som erstatning for det utfasede kravlinje-feltet i VilkarsVedtak.
 */
data class VilkaarsvedtakKravlinje(
    val type: KravlinjeTypeEnum,
    val person: PenPerson?,
    val status: KravlinjeStatus? = null,
    val land: LandkodeEnum? = null
) {
    constructor(source: Kravlinje) : this(
        type = source.kravlinjeTypeEnum!!,
        person = source.relatertPerson?.copy(),
        status = source.kravlinjeStatus,
        land = source.land
    )

    fun with(status: KravlinjeStatus?, land: LandkodeEnum?) =
        copy(status = status, land = land)
}