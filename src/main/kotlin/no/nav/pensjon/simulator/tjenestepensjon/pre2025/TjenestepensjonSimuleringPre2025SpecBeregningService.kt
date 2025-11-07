package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.TjenestepensjonSimuleringPre2025SpecAggregator.aggregateSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TjenestepensjonSimuleringPre2025Spec
import org.springframework.stereotype.Service

@Service
class TjenestepensjonSimuleringPre2025SpecBeregningService(
    private val simulator: SimulatorCore,
    private val generelleDataHolder: GenerelleDataHolder
) {
    fun kompletterMedAlderspensjonsberegning(
        simuleringSpec: SimuleringSpec,
        stillingsprosentSpec: StillingsprosentSpec
    ): TjenestepensjonSimuleringPre2025Spec {
        val simuleringResultat = simulator.simuler(simuleringSpec)

        return aggregateSpec(
            simuleringResultat,
            simuleringSpec,
            stillingsprosentSpec,
            sisteGyldigeOpptjeningsaar = generelleDataHolder.getSisteGyldigeOpptjeningsaar()
        )
    }
}