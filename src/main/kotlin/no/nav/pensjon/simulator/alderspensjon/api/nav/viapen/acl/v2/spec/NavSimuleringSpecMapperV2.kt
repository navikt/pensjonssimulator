package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec

import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import org.springframework.stereotype.Component

@Component
class NavSimuleringSpecMapperV2(val personService: GeneralPersonService) {

    fun fromSimuleringSpecV2(
        source: NavSimuleringSpecV2,
        isHentPensjonsbeholdninger: Boolean,
        isOutputSimulertBeregningsinformasjonForAllKnekkpunkter: Boolean
    ): SimuleringSpec {
        val pid = source.fnr?.let(::Pid)

        return SimuleringSpec(
            type = source.simuleringType?.let { NavSimuleringTypeSpecV2.fromExternalValue(it.name).internalValue }
                ?: SimuleringTypeEnum.ALDER,
            sivilstatus = source.sivilstatus?.let { NavSivilstandSpecV2.fromExternalValue(it.name).internalValue }
                ?: SivilstatusType.UGIF,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = source.forsteUttakDato?.toNorwegianLocalDate(),
            heltUttakDato = source.heltUttakDato?.toNorwegianLocalDate(),
            pid = pid,
            foedselDato = pid?.let(personService::foedselsdato),
            avdoed = avdoed(source),
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = source.utg ?: UttakGradKode.P_100,
            forventetInntektBeloep = source.forventetInntekt ?: 0,
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttak ?: 0,
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttak ?: 0,
            inntektEtterHeltUttakAntallAar = source.antallArInntektEtterHeltUttak ?: 0,
            foedselAar = source.fodselsar ?: 0,
            utlandAntallAar = source.utenlandsopphold ?: 0,
            utlandPeriodeListe = source.utenlandsperiodeForSimuleringList.orEmpty().map(::utlandPeriode).toMutableList(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0, // used for anonym only
            flyktning = source.flyktning,
            epsHarInntektOver2G = source.eps2G == true,
            livsvarigOffentligAfp = null, //TODO map to offentligAfpRett?
            pre2025OffentligAfp = pre2025OffentligAfpSpec(source),
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = isHentPensjonsbeholdninger,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = isOutputSimulertBeregningsinformasjonForAllKnekkpunkter,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }

    private fun pre2025OffentligAfpSpec(simuleringSpec: NavSimuleringSpecV2): Pre2025OffentligAfpSpec? =
        if (simuleringSpec.simuleringType == NavSimuleringTypeSpecV2.AFP_ETTERF_ALDER)
            Pre2025OffentligAfpSpec(
                afpOrdning = simuleringSpec.afpOrdning!!,
                inntektMaanedenFoerAfpUttakBeloep = simuleringSpec.afpInntektMndForUttak ?: 0,
                // NB: For pre-2025 offentlig AFP brukes 'gradert uttak'-perioden som AFP-periode:
                inntektUnderAfpUttakBeloep = simuleringSpec.inntektUnderGradertUttak ?: 0
            )
        else
            null

    private fun utlandPeriode(source: NavSimuleringUtlandPeriodeV2) =
        UtlandPeriode(
            land = source.land,
            arbeidet = source.arbeidetIUtland,
            fom = source.periodeFom.toNorwegianLocalDate(),
            tom = source.periodeTom?.toNorwegianLocalDate()
        )

    private fun avdoed(source: NavSimuleringSpecV2): Avdoed? =
        source.fnrAvdod?.let {
            Avdoed(
                pid = Pid(it),
                antallAarUtenlands = source.avdodAntallArIUtlandet ?: 0,
                inntektFoerDoed = source.avdodInntektForDod ?: 0,
                doedDato = source.dodsdato!!.toNorwegianLocalDate(),
                erMedlemAvFolketrygden = source.avdodMedlemAvFolketrygden == true,
                harInntektOver1G = source.inntektAvdodOver1G == true
            )
        }
}
