package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import java.time.LocalDate

// 2026-04-23
class ForsteVirkningsdatoGrunnlag {
    var virkningsdatoLd: LocalDate? = null
    var kravFremsattDatoLd: LocalDate? = null
    var bruker: PenPerson? = null
    var annenPerson: PenPerson? = null
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null

    //--- Extra:
    var sakType: SakTypeEnum? = null

    constructor()

    constructor(source: ForsteVirkningsdatoGrunnlag) {
        virkningsdatoLd = source.virkningsdatoLd
        kravFremsattDatoLd = source.kravFremsattDatoLd
        bruker = source.bruker
        annenPerson = source.annenPerson
        kravlinjeTypeEnum = source.kravlinjeTypeEnum
        sakType = source.sakType
    }
}
