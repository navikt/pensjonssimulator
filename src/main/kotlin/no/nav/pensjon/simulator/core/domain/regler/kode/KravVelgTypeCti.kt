package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class KravVelgTypeCti : TypeCti, Serializable {
    constructor() : super("")

    /**
     * Kode som angir detaljering av kravet.
     * Se K_KRAV_VELG_T
     */
    var hovedKravlinje = false

    constructor(kravVelgTypeCti: KravVelgTypeCti) : super(kravVelgTypeCti) {
        hovedKravlinje = kravVelgTypeCti.hovedKravlinje
    }

    constructor(kode: String) : super(kode)
}
