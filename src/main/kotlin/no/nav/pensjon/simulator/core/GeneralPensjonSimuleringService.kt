package no.nav.pensjon.simulator.core

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.domain.regler.to.SimuleringRequest
import no.nav.pensjon.simulator.core.spec.ExtraSimuleringSpec
import org.springframework.stereotype.Service

@Service
class GeneralPensjonSimuleringService(private val simulator: SimulatorContext) {

    // PEN: DefaultSimuleringConsumerService.simulerAlderspensjon
    //   -> SimulerPensjonsberegningConsumerCommand.execute
    fun simulerPensjon(
        coreSpec: Simulering,
        extraSpec: ExtraSimuleringSpec,
        simuleringType: SimuleringTypeEnum
    ): Simuleringsresultat {
        val spec = SimuleringRequest(
            simulering = coreSpec,
            fom = coreSpec.uttaksdato,
            ektefelleMottarPensjon = extraSpec.epsMottarPensjon,
            beregnForsorgingstillegg = extraSpec.beregnForsoergingstillegg,
            beregnInstitusjonsopphold = extraSpec.beregnInstitusjonsopphold
        )

        return when (simuleringType) {
            SimuleringTypeEnum.AFP -> simulator.simulerPensjon(spec, serviceName = "simulerAFP") // tidsbegrenset offentlig AFP
            SimuleringTypeEnum.ALDER -> simulator.simulerPensjon(spec, serviceName = "simulerAlderspensjon")
            SimuleringTypeEnum.ALDER_M_GJEN -> simulator.simulerPensjon(spec, serviceName = "simulerGjenlevendeAlderspensjon")
            SimuleringTypeEnum.BARN -> simulator.simulerPensjon(spec, serviceName = "simulerBarnepensjon")
            SimuleringTypeEnum.GJENLEVENDE -> simulator.simulerPensjon(spec, serviceName = "simulerGjenlevendepensjon")
            else -> throw IllegalArgumentException("Ukjent simuleringtype: $simuleringType")
        }
    }
}