package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate

object InntektListeSpecMapper {

    fun createSpec(simuleringSpec: SimuleringSpec, foedselsdato: LocalDate) =
        InntektListeSpec(
            foedselsdato,
            inntektFoerFoersteUttak = simuleringSpec.forventetInntektBeloep,
            gradertUttak = simuleringSpec.uttakGrad != UttakGradKode.P_100,
            simuleringTypeErAfpEtterfAlder = simuleringSpec.type == SimuleringTypeEnum.AFP_ETTERF_ALDER,
            inntektUnderGradertUttakBeloep = simuleringSpec.inntektUnderGradertUttakBeloep,
            inntektEtterHeltUttakBeloep = simuleringSpec.inntektEtterHeltUttakBeloep,
            inntektEtterHeltUttakTom = simuleringSpec.inntektEtterHeltUttakTom
                ?: throw BadSpecException("mangler inntektEtterHeltUttakTom i SimuleringSpec"),
            foersteUttakDato = simuleringSpec.foersteUttakDato!!,
            heltUttakDato = simuleringSpec.heltUttakDato
        )
}