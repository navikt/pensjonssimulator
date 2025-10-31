package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning

import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2.SimulerOffentligTjenestepensjonSpecV2
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2.SimuleringEtter2011SpecV2
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2.SimuleringTypeSpecV2
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2.UtenlandsperiodeForSimuleringV2
import org.springframework.stereotype.Component

@Component
class SimulerOffentligTjenestepensjonMapperV2(val personService: GeneralPersonService) {

    fun fromDto(specV2: SimulerOffentligTjenestepensjonSpecV2): SimuleringSpec {
        val source = specV2.simuleringEtter2011
        val pid = source.fnr.pid.let(::Pid)

        return SimuleringSpec(
            type = source.simuleringType.let { OftpSimuleringTypeSpec.fromExternalValue(it.name).internalValue },
            sivilstatus = source.sivilstatus?.let { OftpSimuleringSivilstandSpec.fromExternalValue(it.name).internalValue }
                ?: SivilstatusType.UGIF,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = source.forsteUttakDato,
            heltUttakDato = source.heltUttakDato,
            pid = pid,
            foedselDato = pid.let(personService::foedselsdato),
            avdoed = avdoed(source),
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = source.utg?.internalValue ?: UttakGradKode.P_100,
            forventetInntektBeloep = source.forventetInntekt ?: 0,
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttak ?: 0,
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttak,
            inntektEtterHeltUttakAntallAar = source.antallArInntektEtterHeltUttak,
            foedselAar = source.fodselsar ?: 0,
            utlandAntallAar = source.utenlandsopphold ?: 0,
            utlandPeriodeListe = source.utenlandsperiodeForSimuleringList.filterNotNull().map(::utlandPeriode)
                .toMutableList(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0, // used for anonym only
            flyktning = source.flyktning,
            epsHarInntektOver2G = source.eps2G == true,
            rettTilOffentligAfpFom = null,
            pre2025OffentligAfp = pre2025OffentligAfpSpec(source),
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }

    fun stillingsprosentFromDto(spec: SimulerOffentligTjenestepensjonSpecV2): StillingsprosentSpec {
        return StillingsprosentSpec(
            heltUttak = spec.simuleringEtter2011.stillingsprosentOffHeltUttak,
            gradertUttak = spec.simuleringEtter2011.stillingsprosentOffGradertUttak
        )
    }

    fun pre2025OffentligAfpSpec(simuleringSpec: SimuleringEtter2011SpecV2): Pre2025OffentligAfpSpec? =
        if (simuleringSpec.simuleringType == SimuleringTypeSpecV2.AFP_ETTERF_ALDER)
            Pre2025OffentligAfpSpec(
                afpOrdning = AFPtypeEnum.valueOf(simuleringSpec.afpOrdning!!.name),
                inntektMaanedenFoerAfpUttakBeloep = simuleringSpec.afpInntektMndForUttak ?: 0,
                // NB: For pre-2025 offentlig AFP brukes 'gradert uttak'-perioden som AFP-periode:
                inntektUnderAfpUttakBeloep = simuleringSpec.inntektUnderGradertUttak ?: 0
            )
        else
            null

    private fun utlandPeriode(source: UtenlandsperiodeForSimuleringV2) =
        UtlandPeriode(
            land = mapLand(source.land),
            arbeidet = source.arbeidetIUtland,
            fom = source.periodeFom,
            tom = source.periodeTom
        )

    fun mapLand(land: String) : LandkodeEnum {
        return irregularLandEnums[land] ?: LandkodeEnum.valueOf(land)
    }

    private val irregularLandEnums: Map<String, LandkodeEnum>
        get() {
            val land: MutableMap<String, LandkodeEnum> = mutableMapOf()
            land["???"] = LandkodeEnum.P_UKJENT
            land["349"] = LandkodeEnum.P_SPANSKE_OMR_AFRIKA
            land["546"] = LandkodeEnum.P_SIKKIM
            land["556"] = LandkodeEnum.P_YEMEN
            land["669"] = LandkodeEnum.P_PANAMAKANALSONEN
            return land
        }

    fun avdoed(source: SimuleringEtter2011SpecV2): Avdoed? =
        source.fnrAvdod?.pid?.let {
            Avdoed(
                pid = Pid(it),
                antallAarUtenlands = source.avdodAntallArIUtlandet ?: 0,
                inntektFoerDoed = source.avdodInntektForDod ?: 0,
                doedDato = source.dodsdato!!,
                erMedlemAvFolketrygden = source.avdodMedlemAvFolketrygden == true,
                harInntektOver1G = source.inntektAvdodOver1G == true
            )
        }
}
