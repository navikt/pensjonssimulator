package no.nav.pensjon.simulator.core.domain.regler.enum

// Aligned with pensjon-regler-api 2025-11-11
enum class KravVelgtypeEnum  {
    AVDOD_MOR,
    AVDOD_FAR,
    FORELDRELOS,
    FORELOPIG,
    VARIG,
    BP,
    EP,

    /**
     * Tatt fra PEN. Kan de fjernes, eller vil vi ta i mot disse?
     */
    MIL_BARNEP,
    MIL_GJENLEV,
    MIL_INV,
    NSB,
    SIVIL_BARNEP,
    SIVIL_GJENLEV,
    SIVIL_INV,
    UP
}
