package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import org.springframework.stereotype.Service

@Service
class FppSimuleringFacade(
    private val simuleringSpecCreator: FppSimuleringSpecCreator,
    private val simulator: FppSimuleringService
) {
    // PEN: PensjonskalkulatorController.lagreFpp
    //   -> PensjonskalkulatorController.simulerPensjon
    fun simulerPensjon(
        simuleringType: SimuleringTypeEnum,
        spec: FppSimuleringSpec
    ): FppSimuleringResult {
        val coreSpec: Simulering = simuleringSpecCreator.createSpec(
            simuleringType,
            uttaksdato = spec.uttaksdato,
            personopplysninger = spec.personopplysninger,
            opptjeningFolketrygden = spec.opptjeningFolketrygden,
            barneopplysninger = spec.barneopplysninger
        )

        return simulator.simulerPensjonsberegning(coreSpec)
    }
}