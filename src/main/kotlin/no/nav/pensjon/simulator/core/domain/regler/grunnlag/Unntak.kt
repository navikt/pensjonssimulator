package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.EksportUnntakEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.InngangUnntakEnum

// Checked 2025-02-28
class Unntak {
    /**
     * Angir om personen har unntak eller ikke.
     */
    var unntak = false

    /**
     * Angir type unntak.
     */
    var unntakTypeEnum: InngangUnntakEnum? = null

    /**
     * Unntak fra eksportforbud.
     */
    var eksportUnntakEnum: EksportUnntakEnum? = null

    constructor()

    constructor(source: Unntak) : this() {
        unntak = source.unntak
        unntakTypeEnum = source.unntakTypeEnum
        eksportUnntakEnum = source.eksportUnntakEnum
    }
}
