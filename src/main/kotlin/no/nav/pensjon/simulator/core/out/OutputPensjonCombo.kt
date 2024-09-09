package no.nav.pensjon.simulator.core.out

import no.nav.pensjon.simulator.core.anonym.AnonymSimuleringResult
import no.nav.pensjon.simulator.core.legacy.OutputLegacyPensjon

/**
 * Holds simuleringsresultat "combo", i.e. results of either:
 * - "legacy" simulering (with PEN domain classes)
 * - "refactored" simulering with pensjon-regler domain classes (for users born 1963 or later)
 * - "forenklet" (uinnlogget/anonym) simulering
 */
data class OutputPensjonCombo(
    val legacyPensjon: OutputLegacyPensjon? = null,
    val pensjon: OutputPensjon? = null,
    val anonymSimuleringResult: AnonymSimuleringResult? = null
)
