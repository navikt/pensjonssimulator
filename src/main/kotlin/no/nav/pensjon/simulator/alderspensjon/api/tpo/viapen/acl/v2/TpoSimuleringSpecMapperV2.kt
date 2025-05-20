package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2

import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component

@Component
class TpoSimuleringSpecMapperV2(val personService: GeneralPersonService) {

    fun fromDto(source: TpoSimuleringSpecV2): SimuleringSpec {
        val pid = source.pid?.let(::Pid)

        return SimuleringSpec(
            type = source.simuleringType ?: SimuleringType.ALDER,
            sivilstatus = source.sivilstatus ?: SivilstatusType.UGIF,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = source.foersteUttakDato,
            heltUttakDato = source.heltUttakDato,
            pid = pid,
            foedselDato = pid?.let(personService::foedselsdato),
            avdoed = null,
            isTpOrigSimulering = true, // true for TPO
            simulerForTp = false,
            uttakGrad = source.uttakGrad ?: UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = source.antallArInntektEtterHeltUttak, // V1, V2 only
            foedselAar = 0,
            utlandAntallAar = source.utenlandsopphold ?: 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = source.fremtidigInntektList.orEmpty().map(::inntekt).toMutableList(), // V2, V3 only
            brukFremtidigInntekt = true,
            inntektOver1GAntallAar = 0,
            flyktning = null,
            epsHarInntektOver2G = source.eps2G == true,
            rettTilOffentligAfpFom = null,
            pre2025OffentligAfp = null, // never used in this context
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // true for TPO
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // true for TPO
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }

    private fun inntekt(source: InntektSpecLegacyV2) =
        FremtidigInntekt(
            aarligInntektBeloep = source.arligInntekt,
            fom = source.fomDato
        )
}
