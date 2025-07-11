package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct.acl.v0.spec

import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component

/**
 * Maps from received DTO to domain object for specification of 'simulering av folketrygdberegnet AFP'.
 * V0 = Versipn 0 of the API (application programming interface) and DTO (data transfer object)
 * AFP = Avtalefestet pensjon
 */
@Component
class TpoFolketrygdberegnetAfpSpecMapperV0(val personService: GeneralPersonService) {

    fun fromSimuleringSpecV0(source: TpoFolketrygdberegnetAfpSpecV0): SimuleringSpec {
        val pid = source.fnr?.pid?.let(::Pid)

        return SimuleringSpec(
            type = source.simuleringType?.let { TpoFolketrygdberegnetAfpSimuleringTypeSpecV0.fromExternalValue(it.name).internalValue }
                ?: SimuleringTypeEnum.ALDER,
            sivilstatus = source.sivilstatus?.let { TpoFolketrygdberegnetAfpSivilstandSpecV0.fromExternalValue(it.name).internalValue }
                ?: SivilstatusType.UGIF,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = source.forsteUttakDato?.toNorwegianLocalDate(),
            heltUttakDato = null, //TODO verify
            pid = pid,
            foedselDato = pid?.let(personService::foedselsdato),
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100, //TODO verify
            forventetInntektBeloep = source.forventetInntekt ?: 0,
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttak ?: 0,
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttak ?: 0,
            inntektEtterHeltUttakAntallAar = source.antallArInntektEtterHeltUttak ?: 0,
            foedselAar = 0,
            utlandAntallAar = source.utenlandsopphold ?: 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0, // used for anonym only
            flyktning = null,
            epsHarInntektOver2G = source.eps2G == true,
            rettTilOffentligAfpFom = null, //TODO map to offentligAfpRett?
            pre2025OffentligAfp = pre2025OffentligAfpSpec(source),
            erAnonym = false, //TODO verify
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false, //TODO verify
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false, //TODO verify
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false,
        )
    }

    private fun pre2025OffentligAfpSpec(simuleringSpec: TpoFolketrygdberegnetAfpSpecV0): Pre2025OffentligAfpSpec? =
        if (simuleringSpec.simuleringType == TpoFolketrygdberegnetAfpSimuleringTypeSpecV0.AFP_ETTERF_ALDER)
            Pre2025OffentligAfpSpec(
                afpOrdning = AFPtypeEnum.valueOf(simuleringSpec.afpOrdning!!),
                inntektMaanedenFoerAfpUttakBeloep = simuleringSpec.afpInntektMndForUttak ?: 0,
                // NB: For pre-2025 offentlig AFP brukes 'gradert uttak'-perioden som AFP-periode:
                inntektUnderAfpUttakBeloep = simuleringSpec.inntektUnderGradertUttak ?: 0
            )
        else
            null
}
