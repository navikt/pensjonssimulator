package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.EksportUnntakEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.InngangUnntakEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.EksportUnntakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InngangUnntakCti

// Checked 2025-02-28
class Unntak {
    /**
     * Angir om personen har unntak eller ikke.
     */
    var unntak = false

    /**
     * Angir type unntak.
     */
    var unntakType: InngangUnntakCti? = null
    var unntakTypeEnum: InngangUnntakEnum? = null

    /**
     * Unntak fra eksportforbud.
     */
    var eksportUnntak: EksportUnntakCti? = null
    var eksportUnntakEnum: EksportUnntakEnum? = null

    constructor()

    constructor(source: Unntak) : this() {
        unntak = source.unntak
        unntakType = source.unntakType?.let(::InngangUnntakCti)
        unntakTypeEnum = source.unntakTypeEnum
        eksportUnntak = source.eksportUnntak?.let(::EksportUnntakCti)
        eksportUnntakEnum = source.eksportUnntakEnum
    }
}
