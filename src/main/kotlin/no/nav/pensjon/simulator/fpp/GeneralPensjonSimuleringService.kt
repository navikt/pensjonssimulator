package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum.*
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.domain.regler.to.SimuleringRequest
import org.springframework.stereotype.Service

@Service
class GeneralPensjonSimuleringService(private val simulator: SimulatorContext) {
    // PEN: DefaultSimuleringConsumerService.simulerVilkarsprovAfp
    //   -> SimulerVilkarsprovAfpConsumerCommand.execute
    fun simulerVilkaarsproevingAvTidsbegrensetOffentligAfp(spec: Simulering): Simuleringsresultat =
        simulator.simulerVilkarsprovPre2025OffentligAfp(
            spec = SimuleringRequest(spec, fom = spec.uttaksdato)
        )

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
            AFP -> simulator.simulerPensjon(spec, serviceName = "simulerAFP") // tidsbegrenset offentlig AFP
            ALDER -> simulator.simulerPensjon(spec, serviceName = "simulerAlderspensjon")
            ALDER_M_GJEN -> simulator.simulerPensjon(spec, serviceName = "simulerGjenlevendeAlderspensjon")
            BARN -> simulator.simulerPensjon(spec, serviceName = "simulerBarnepensjon")
            GJENLEVENDE -> simulator.simulerPensjon(spec, serviceName = "simulerGjenlevendepensjon")
            else -> throw IllegalArgumentException("Ukjent simuleringtype: $simuleringType")
        }
    }
}