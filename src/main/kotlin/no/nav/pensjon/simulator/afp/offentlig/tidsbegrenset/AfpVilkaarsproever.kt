package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.domain.regler.to.SimuleringRequest
import org.springframework.stereotype.Service

/**
 * Vilkårsprøver tidsbegrenset avtalefestet pensjon i offentlig sektor.
 */
@Service
class AfpVilkaarsproever(private val simulator: SimulatorContext) {

    // PEN: DefaultSimuleringConsumerService.simulerVilkarsprovAfp
    //   -> SimulerVilkarsprovAfpConsumerCommand.execute
    fun vilkaarsproevTidsbegrensetOffentligAfp(spec: Simulering): Simuleringsresultat =
        simulator.simulerVilkarsprovTidsbegrensetOffentligAfp(
            spec = SimuleringRequest(simulering = spec, fom = spec.uttaksdatoLd)
        )
}