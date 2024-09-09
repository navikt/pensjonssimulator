package no.nav.pensjon.simulator.core.krav

import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti

object KravUtil {

    fun kravlinjeType(type: KravlinjeTypePlus) =
        KravlinjeTypeCti(type.name).apply { hovedKravlinje = type.erHovedkravlinje }
}
