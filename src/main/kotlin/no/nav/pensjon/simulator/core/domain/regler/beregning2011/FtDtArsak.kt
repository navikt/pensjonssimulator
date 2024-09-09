package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FtDtArsakCti

/**
 * @author Lars Hartvigsen (Decisive), PK-9169
 * @author Magnus Bakken (Accenture), PK-9169
 */
class FtDtArsak {
    /**
     * Angir årsaker til at delingstall eller forholdstall er benyttet i beregninger
     */
    var ftDtArsak: FtDtArsakCti? = null

    constructor() : super() {}

    constructor(arsak: FtDtArsak) {
        if (arsak.ftDtArsak != null) {
            ftDtArsak = FtDtArsakCti(arsak.ftDtArsak)
        }
    }

    constructor(ftDtArsak: FtDtArsakCti? = null) {
        this.ftDtArsak = ftDtArsak
    }

}
