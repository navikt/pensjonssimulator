package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.ServiceberegningAfpResult
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.fpp.FppSimuleringResult
import org.springframework.stereotype.Service

@Service
class TidsbegrensetAfpSimuleringFacade(
    private val specCreator: TidsbegrensetAfpSpecCreator,
    private val simulator: TidsbegrensetAfpSimuleringService
) {
    //TODO bruk denne for FPP-simulering
    fun simulerAfpForFpp(spec: TidsbegrensetAfpSpec): FppSimuleringResult =
        fppResult(simulator.simulerPensjonsberegning(simuleringSpec(spec)))

    fun simulerAfpForServiceberegning(spec: TidsbegrensetAfpSpec): ServiceberegningAfpResult =
        simulerAfpForServiceberegning(simuleringSpec(spec))

    private fun simulerAfpForServiceberegning(spec: Simulering): ServiceberegningAfpResult =
        serviceberegningResult(simulator.simulerPensjonsberegning(spec))

    private fun simuleringSpec(spec: TidsbegrensetAfpSpec): Simulering =
        specCreator.createSpec(
            uttakFom = spec.uttakFom,
            personinfo = spec.personopplysninger,
            opptjeningListe = spec.opptjeningListe,
            utenlandsoppholdListe = spec.personopplysninger.utenlandsoppholdListe
        )

    private companion object {
        private fun fppResult(result: TidsbegrensetAfpSimuleringResult) =
            FppSimuleringResult(
                afpOrdning = result.afpOrdning,
                beregnetAfp = result.beregnetAfp,
                problem = result.problem
            )

        private fun serviceberegningResult(result: TidsbegrensetAfpSimuleringResult) =
            ServiceberegningAfpResult(
                beregnetAfp = result.beregnetAfp,
                opptjeningListe = result.opptjeningListe,
                problem = result.problem
            )
    }
}