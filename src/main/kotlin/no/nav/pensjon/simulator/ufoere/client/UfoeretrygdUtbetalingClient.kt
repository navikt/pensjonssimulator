package no.nav.pensjon.simulator.ufoere.client

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT

interface UfoeretrygdUtbetalingClient {
    fun fetchUtbetalingsgradListe(penPersonId: Long): List<UtbetalingsgradUT>
}
