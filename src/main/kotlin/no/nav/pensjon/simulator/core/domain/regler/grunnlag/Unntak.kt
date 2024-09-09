package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.EksportUnntakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InngangUnntakCti
import java.io.Serializable

class Unntak(
    /**
     * Angir om personen har unntak eller ikke.
     */
    var unntak: Boolean = false,

    /**
     * Angir type unntak.
     */
    var unntakType: InngangUnntakCti? = null,

    /**
     * Unntak fra eksportforbud.
     */
    var eksportUnntak: EksportUnntakCti? = null
) : Serializable {

    constructor(unntak: Unntak) : this() {
        this.unntak = unntak.unntak
        if (unntak.unntakType != null) {
            this.unntakType = unntak.unntakType
        }
        if (unntak.eksportUnntak != null) {
            this.eksportUnntak = unntak.eksportUnntak
        }
    }

    constructor(unntak: Boolean, unntakType: InngangUnntakCti?) : this() {
        this.unntak = unntak
        if (unntakType != null) {
            this.unntakType = unntakType
        }
    }
}
