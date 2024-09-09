package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class YrkeYrkesskadeCti : TypeCti, Serializable {
    constructor() : super("")

    /**
     * Koder for yrker brukt ved uførepensjon med yrkesskade.
     * Arkfane: k_yrke_yrkesskad
     * pr september 2007 ser tabellen slik ut:
     * EL Elev
     * EL_UTEN_YRK Elev uten yrkesutdanning
     * FISK Fiskere, fagnstmenn
     * MIL Militæreperson
     * UNG Ungdom
     * ARB Vanlig arbeidstagere
     * SELV_NAER Selvstendig næringsdrivende
     */

    constructor(kode: String) : super(kode)
    constructor(yrkeYrkesskadeCti: YrkeYrkesskadeCti?) : super(yrkeYrkesskadeCti!!)
}
