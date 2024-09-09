package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.util.*

/**
 * Denne klassen inneholder informasjon om TP-ordningers uførepensjonsgrunnlag. Dette er manuelt registrerte data og ikke hentet fra TP-registeret eller andre eksterne kilder.
 */
class AfpTpoUpGrunnlag(
    var belop: Int = 0,

    var virkFom: Date? = null
) {

    constructor(AfpTpoUpGrunnlag: AfpTpoUpGrunnlag) : this() {
        this.belop = AfpTpoUpGrunnlag.belop
        this.virkFom = AfpTpoUpGrunnlag.virkFom
    }
}
