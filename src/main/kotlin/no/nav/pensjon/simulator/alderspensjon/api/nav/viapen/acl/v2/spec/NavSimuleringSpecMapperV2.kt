package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec

import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.person.Pid

object NavSimuleringSpecMapperV2 {

    fun fromSimuleringSpecV2(source: NavSimuleringSpecV2) =
        SimuleringSpec(
            type = source.simuleringType?.let { NavSimuleringTypeSpecV2.fromExternalValue(it.name).internalValue }
                ?: SimuleringType.ALDER,
            sivilstatus = source.sivilstatus?.let { NavSivilstandSpecV2.fromExternalValue(it.name).internalValue }
                ?: SivilstatusType.UGIF,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = source.forsteUttakDato,
            heltUttakDato = source.heltUttakDato,
            pid = source.fnr?.let(::Pid),
            foedselDato = null, // used for anonym only
            avdoed = avdoed(source),
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = source.utg ?: UttakGradKode.P_100,
            forventetInntektBeloep = source.forventetInntekt ?: 0,
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttak ?: 0,
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttak ?: 0,
            inntektEtterHeltUttakAntallAar = source.antallArInntektEtterHeltUttak ?: 0,
            foedselAar = source.fodselsar ?: 0,
            boddUtenlands = false, // used for anonym only
            utlandAntallAar = source.utenlandsopphold ?: 0,
            utlandPeriodeListe = source.utenlandsperiodeForSimuleringList.map(::utlandPeriode).toMutableList(),
            fremtidigInntektListe = mutableListOf(), // not used in PSELV
            inntektOver1GAntallAar = 0, // used for anonym only
            flyktning = source.flyktning,
            epsHarInntektOver2G = source.eps2G == true,
            rettTilOffentligAfpFom = null, //TODO map to offentligAfpRett?
            afpOrdning = source.afpOrdning, // Hvilken AFP-ordning bruker er tilknyttet (kun for simulering av pre-2025 offentlig AFP)
            afpInntektMaanedFoerUttak = source.afpInntektMndForUttak, // Brukers inntekt måneden før uttak av AFP (kun for simulering av pre-2025 offentlig AFP)
            erAnonym = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false
        )

    private fun utlandPeriode(source: UtlandPeriodeV2) =
        UtlandPeriode(
            land = source.land,
            arbeidet = source.arbeidetIUtland,
            fom = source.periodeFom,
            tom = source.periodeTom
        )

  private  fun avdoed(source: NavSimuleringSpecV2): Avdoed? =
        source.fnrAvdod?.let {
            Avdoed(
                pid = it.let(::Pid),
                antallAarUtenlands = source.avdodAntallArIUtlandet ?: 0,
                inntektFoerDoed = source.avdodInntektForDod ?: 0,
                doedDato = source.dodsdato!!,
                erMedlemAvFolketrygden = source.avdodMedlemAvFolketrygden == true,
                harInntektOver1G = source.inntektAvdodOver1G == true
            )
        }
}
