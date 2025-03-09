package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.EksportUnntakEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.EksportUnntakCti

// Checked 2025-02-28
class Eksportforbud {
    /**
     * Angir om personen har eksportforbud eller ikke.
     */
    var forbud = false

    /**
     * Angir type eksportunntak.
     */
    var unntakType: EksportUnntakCti? = null
    var unntakTypeEnum: EksportUnntakEnum? = null

    constructor()

    constructor(source: Eksportforbud) : this() {
        forbud = source.forbud
        unntakType = source.unntakType?.let(::EksportUnntakCti)
        unntakTypeEnum = source.unntakTypeEnum
    }

    override fun toString(): String =
        StringBuilder().append("Eksportforbud ( ").append(super.toString()).append("    ").append("forbud = ")
            .append(forbud)
            .append("    ").append("unntakType = ").append(unntakType)
            .append("    ").append(" )").toString()
}
