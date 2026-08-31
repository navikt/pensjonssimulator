package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetAfpSpecCreator
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import org.springframework.stereotype.Service

@Service
class FppSimuleringFacade(
    private val specCreator: TidsbegrensetAfpSpecCreator,
    private val simulator: FppSimuleringService
) {
    // PEN: PensjonskalkulatorController.lagreFpp
    //   -> PensjonskalkulatorController.simulerPensjon
    fun simulerPensjon(spec: TidsbegrensetAfpSpec): FppSimuleringResult {
        val coreSpec: Simulering = specCreator.createSpec(
            uttakFom = spec.uttakFom,
            personinfo = spec.personopplysninger,
            opptjeningListe = spec.opptjeningListe
        )

        return simulator.simulerPensjonsberegning(coreSpec)
    }
}