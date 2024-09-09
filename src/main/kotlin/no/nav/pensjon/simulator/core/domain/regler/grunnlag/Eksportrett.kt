package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.EksportlandCti
import java.io.Serializable

class Eksportrett(
    /**
     * Angir land personen bor i.
     */
    var bostedsland: EksportlandCti? = null,
    /**
     * Angir om personen har eksportrett eller ikke.
     */
    var eksportrett: Boolean = false
) : Serializable {
    constructor(eksportrett: Eksportrett) : this() {
        this.eksportrett = eksportrett.eksportrett
        if (eksportrett.bostedsland != null) {
            this.bostedsland = EksportlandCti(eksportrett.bostedsland)
        }
    }
}
