package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.spec

import no.nav.pensjon.simulator.core.spec.SimuleringSpec

/**
 * Maps between data transfer objects (DTOs) and domain objects related to 'anonym simulering'.
 * The DTOs are specified by version 1 of the API offered to clients.
 */
object AnonymSimuleringSpecMapperV1 {

    fun fromAnonymSimuleringSpecV1(source: AnonymSimuleringSpecV1) =
        SimuleringSpec(
            type = AnonymSimuleringTypeSpecV1.fromExternalValue(source.simuleringType).internalValue,
            foedselAar = source.fodselsar ?: 0,
            forventetInntektBeloep = source.forventetInntekt ?: 0,
            inntektOver1GAntallAar = source.antArInntektOverG ?: 0,
            foersteUttakDato = source.forsteUttakDato,
            uttakGrad = AnonymUttakGradSpecV1.fromExternalValue(source.utg).internalValue,
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttak ?: 0,
            heltUttakDato = source.heltUttakDato,
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttak ?: 0,
            inntektEtterHeltUttakAntallAar = source.antallArInntektEtterHeltUttak ?: 0,
            utlandAntallAar = source.utenlandsopphold ?: 0,
            sivilstatus = AnonymSivilstandSpecV1.fromExternalValue(source.sivilstatus).internalValue,
            epsHarPensjon = source.epsPensjon == true,
            epsHarInntektOver2G = source.eps2G == true,
            erAnonym = true,
            ignoreAvslag = false,
            // Resten er irrelevante for anonym simulering:
            pid = null,
            foedselDato = null,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            boddUtenlands = false,
            flyktning = false,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            rettTilOffentligAfpFom = null
        )
}
