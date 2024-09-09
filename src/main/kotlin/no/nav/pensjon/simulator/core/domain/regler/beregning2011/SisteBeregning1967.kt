package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Grunnpensjon
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ResultatTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import java.util.*

/**
 * Håndterer "gammel" beregning
 */
class SisteBeregning1967 : SisteBeregning {
    var gp: Grunnpensjon? = null
    var gpKapittel3: Grunnpensjon? = null

    /**
     * CR????? - Nytt felt for å håndtere uavkortet, men pensjonsregulert grunnpensjon i de tilfeller
     * pensjonen er avkortet til 70% av tpi ved revurdering av AFP
     */
    var gpAfpPensjonsregulert: Grunnpensjon? = null

    constructor() : super() {}

    constructor(sb: SisteBeregning1967) : super(sb) {
        if (sb.gp != null) {
            gp = Grunnpensjon(sb.gp!!)
        }
        if (sb.gpKapittel3 != null) {
            gpKapittel3 = Grunnpensjon(sb.gpKapittel3!!)
        }
        if (sb.gpAfpPensjonsregulert != null) {
            gpAfpPensjonsregulert = Grunnpensjon(sb.gpAfpPensjonsregulert!!)
        }
    }

    constructor(
            gp: Grunnpensjon? = null,
            gpKapittel3: Grunnpensjon? = null,
            gpAfpPensjonsregulert: Grunnpensjon? = null,
            /** super SisteBeregning */
            virkDato: Date? = null,
            tt_anv: Int = 0,
            resultatType: ResultatTypeCti? = null,
            sivilstandType: SivilstandTypeCti? = null,
            benyttetSivilstand: BorMedTypeCti? = null
    ) : super(
            virkDato = virkDato,
            tt_anv = tt_anv,
            resultatType = resultatType,
            sivilstandType = sivilstandType,
            benyttetSivilstand = benyttetSivilstand
    ) {
        this.gp = gp
        this.gpKapittel3 = gpKapittel3
        this.gpAfpPensjonsregulert = gpAfpPensjonsregulert
    }

}
