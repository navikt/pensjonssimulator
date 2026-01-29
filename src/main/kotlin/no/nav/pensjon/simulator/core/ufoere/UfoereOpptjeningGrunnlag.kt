package no.nav.pensjon.simulator.core.ufoere

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT

class UfoereOpptjeningGrunnlag {
    var maksUtbetalingsgradPerArUTListe: MutableList<UtbetalingsgradUT>? = null

    fun copy() =
        UfoereOpptjeningGrunnlag().also {
            it.maksUtbetalingsgradPerArUTListe = maksUtbetalingsgradPerArUTListe?.toMutableList()
        }
}