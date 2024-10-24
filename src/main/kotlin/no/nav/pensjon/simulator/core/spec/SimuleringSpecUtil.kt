package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.alder.PensjonAlderDato
import java.time.LocalDate

object SimuleringSpecUtil {

    private val utkantUttakGrad = UttakGradKode.P_20 // kun hvis gradert uttak

    /**
     * Spesifikasjon for å simulere for ubetinget uttaksalder (normalder, dvs. alderen der enhver kan ta ut pensjon).
     */
    fun ubetingetSimuleringSpec(
        source: SimuleringSpec,
        normAlder: Alder,
        foedselDato: LocalDate
    ): SimuleringSpec {
        val uttakFomAlder = PensjonAlderDato(foedselDato, alderSpec(normAlder))

        return newSimuleringSpec(
            source,
            foersteUttakFom = uttakFomAlder,
            uttakGrad = UttakGradKode.P_100,
            heltUttakFom = uttakFomAlder,
            foedselDato
        )
    }

    /**
     * Spesifikasjon for simulering med "utkanttilfellet" (dårligste uttaksparametre før normalder).
     */
    fun utkantSimuleringSpec(
        source: SimuleringSpec,
        normAlder: Alder,
        foedselDato: LocalDate
    ): SimuleringSpec {
        val gradert = source.isGradert()
        val utkantFoersteUttakAlder: Alder = normAlder.minusMaaneder(1)
        val utkantFoersteUttakFomAlderSpec: Alder = alderSpec(utkantFoersteUttakAlder)
        val heltUttakFomAlderDto: Alder =
            if (gradert) alderSpec(normAlder) else utkantFoersteUttakFomAlderSpec

        return newSimuleringSpec(
            source,
            foersteUttakFom = PensjonAlderDato(foedselDato, utkantFoersteUttakFomAlderSpec),
            uttakGrad = if (gradert) utkantUttakGrad else UttakGradKode.P_100,
            heltUttakFom = PensjonAlderDato(foedselDato, heltUttakFomAlderDto),
            foedselDato
        )
    }

    fun withLavereUttakGrad(source: SimuleringSpec): SimuleringSpec =
        newSimuleringSpec(
            source,
            uttaksgrad = if (source.isGradert()) naermesteLavereUttakGrad(source.uttakGrad) else UttakGradKode.P_100
        )

    private fun newSimuleringSpec(
        source: SimuleringSpec,
        foersteUttakFom: PensjonAlderDato,
        uttakGrad: UttakGradKode,
        heltUttakFom: PensjonAlderDato,
        foedselDato: LocalDate
    ): SimuleringSpec {
        val heltUttakDato: LocalDate = source.heltUttak(foedselDato, heltUttakFom).uttakFom.dato

        return SimuleringSpec(
            type = source.type,
            sivilstatus = source.sivilstatus,
            epsHarPensjon = source.epsHarPensjon,
            foersteUttakDato = source.gradertUttak(foersteUttakFom, uttakGrad)?.uttakFom?.dato ?: heltUttakDato,
            heltUttakDato = heltUttakDato,
            pid = source.pid,
            foedselDato = source.foedselDato,
            avdoed = source.avdoed,
            isTpOrigSimulering = source.isTpOrigSimulering,
            simulerForTp = source.simulerForTp,
            uttakGrad = uttakGrad,
            forventetInntektBeloep = source.forventetInntektBeloep,
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttakBeloep,
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttakBeloep,
            inntektEtterHeltUttakAntallAar = source.inntektEtterHeltUttakAntallAar, // assuming this is independent of heltUttakFom
            foedselAar = source.foedselAar,
            boddUtenlands = source.boddUtenlands,
            utlandAntallAar = source.utlandAntallAar,
            utlandPeriodeListe = source.utlandPeriodeListe,
            fremtidigInntektListe = source.fremtidigInntektListe,
            inntektOver1GAntallAar = source.inntektOver1GAntallAar,
            flyktning = source.flyktning,
            epsHarInntektOver2G = source.epsHarInntektOver2G,
            rettTilOffentligAfpFom = source.rettTilOffentligAfpFom,
            afpOrdning = source.afpOrdning,
            afpInntektMaanedFoerUttak = source.afpInntektMaanedFoerUttak,
            erAnonym = source.erAnonym,
            isHentPensjonsbeholdninger = source.isHentPensjonsbeholdninger,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = source.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter
        )
    }

    private fun newSimuleringSpec(source: SimuleringSpec, uttaksgrad: UttakGradKode) =
        SimuleringSpec(
            type = source.type,
            sivilstatus = source.sivilstatus,
            epsHarPensjon = source.epsHarPensjon,
            foersteUttakDato = source.foersteUttakDato,
            heltUttakDato = source.heltUttakDato,
            pid = source.pid,
            foedselDato = source.foedselDato,
            avdoed = source.avdoed,
            isTpOrigSimulering = source.isTpOrigSimulering,
            simulerForTp = source.simulerForTp,
            uttakGrad = uttaksgrad,
            forventetInntektBeloep = source.forventetInntektBeloep,
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttakBeloep,
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttakBeloep,
            inntektEtterHeltUttakAntallAar = source.inntektEtterHeltUttakAntallAar,
            foedselAar = source.foedselAar,
            boddUtenlands = source.boddUtenlands,
            utlandAntallAar = source.utlandAntallAar,
            utlandPeriodeListe = source.utlandPeriodeListe,
            fremtidigInntektListe = source.fremtidigInntektListe,
            inntektOver1GAntallAar = source.inntektOver1GAntallAar,
            flyktning = source.flyktning,
            epsHarInntektOver2G = source.epsHarInntektOver2G,
            rettTilOffentligAfpFom = source.rettTilOffentligAfpFom,
            afpOrdning = source.afpOrdning,
            afpInntektMaanedFoerUttak = source.afpInntektMaanedFoerUttak,
            erAnonym = source.erAnonym,
            isHentPensjonsbeholdninger = source.isHentPensjonsbeholdninger,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = source.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter
        )

    private fun naermesteLavereUttakGrad(grad: UttakGradKode) =
        when (grad) {
            UttakGradKode.P_0 -> UttakGradKode.P_0
            UttakGradKode.P_20 -> UttakGradKode.P_0
            UttakGradKode.P_40 -> UttakGradKode.P_20
            UttakGradKode.P_50 -> UttakGradKode.P_40
            UttakGradKode.P_60 -> UttakGradKode.P_50
            UttakGradKode.P_80 -> UttakGradKode.P_60
            UttakGradKode.P_100 -> UttakGradKode.P_80
        }

    private fun alderSpec(source: Alder) = Alder(source.aar, source.maaneder)
}
