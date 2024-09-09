package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.EksportUnntakCti

class Eksportforbud(

    /**
     * Angir om personen har eksportforbud eller ikke.
     */
    var forbud: Boolean = false,

    /**
     * Angir type eksportunntak.
     */
    var unntakType: EksportUnntakCti? = null
) {
    constructor(eksportforbud: Eksportforbud) : this() {
        this.forbud = eksportforbud.forbud
        if (eksportforbud.unntakType != null) {
            this.unntakType = EksportUnntakCti(eksportforbud.unntakType)
        }
    }

    override fun toString(): String {
        val TAB = "    "

        val retValue = StringBuilder()

        retValue.append("Eksportforbud ( ").append(super.toString()).append(TAB).append("forbud = ").append(forbud)
            .append(TAB).append("unntakType = ").append(unntakType)
            .append(TAB).append(" )")

        return retValue.toString()
    }
}
