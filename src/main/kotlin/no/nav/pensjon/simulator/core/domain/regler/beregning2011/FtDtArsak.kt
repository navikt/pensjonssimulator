package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.ArsakFtDtEnum

class FtDtArsak {
    /**
     * Angir årsaker til at delingstall eller forholdstall er benyttet i beregninger
     */
    var ftDtArsakEnum: ArsakFtDtEnum? = null

    constructor() : super() {}

    constructor(source: FtDtArsak) {
        ftDtArsakEnum = source.ftDtArsakEnum
    }
}
