package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1

import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid

object TpoSimuleringSpecMapperV1 {

    fun fromDto(source: TpoSimuleringSpecV1) =
        SimuleringSpec(
            type = source.simuleringType ?: SimuleringType.ALDER,
            sivilstatus = source.sivilstatus ?: SivilstatusType.UGIF,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = source.foersteUttakDato,
            heltUttakDato = source.heltUttakDato,
            pid = source.pid?.let(::Pid),
            foedselDato = null,
            avdoed = null,
            isTpOrigSimulering = true, // true for TPO
            simulerForTp = false,
            uttakGrad = source.uttakGrad ?: UttakGradKode.P_100,
            forventetInntektBeloep = source.forventetInntekt ?: 0, // V1 only
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttak ?: 0, // V1 only
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttak ?: 0, // V1 only
            inntektEtterHeltUttakAntallAar = source.antallArInntektEtterHeltUttak, // V1, V2 only
            foedselAar = 0,
            boddUtenlands = false,
            utlandAntallAar = source.utenlandsopphold ?: 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(), // V2, V3 only
            inntektOver1GAntallAar = 0,
            flyktning = null,
            epsHarInntektOver2G = source.eps2G == true,
            rettTilOffentligAfpFom = null,
            afpOrdning = null, // Hvilken AFP-ordning bruker er tilknyttet (kun for simulering av pre-2025 offentlig AFP)
            afpInntektMaanedFoerUttak = null, // Brukers inntekt måneden før uttak av AFP (kun for simulering av pre-2025 offentlig AFP)
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // true for TPO
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true // true for TPO
        )
}
