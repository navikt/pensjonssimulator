package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.alder.PensjonAlderDato
import java.time.LocalDate

object SimuleringSpecUtil {

    private val utkantUttaksgrad = UttakGradKode.P_20 // kun hvis gradert uttak

    /**
     * Spesifikasjon for å simulere for ubetinget uttaksalder (normalder, dvs. alderen der enhver kan ta ut pensjon).
     */
    fun ubetingetSimuleringSpec(
        source: SimuleringSpec,
        normalder: Alder
    ): SimuleringSpec {
        val uttakFomAlder = PensjonAlderDato(source.foedselDato!!, normalder)

        return newSimuleringSpec(
            source,
            foersteUttakFom = uttakFomAlder,
            uttaksgrad = UttakGradKode.P_100,
            heltUttakFom = uttakFomAlder
        )
    }

    /**
     * Spesifikasjon for simulering med "utkanttilfellet" (dårligste uttaksparametre før normalder).
     */
    fun utkantSimuleringSpec(
        source: SimuleringSpec,
        normalder: Alder,
        foedselsdato: LocalDate,
        foersteUttakAlderIsConstant: Boolean = false
    ): SimuleringSpec {
        val gradert = source.isGradert()

        val maxAlder = if (source.onlyVilkaarsproeving && gradert)
            PensjonAlderDato(foedselsdato, source.heltUttakDato!!)
        else
            PensjonAlderDato(foedselsdato, normalder)

        val variableFoersteUttakAlder = maxAlder.alder.minusMaaneder(1)

        val utkantFoersteUttakAlder: Alder =
            if (foersteUttakAlderIsConstant) PensjonAlderDato(foedselsdato, source.foersteUttakDato!!).alder
            else variableFoersteUttakAlder

        val heltUttakFomAlder: Alder =
            if (gradert) maxAlder.alder else variableFoersteUttakAlder

        return newSimuleringSpec(
            source,
            foersteUttakFom = PensjonAlderDato(foedselsdato, utkantFoersteUttakAlder),
            uttaksgrad =
                if (gradert)
                    if (source.onlyVilkaarsproeving) source.uttakGrad else utkantUttaksgrad
                else
                    UttakGradKode.P_100,
            heltUttakFom = PensjonAlderDato(foedselsdato, heltUttakFomAlder)
        )
    }

    fun withLavereUttakGrad(
        source: SimuleringSpec,
        tillatOvergangFraHeltTilGradertUttak: Boolean = false
    ): SimuleringSpec =
        newSimuleringSpec(
            source,
            uttaksgrad =
                if (tillatOvergangFraHeltTilGradertUttak || source.isGradert())
                    naermesteLavereUttaksgrad(source.uttakGrad)
                else
                    UttakGradKode.P_100
        )

    fun withGradertInsteadOfHeltUttak(
        source: SimuleringSpec,
        normalder: Alder,
        foedselsdato: LocalDate
    ): SimuleringSpec =
        newSimuleringSpec(
            source,
            foersteUttakFom = PensjonAlderDato(foedselsdato, source.foersteUttakDato!!),
            uttaksgrad = naermesteLavereUttaksgrad(UttakGradKode.P_100),
            heltUttakFom = PensjonAlderDato(foedselsdato, normalder)
        )

    private fun newSimuleringSpec(
        source: SimuleringSpec,
        foersteUttakFom: PensjonAlderDato,
        uttaksgrad: UttakGradKode,
        heltUttakFom: PensjonAlderDato
    ): SimuleringSpec {
        val heltUttakDato: LocalDate = source.heltUttak(heltUttakFom).uttakFom.dato

        return SimuleringSpec(
            type = source.type,
            sivilstatus = source.sivilstatus,
            epsHarPensjon = source.epsHarPensjon,
            foersteUttakDato = source.gradertUttak(foersteUttakFom, uttaksgrad)?.uttakFom?.dato ?: heltUttakDato,
            heltUttakDato = heltUttakDato,
            pid = source.pid,
            foedselDato = source.foedselDato,
            avdoed = source.avdoed,
            isTpOrigSimulering = source.isTpOrigSimulering,
            simulerForTp = source.simulerForTp,
            uttakGrad = uttaksgrad,
            forventetInntektBeloep = source.forventetInntektBeloep,
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttakBeloep,
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttakBeloep,
            inntektEtterHeltUttakAntallAar = source.inntektEtterHeltUttakAntallAar, // assuming this is independent of heltUttakFom
            foedselAar = source.foedselAar,
            utlandAntallAar = source.utlandAntallAar,
            utlandPeriodeListe = source.utlandPeriodeListe,
            fremtidigInntektListe = source.fremtidigInntektListe,
            brukFremtidigInntekt = source.brukFremtidigInntekt,
            inntektOver1GAntallAar = source.inntektOver1GAntallAar,
            flyktning = source.flyktning,
            epsHarInntektOver2G = source.epsHarInntektOver2G,
            rettTilOffentligAfpFom = source.rettTilOffentligAfpFom,
            pre2025OffentligAfp = source.pre2025OffentligAfp,
            erAnonym = source.erAnonym,
            ignoreAvslag = source.ignoreAvslag,
            isHentPensjonsbeholdninger = source.isHentPensjonsbeholdninger,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = source.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter,
            onlyVilkaarsproeving = source.onlyVilkaarsproeving,
            epsKanOverskrives = source.epsKanOverskrives,
            registerData = source.registerData
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
            utlandAntallAar = source.utlandAntallAar,
            utlandPeriodeListe = source.utlandPeriodeListe,
            fremtidigInntektListe = source.fremtidigInntektListe,
            brukFremtidigInntekt = source.brukFremtidigInntekt,
            inntektOver1GAntallAar = source.inntektOver1GAntallAar,
            flyktning = source.flyktning,
            epsHarInntektOver2G = source.epsHarInntektOver2G,
            rettTilOffentligAfpFom = source.rettTilOffentligAfpFom,
            pre2025OffentligAfp = source.pre2025OffentligAfp,
            erAnonym = source.erAnonym,
            ignoreAvslag = source.ignoreAvslag,
            isHentPensjonsbeholdninger = source.isHentPensjonsbeholdninger,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = source.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter,
            onlyVilkaarsproeving = source.onlyVilkaarsproeving,
            epsKanOverskrives = source.epsKanOverskrives,
            registerData = source.registerData
        )

    private fun naermesteLavereUttaksgrad(grad: UttakGradKode) =
        when (grad) {
            UttakGradKode.P_0 -> UttakGradKode.P_0
            UttakGradKode.P_20 -> UttakGradKode.P_0
            UttakGradKode.P_40 -> UttakGradKode.P_20
            UttakGradKode.P_50 -> UttakGradKode.P_40
            UttakGradKode.P_60 -> UttakGradKode.P_50
            UttakGradKode.P_80 -> UttakGradKode.P_60
            UttakGradKode.P_100 -> UttakGradKode.P_80
        }
}
