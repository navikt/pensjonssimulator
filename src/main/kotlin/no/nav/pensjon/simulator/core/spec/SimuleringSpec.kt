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
import java.util.*

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
    val inntektUnderGradertUttakBeloep: Int, // NB: For AFP_ETTERF_ALDER this is inntekt during AFP-uttak
    val inntektEtterHeltUttakBeloep: Int,
    val inntektEtterHeltUttakAntallAar: Int?,
    val foedselAar: Int,
    val utlandAntallAar: Int, // PEN: SimuleringEtter2011.utenlandsopphold
    val utlandPeriodeListe: MutableList<UtlandPeriode>,
    val fremtidigInntektListe: MutableList<FremtidigInntekt>,
    val brukFremtidigInntekt: Boolean,
    val inntektOver1GAntallAar: Int,
    val flyktning: Boolean?,
    val epsHarInntektOver2G: Boolean,
    val rettTilOffentligAfpFom: LocalDate?,
    val pre2025OffentligAfp: Pre2025OffentligAfpSpec?,
    val erAnonym: Boolean,
    val ignoreAvslag: Boolean, //TODO Sett ignoreAvslag = true hvis simulering alderspensjon for folketrygdbeholdning
    val isHentPensjonsbeholdninger: Boolean,
    val isOutputSimulertBeregningsinformasjonForAllKnekkpunkter: Boolean,
    val onlyVilkaarsproeving: Boolean,
    val epsKanOverskrives: Boolean
) {
    // PEN: SimuleringEtter2011.isBoddIUtlandet()
    val boddUtenlands: Boolean = utlandPeriodeListe.isNotEmpty()

    fun isGradert() = isGradert(uttakGrad)

    fun uttakErGradertEllerNull() = isGradertOrZero(uttakGrad)

    fun gradertUttak(): GradertUttakSimuleringSpec? =
        if (isGradert())
            GradertUttakSimuleringSpec(
                grad = uttakGrad,
                uttakFom = foersteUttakDato?.let { PensjonAlderDato(foedselDato!!, dato = it) }
                    ?: throw IllegalArgumentException("gradertUttak.uttakFomAlder undefined"),
                aarligInntektBeloep = inntektUnderGradertUttakBeloep
            )
        else
            null

    fun gradertUttak(
        foersteUttakFom: PensjonAlderDato,
        uttaksgrad: UttakGradKode
    ): GradertUttakSimuleringSpec? =
        if (isGradert(uttaksgrad))
            GradertUttakSimuleringSpec(
                grad = uttaksgrad,
                uttakFom = foersteUttakFom,
                aarligInntektBeloep = inntektUnderGradertUttakBeloep
            )
        else
            null

    fun heltUttak(): HeltUttakSimuleringSpec {
        val uttakDato: LocalDate =
            if (uttakGrad == UttakGradKode.P_100)
            // Kun helt uttak: Bare foersteUttakDato bør være definert
                foersteUttakDato ?: heltUttakDato
                ?: throw IllegalArgumentException("Ingen uttaksdato definert for ugradert uttak")
            else
            // Gradert uttak fulgt av helt uttak: heltUttakDato brukes for 100%-uttaket
                heltUttakDato ?: foersteUttakDato ?: throw IllegalArgumentException("Ingen uttaksdato definert")

        val inntektAntallAar = inntektEtterHeltUttakAntallAar?.toLong() ?: 0L

        return HeltUttakSimuleringSpec(
            uttakFom = PensjonAlderDato(foedselDato!!, uttakDato),
            aarligInntektBeloep = inntektEtterHeltUttakBeloep,
            inntektTom = PensjonAlderDato(foedselDato, uttakDato.plusYears(inntektAntallAar)),
        )
    }

    fun heltUttak(heltUttakFom: PensjonAlderDato): HeltUttakSimuleringSpec {
        val inntektAntallAar = inntektEtterHeltUttakAntallAar?.toLong() ?: 0L

        return HeltUttakSimuleringSpec(
            uttakFom = heltUttakFom,
            aarligInntektBeloep = inntektEtterHeltUttakBeloep,
            inntektTom = PensjonAlderDato(foedselDato!!, heltUttakFom.dato.plusYears(inntektAntallAar))
        )
    }

    fun withUttak(
        foersteUttakDato: LocalDate?,
        uttaksgrad: UttakGradKode,
        heltUttakDato: LocalDate?,
        inntektEtterHeltUttakAntallAar: Int?
    ) =
        SimuleringSpec(
            type = type,
            sivilstatus = sivilstatus,
            epsHarPensjon = epsHarPensjon,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = heltUttakDato,
            pid = pid,
            foedselDato = foedselDato,
            avdoed = avdoed,
            isTpOrigSimulering = isTpOrigSimulering,
            simulerForTp = simulerForTp,
            uttakGrad = uttaksgrad,
            forventetInntektBeloep = forventetInntektBeloep,
            inntektUnderGradertUttakBeloep = inntektUnderGradertUttakBeloep,
            inntektEtterHeltUttakBeloep = inntektEtterHeltUttakBeloep,
            inntektEtterHeltUttakAntallAar = inntektEtterHeltUttakAntallAar,
            foedselAar = foedselAar,
            utlandAntallAar = utlandAntallAar,
            utlandPeriodeListe = utlandPeriodeListe,
            fremtidigInntektListe = fremtidigInntektListe,
            brukFremtidigInntekt = brukFremtidigInntekt,
            inntektOver1GAntallAar = inntektOver1GAntallAar,
            flyktning = flyktning,
            epsHarInntektOver2G = epsHarInntektOver2G,
            rettTilOffentligAfpFom = rettTilOffentligAfpFom,
            pre2025OffentligAfp = pre2025OffentligAfp,
            erAnonym = erAnonym,
            ignoreAvslag = ignoreAvslag,
            isHentPensjonsbeholdninger = isHentPensjonsbeholdninger,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = isOutputSimulertBeregningsinformasjonForAllKnekkpunkter,
            onlyVilkaarsproeving = onlyVilkaarsproeving,
            epsKanOverskrives = epsKanOverskrives
        )

    fun withFoersteUttakDato(dato: LocalDate?) =
        withUttak(foersteUttakDato = dato, uttakGrad, heltUttakDato, inntektEtterHeltUttakAntallAar)

    fun withHeltUttakDato(dato: LocalDate?) =
        withUttak(foersteUttakDato, uttakGrad, heltUttakDato = dato, inntektEtterHeltUttakAntallAar)

    fun gjelderPre2025OffentligAfp() =
        EnumSet.of(SimuleringType.AFP_ETTERF_ALDER, SimuleringType.AFP_FPP).contains(type)

    fun gjelderPre2025OffentligAfpEtterfulgtAvAlderspensjon() =
        type == SimuleringType.AFP_ETTERF_ALDER

    /**
     * "2-fase-simulering" er simulering som innbefatter to forskjellige pensjonsuttak,
     * separert i tid. Uttrykket brukes for:
     * - Gradert uttak (eller 0 %) etterfulgt av helt uttak
     * - Offentlig AFP (før 2025) etterfulgt av alderspensjon
     */
    fun gjelder2FaseSimulering() =
        gjelderPre2025OffentligAfpEtterfulgtAvAlderspensjon() || uttakErGradertEllerNull()

    fun gjelderPrivatAfpFoersteUttak() =
        type == SimuleringType.ALDER_M_AFP_PRIVAT

    //TODO move to SimuleringType?
    fun gjelderEndring() =
        type == SimuleringType.ENDR_ALDER ||
                type == SimuleringType.ENDR_AP_M_AFP_PRIVAT ||
                type == SimuleringType.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG ||
                type == SimuleringType.ENDR_ALDER_M_GJEN

    //TODO move to SimuleringType?
    fun gjelderAfp() =
        type == SimuleringType.ALDER_M_AFP_PRIVAT ||
                type == SimuleringType.ENDR_AP_M_AFP_PRIVAT ||
                type == SimuleringType.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG ||
                type == SimuleringType.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG

    fun hasSameUttakAs(other: SimuleringSpec) =
        uttakGrad == other.uttakGrad &&
                foersteUttakDato == other.foersteUttakDato &&
                heltUttakDato == other.heltUttakDato

    private companion object {
        //TODO move to UttakGradKode?
        private fun isGradert(grad: UttakGradKode) =
            grad != UttakGradKode.P_0 && grad != UttakGradKode.P_100

        // PEN: SimulerFleksibelAPCommand.isUttaksgradLessThan100Percent
        private fun isGradertOrZero(grad: UttakGradKode) =
            grad != UttakGradKode.P_100
    }
}

data class Pre2025OffentligAfpSpec(
    val afpOrdning: AfpOrdningType, // Hvilken AFP-ordning bruker er tilknyttet
    val inntektMaanedenFoerAfpUttakBeloep: Int, // Brukers inntekt måneden før uttak av AFP
    val inntektUnderAfpUttakBeloep: Int,
)
