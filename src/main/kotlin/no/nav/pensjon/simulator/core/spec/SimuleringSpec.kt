package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.alder.PensjonAlderDato
import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

// no.nav.domain.pensjon.kjerne.simulering.SimuleringEtter2011 &
// SimuleringSpecAlderspensjon1963Plus
data class SimuleringSpec(
    val type: SimuleringType,
    val sivilstatus: SivilstatusType,
    var epsHarPensjon: Boolean,
    val foersteUttakDato: LocalDate?,
    val heltUttakDato: LocalDate?,
    val pid: Pid?, // null for forenklet simulering
    val foedselDato: LocalDate?, // null for forenklet simulering
    val avdoed: Avdoed?,
    val isTpOrigSimulering: Boolean,
    var simulerForTp: Boolean,
    val uttakGrad: UttakGradKode,
    val forventetInntektBeloep: Int,
    val inntektUnderGradertUttakBeloep: Int,
    val inntektEtterHeltUttakBeloep: Int,
    val inntektEtterHeltUttakAntallAar: Int?,
    val foedselAar: Int,
    val boddUtenlands: Boolean,
    val utlandAntallAar: Int,
    val utlandPeriodeListe: MutableList<UtlandPeriode>,
    val fremtidigInntektListe: MutableList<FremtidigInntekt>,
    val inntektOver1GAntallAar: Int,
    val flyktning: Boolean?,
    val epsHarInntektOver2G: Boolean,
    val rettTilOffentligAfpFom: LocalDate?,
    val afpOrdning: AfpOrdningType? = null, // Hvilken AFP-ordning bruker er tilknyttet (kun for simulering av pre-2025 offentlig AFP)
    val afpInntektMaanedFoerUttak: Int? = null, // Brukers inntekt måneden før uttak av AFP (kun for simulering av pre-2025 offentlig AFP)
    val erAnonym: Boolean,
    val isHentPensjonsbeholdninger: Boolean = false,
    val isOutputSimulertBeregningsinformasjonForAllKnekkpunkter: Boolean = false
) {

    fun isGradert() = isGradert(uttakGrad)

    fun gradertUttak(foedselDato: LocalDate): GradertUttakSimuleringSpec? =
        if (isGradert())
            GradertUttakSimuleringSpec(
                grad = uttakGrad,
                uttakFom = foersteUttakDato?.let { PensjonAlderDato(it, foedselDato) }
                    ?: throw IllegalArgumentException("gradertUttak.uttakFomAlder undefined"),
                aarligInntektBeloep = inntektUnderGradertUttakBeloep
            )
        else
            null

    fun gradertUttak(
        foersteUttakFom: PensjonAlderDato,
        uttakGrad: UttakGradKode
    ): GradertUttakSimuleringSpec? =
        if (isGradert(uttakGrad))
            GradertUttakSimuleringSpec(
                grad = uttakGrad,
                uttakFom = foersteUttakFom,
                aarligInntektBeloep = inntektUnderGradertUttakBeloep
            )
        else
            null

    fun heltUttak(foedselDato: LocalDate): HeltUttakSimuleringSpec {
        val uttakDato: LocalDate =
            heltUttakDato ?: foersteUttakDato ?: throw IllegalArgumentException("Ingen uttaksdato definert")
        val inntektAntallAar = inntektEtterHeltUttakAntallAar?.toLong() ?: 0L

        return HeltUttakSimuleringSpec(
            uttakFom = PensjonAlderDato(uttakDato, foedselDato),
            aarligInntektBeloep = inntektEtterHeltUttakBeloep,
            inntektTom = PensjonAlderDato(uttakDato.plusYears(inntektAntallAar), foedselDato)
        )
    }

    fun heltUttak(foedselDato: LocalDate, heltUttakFom: PensjonAlderDato): HeltUttakSimuleringSpec {
        val inntektAntallAar = inntektEtterHeltUttakAntallAar?.toLong() ?: 0L

        return HeltUttakSimuleringSpec(
            uttakFom = heltUttakFom,
            aarligInntektBeloep = inntektEtterHeltUttakBeloep,
            inntektTom = PensjonAlderDato(heltUttakFom.dato.plusYears(inntektAntallAar), foedselDato)
        )
    }

    fun withUttak(
        foersteUttakDato: LocalDate?,
        uttaksgrad: UttakGradKode,
        heltUttakDato: LocalDate?,
        inntektEtterHeltUttakAntallAar: Int?
    ) =
        SimuleringSpec(
            type,
            sivilstatus,
            epsHarPensjon,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = heltUttakDato,
            pid,
            foedselDato,
            avdoed,
            isTpOrigSimulering,
            simulerForTp,
            uttakGrad = uttaksgrad,
            forventetInntektBeloep,
            inntektUnderGradertUttakBeloep,
            inntektEtterHeltUttakBeloep,
            inntektEtterHeltUttakAntallAar = inntektEtterHeltUttakAntallAar,
            foedselAar,
            boddUtenlands,
            utlandAntallAar,
            utlandPeriodeListe,
            fremtidigInntektListe,
            inntektOver1GAntallAar,
            flyktning,
            epsHarInntektOver2G,
            rettTilOffentligAfpFom,
            afpOrdning,
            afpInntektMaanedFoerUttak,
            erAnonym,
            isHentPensjonsbeholdninger,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter
        )

    fun withHeltUttakDato(dato: LocalDate?) =
        withUttak(foersteUttakDato, uttakGrad, heltUttakDato = dato, inntektEtterHeltUttakAntallAar)

    fun gjelderPre2025OffentligAfp() =
        type == SimuleringType.AFP_ETTERF_ALDER

    fun gjelderPrivatAfpFoersteUttak() =
        type == SimuleringType.ALDER_M_AFP_PRIVAT

    //TODO move to SimuleringType?
    fun gjelderEndring() =
        type == SimuleringType.ENDR_ALDER ||
                type == SimuleringType.ENDR_AP_M_AFP_PRIVAT ||
                type == SimuleringType.ENDR_ALDER_M_GJEN

    private companion object {
        //TODO move to UttakGradKode?
        private fun isGradert(grad: UttakGradKode) =
            grad != UttakGradKode.P_0 && grad != UttakGradKode.P_100
    }
}
