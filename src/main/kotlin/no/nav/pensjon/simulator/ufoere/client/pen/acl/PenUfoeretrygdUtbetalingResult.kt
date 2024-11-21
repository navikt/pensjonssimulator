package no.nav.pensjon.simulator.ufoere.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT

/**
 * Corresponds with UtbetalingsgradResultV1 in PEN
 */
data class PenUfoeretrygdUtbetalingResult(
    val utbetalingsgradListe: List<UtbetalingsgradUT>
)
