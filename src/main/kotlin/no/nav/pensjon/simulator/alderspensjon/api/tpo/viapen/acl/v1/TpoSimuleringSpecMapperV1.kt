package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1

import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component

@Component
class TpoSimuleringSpecMapperV1(val personService: GeneralPersonService) {

    fun fromDto(source: TpoSimuleringSpecV1): SimuleringSpec {
        val pid = source.pid?.let(::Pid)

        return SimuleringSpec(
            type = source.simuleringType ?: SimuleringTypeEnum.ALDER,
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
            forventetInntektBeloep = source.forventetInntekt ?: 0, // V1 only
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttak ?: 0, // V1 only
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttak ?: 0, // V1 only
            inntektEtterHeltUttakAntallAar = source.antallArInntektEtterHeltUttak, // V1 only
            foedselAar = 0,
            utlandAntallAar = source.utenlandsopphold ?: 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(), // V3 only
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = null,
            epsHarInntektOver2G = source.eps2G == true,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null, // never used in this context
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // true for TPO
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // true for TPO
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }
}
