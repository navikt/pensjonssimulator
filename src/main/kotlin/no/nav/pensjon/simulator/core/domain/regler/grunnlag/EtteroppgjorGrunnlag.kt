package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.PensjonUnderUtbetaling

class EtteroppgjorGrunnlag(

        /**
         * Ytterligere informasjon om etteroppgjør
         */
        var uforetrygdEtteroppgjor: UforetrygdEtteroppgjor? = null,

        var pensjonUnderUtbetalingForRevurdering: PensjonUnderUtbetaling? = null
) {
    constructor(etteroppgjorGrunnlag: EtteroppgjorGrunnlag) : this() {

        if (etteroppgjorGrunnlag.uforetrygdEtteroppgjor != null) {
            this.uforetrygdEtteroppgjor = UforetrygdEtteroppgjor(etteroppgjorGrunnlag.uforetrygdEtteroppgjor!!)
        }

        if (etteroppgjorGrunnlag.pensjonUnderUtbetalingForRevurdering != null) {
            this.pensjonUnderUtbetalingForRevurdering = PensjonUnderUtbetaling(etteroppgjorGrunnlag.pensjonUnderUtbetalingForRevurdering!!)
        }
    }
}
