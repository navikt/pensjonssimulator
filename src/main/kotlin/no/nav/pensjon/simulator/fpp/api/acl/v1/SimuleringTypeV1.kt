package no.nav.pensjon.simulator.fpp.api.acl.v1

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum

/**
 * Anti-corruption.
 * Versjonert utgave av simuleringstyper for bruk i API-et (isolerer API-et fra den interne reprsentasjonen i domenet).
 */
enum class SimuleringTypeV1(val internalValue: SimuleringTypeEnum) {
    /**
     * Tidsbegrenset AFP i offentlig sektor
     */
    AFP(internalValue = SimuleringTypeEnum.AFP),

    /**
     * Framtidige pensjonspoeng
     */
    AFP_FPP(internalValue = SimuleringTypeEnum.AFP_FPP),

    /**
     * Alderspensjon
     */
    ALDER(internalValue = SimuleringTypeEnum.ALDER),

    /**
     * Alderspensjon med gjenlevenderettigheter
     */
    ALDER_M_GJEN(internalValue = SimuleringTypeEnum.ALDER_M_GJEN),

    /**
     * Barnepensjon
     */
    BARN(internalValue = SimuleringTypeEnum.BARN),

    /**
     * Gjenlevendepensjon
     */
    GJENLEVENDE(internalValue = SimuleringTypeEnum.GJENLEVENDE)
}
