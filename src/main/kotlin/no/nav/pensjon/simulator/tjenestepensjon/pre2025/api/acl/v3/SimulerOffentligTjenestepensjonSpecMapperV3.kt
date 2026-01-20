package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.OftpSimuleringSivilstandSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.OftpSimuleringTypeSpec
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import org.springframework.stereotype.Component

@Component
class SimulerOffentligTjenestepensjonSpecMapperV3(
    private val personService: GeneralPersonService,
    private val inntektService: InntektService
) {
    fun fromDto(dto: SimulerOffentligTjenestepensjonSpecV3): SimuleringSpec {
        val source = dto.simuleringEtter2011
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
            utlandPeriodeListe = source.utenlandsperiodeForSimuleringList.map(::utlandPeriode)
                .toMutableList(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0, // used for anonym only
            flyktning = source.flyktning,
            epsHarInntektOver2G = source.eps2G == true,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = pre2025OffentligAfpSpec(source),
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }

    fun stillingsprosentFromDto(spec: SimulerOffentligTjenestepensjonSpecV3) =
        StillingsprosentSpec(
            heltUttak = spec.simuleringEtter2011.stillingsprosentOffHeltUttak,
            gradertUttak = spec.simuleringEtter2011.stillingsprosentOffGradertUttak
        )

    private fun pre2025OffentligAfpSpec(simuleringSpec: SimuleringEtter2011SpecV3): Pre2025OffentligAfpSpec? =
        if (simuleringSpec.simuleringType == SimuleringTypeSpecV3.AFP_ETTERF_ALDER)
            Pre2025OffentligAfpSpec(
                afpOrdning = AFPtypeEnum.valueOf(simuleringSpec.afpOrdning!!.name),
                inntektMaanedenFoerAfpUttakBeloep = simuleringSpec.afpInntektMndForUttak?.let(inntektService::hentSisteMaanedsInntektOver1G)
                    ?: 0,
                // NB: For pre-2025 offentlig AFP brukes 'gradert uttak'-perioden som AFP-periode:
                inntektUnderAfpUttakBeloep = simuleringSpec.inntektUnderGradertUttak ?: 0
            )
        else
            null

    private fun utlandPeriode(source: UtenlandsperiodeForSimuleringV3) =
        UtlandPeriode(
            land = LandkodeEnum.extendedValueOf(source.land),
            arbeidet = source.arbeidetIUtland,
            fom = source.periodeFom,
            tom = source.periodeTom
        )

    private fun avdoed(source: SimuleringEtter2011SpecV3): Avdoed? =
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