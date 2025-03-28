package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.spec

import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.Pid

/**
 * Maps from received DTO to domain object for specification of 'simulering av folketrygdberegnet AFP'.
 * V1 = Versipn 1 of the API (application programming interface) and DTO (data transfer object)
 * AFP = Avtalefestet pensjon
 */
object FolketrygdberegnetAfpSpecMapperV1 {

    fun fromSimuleringSpecV1(source: FolketrygdberegnetAfpSpecV1) =
        SimuleringSpec(
            type = source.simuleringType?.let { FolketrygdberegnetAfpSimuleringTypeSpecV1.fromExternalValue(it.name).internalValue }
                ?: SimuleringType.ALDER,
            sivilstatus = source.sivilstatus?.let { FolketrygdberegnetAfpSivilstandSpecV1.fromExternalValue(it.name).internalValue }
                ?: SivilstatusType.UGIF,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = source.forsteUttakDato?.toNorwegianLocalDate(),
            heltUttakDato = null, //TODO verify
            pid = source.fnr?.let(::Pid),
            foedselDato = null, // used for anonym only
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
            epsKanOverskrives = false
        )

    private fun pre2025OffentligAfpSpec(simuleringSpec: FolketrygdberegnetAfpSpecV1): Pre2025OffentligAfpSpec? =
        if (simuleringSpec.simuleringType == FolketrygdberegnetAfpSimuleringTypeSpecV1.AFP_ETTERF_ALDER)
            Pre2025OffentligAfpSpec(
                afpOrdning = AfpOrdningType.valueOf(simuleringSpec.afpOrdning!!),
                inntektMaanedenFoerAfpUttakBeloep = simuleringSpec.afpInntektMndForUttak ?: 0,
                // NB: For pre-2025 offentlig AFP brukes 'gradert uttak'-perioden som AFP-periode:
                inntektUnderAfpUttakBeloep = simuleringSpec.inntektUnderGradertUttak ?: 0
            )
        else
            null
}
