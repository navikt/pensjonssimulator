package no.nav.pensjon.simulator.alderspensjon.tpo.api.acl.v3in

import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid

object TpoSimuleringSpecMapperV3 {

    fun fromDto(source: TpoSimuleringSpecV3) =
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
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = null,
            foedselAar = 0,
            boddUtenlands = false,
            utlandAntallAar = source.utenlandsopphold ?: 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = source.fremtidigInntektList.orEmpty().map(::inntekt).toMutableList(),
            inntektOver1GAntallAar = 0,
            flyktning = null,
            epsHarInntektOver2G = source.eps2G == true,
            rettTilOffentligAfpFom = null,
            afpOrdning = null, // Hvilken AFP-ordning bruker er tilknyttet (kun for simulering av pre-2025 offentlig AFP)
            afpInntektMaanedFoerUttak = null, // Brukers inntekt måneden før uttak av AFP (kun for simulering av pre-2025 offentlig AFP)
            erAnonym = false,
            isHentPensjonsbeholdninger = true, // true for TPO
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true // true for TPO
        )

    private fun inntekt(source: InntektSpecLegacyV3) =
        FremtidigInntekt(
            aarligInntektBeloep = source.arligInntekt,
            fom = source.fomDato
        )
}
