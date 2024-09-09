package no.nav.pensjon.simulator.core.result

// no.nav.domain.pensjon.kjerne.kodetabeller.UforeTypeCode
enum class UfoereType {
    /**
     * Uføre
     */
    UFORE,

    /**
     * Uføre m/ yrkesskade
     */
    UF_M_YRKE,

    /**
     * Første virkningsdato, ikke ufør
     */
    VIRK_IKKE_UFOR,

    /**
     * Yrkesskade
     */
    YRKE
}
