package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import java.util.*

// 2025-03-10
class ForsteVirkningsdatoGrunnlag {

    var virkningsdato: Date? = null
    var kravFremsattDato: Date? = null
    var bruker: PenPerson? = null
    var annenPerson: PenPerson? = null
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null

    //--- Extra:
    var sakType: SakTypeEnum? = null

    constructor()

    constructor(source: ForsteVirkningsdatoGrunnlag) {
        virkningsdato = source.virkningsdato
        kravFremsattDato = source.kravFremsattDato
        bruker = source.bruker
        annenPerson = source.annenPerson
        kravlinjeTypeEnum = source.kravlinjeTypeEnum
        sakType = source.sakType
    }
}
