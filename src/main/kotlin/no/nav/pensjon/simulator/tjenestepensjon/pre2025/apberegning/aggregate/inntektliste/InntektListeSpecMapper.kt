package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import java.time.LocalDate

object InntektListeSpecMapper {

    fun createSpec(
        simuleringSpec: SimuleringSpec,
        foedselsdato: LocalDate,
    ): InntektListeSpec = InntektListeSpec(
        foedselsdato = foedselsdato,
        inntektFoerFoersteUttak = simuleringSpec.forventetInntektBeloep,
        gradertUttak = simuleringSpec.uttakGrad != UttakGradKode.P_100,
        simuleringTypeErAfpEtterfAlder =  simuleringSpec.type == SimuleringTypeEnum.AFP_ETTERF_ALDER,
        inntektUnderGradertUttakBeloep =  simuleringSpec.inntektUnderGradertUttakBeloep,
        inntektEtterHeltUttakBeloep =  simuleringSpec.inntektEtterHeltUttakBeloep,
        inntektEtterHeltUttakAntallAar =  simuleringSpec.inntektEtterHeltUttakAntallAar,
        foersteUttakDato =  simuleringSpec.foersteUttakDato!!,
        heltUttakDato =  simuleringSpec.heltUttakDato
    )
}