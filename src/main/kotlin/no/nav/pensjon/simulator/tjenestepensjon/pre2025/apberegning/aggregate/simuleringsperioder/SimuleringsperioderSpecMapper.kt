package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simuleringsperioder

import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SimulertOffentligAfp
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate

object SimuleringsperioderSpecMapper {

    fun createSpec(
        simuleringSpec: SimuleringSpec,
        offentligAfp: SimulertOffentligAfp?,
        stillingsprosentSpec: StillingsprosentSpec,
        foedselsdato: LocalDate
    ) =
        SimuleringsperioderSpec(
            afpEtterfulgtAvAlder = offentligAfp?.brutto != null,
            foedselsdato,
            stillingsprosentSpec,
            folketrygdUttaksgrad = simuleringSpec.uttakGrad.value.toInt(),
            simuleringType = simuleringSpec.type,
            foersteUttakDato = simuleringSpec.foersteUttakDato!!,
            heltUttakDato = simuleringSpec.heltUttakDato,
            inntektEtterHeltUttakTom = simuleringSpec.inntektEtterHeltUttakTom
                ?: throw BadSpecException("SimuleringsperioderSpecMapper melder at inntektEtterHeltUttakTom mangler i SimuleringSpec")
        )
}