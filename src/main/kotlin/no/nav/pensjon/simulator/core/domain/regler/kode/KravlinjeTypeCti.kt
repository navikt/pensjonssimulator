package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class KravlinjeTypeCti : TypeCti, Serializable {
    constructor() : super("")

    /**
     * Denne er true dersom kravet er en hovedytelse (AP, UP osv).Default false.
     */
    var hovedKravlinje = false

    constructor(kravlinjeTypeCti: KravlinjeTypeCti) : super(kravlinjeTypeCti) {
        hovedKravlinje = kravlinjeTypeCti.hovedKravlinje
    }

    constructor(kode: String, hovedKravlinje: Boolean = false) : super(kode) {
        this.hovedKravlinje = hovedKravlinje
    }
}
