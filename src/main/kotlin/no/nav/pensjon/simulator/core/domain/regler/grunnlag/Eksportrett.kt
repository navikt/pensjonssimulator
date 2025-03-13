package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.EksportlandEnum

// Checked 2025-02-28
class Eksportrett {
    /**
     * Angir om personen har eksportrett eller ikke.
     */
    var eksportrett = false

    /**
     * Angir land personen bor i.
     */
    var bostedslandEnum: EksportlandEnum? = null

    constructor()

    constructor(source: Eksportrett) : this() {
        eksportrett = source.eksportrett
        bostedslandEnum = source.bostedslandEnum
    }
}
