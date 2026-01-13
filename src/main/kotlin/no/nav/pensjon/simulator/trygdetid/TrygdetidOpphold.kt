package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagFactory.trygdetidPeriode
import java.util.Date

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.TrygdetidsgrunnlagWithArbeid
data class TrygdetidOpphold(
    val periode: TTPeriode,
    val arbeidet: Boolean
) {
    fun withPeriodeTom(dato: Date?) =
        TrygdetidOpphold(
            periode = trygdetidPeriode(
                fom = periode.fom,
                tom = dato,
                land = periode.landEnum
            ),
            arbeidet
        )
}
