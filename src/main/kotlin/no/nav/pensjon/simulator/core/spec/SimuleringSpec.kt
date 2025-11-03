package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.alder.PensjonAlderDato
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.RegisterData
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate
import java.util.*

// PEN: no.nav.domain.pensjon.kjerne.simulering.SimuleringEtter2011
data class SimuleringSpec(
    val type: SimuleringTypeEnum,
    val sivilstatus: SivilstatusType,
    var epsHarPensjon: Boolean,
    val foersteUttakDato: LocalDate?,
    val heltUttakDato: LocalDate?, // null for ugradert uttak
    val pid: Pid?, // null for anonym simulering
    val foedselDato: LocalDate?, // null for anonym simulering
    val avdoed: Avdoed?, // for ENDR_ALDER_M_GJEN
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
    val pre2025OffentligAfp: Pre2025OffentligAfpSpec?, // for "gammel" AFP i offentlig sektor
    val erAnonym: Boolean, // støtter uinnlogget kalkulator
    val ignoreAvslag: Boolean, // simulering fullføres selv med for lav opptjening/trygdetid
    val isHentPensjonsbeholdninger: Boolean,
    val isOutputSimulertBeregningsinformasjonForAllKnekkpunkter: Boolean,
    val onlyVilkaarsproeving: Boolean,
    val epsKanOverskrives: Boolean,
    val registerData: RegisterData? = null
) {
    init {
        if (erAnonym) require(foedselAar > 0) { "For anonym simulering må fødselsår være angitt" }
        else require(pid != null) { "For personlig simulering må person-ID (pid) være angitt" }
    }

    // PEN: SimuleringEtter2011.isBoddIUtlandet()
    val boddUtenlands: Boolean = utlandPeriodeListe.isNotEmpty()

    val limitedUtenlandsoppholdAntallAar: Int =
        if (utlandAntallAar < 1 && utlandPeriodeListe.isNotEmpty() && foedselDato != null)
            UtlandPeriodeConverter.limitedAntallAar(utlandPeriodeListe, foedselDato)
        else
            utlandAntallAar

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

    fun withAvdoed(avdoedPid: Pid, doedsdato: LocalDate) =
        SimuleringSpec(
            type = if (type == SimuleringTypeEnum.ENDR_ALDER) SimuleringTypeEnum.ENDR_ALDER_M_GJEN else type,
            sivilstatus = sivilstatus,
            epsHarPensjon = epsHarPensjon,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = heltUttakDato,
            pid = pid,
            foedselDato = foedselDato,
            avdoed = Avdoed(
                pid = avdoedPid,
                antallAarUtenlands = 0,
                inntektFoerDoed = 0,
                doedDato = doedsdato,
                erMedlemAvFolketrygden = false,
                harInntektOver1G = false
            ),
            isTpOrigSimulering = isTpOrigSimulering,
            simulerForTp = simulerForTp,
            uttakGrad = uttakGrad,
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
            epsKanOverskrives = epsKanOverskrives,
            registerData = registerData
        )

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
            epsKanOverskrives = epsKanOverskrives,
            registerData = registerData
        )

    fun withFoersteUttakDato(dato: LocalDate?) =
        withUttak(foersteUttakDato = dato, uttakGrad, heltUttakDato, inntektEtterHeltUttakAntallAar)

    fun withHeltUttakDato(dato: LocalDate?) =
        withUttak(foersteUttakDato, uttakGrad, heltUttakDato = dato, inntektEtterHeltUttakAntallAar)

    fun gjelderLivsvarigAfp() =
        gjelderPrivatAfp() || gjelderLivsvarigOffentligAfp()

    fun gjelderLivsvarigOffentligAfp() =
        EnumSet.of(
            SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
            SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG
        ).contains(type)

    fun gjelderPre2025OffentligAfp() =
        // NB: Simuleringstype AFP_FPP har ingen variant for endring av pensjon
        gjelderPre2025OffentligAfpEtterfulgtAvAlderspensjon() || type == SimuleringTypeEnum.AFP_FPP

    fun gjelderPrivatAfp() =
        EnumSet.of(
            SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT
        ).contains(type)

    /**
     * "2-fase-simulering" er simulering som innbefatter to forskjellige pensjonsuttak, separert i tid.
     * Uttrykket brukes for:
     * - Gradert uttak (eller 0 %) etterfulgt av helt uttak
     * - Offentlig AFP (før 2025) etterfulgt av alderspensjon
     */
    fun gjelder2FaseSimulering() =
        gjelderPre2025OffentligAfpEtterfulgtAvAlderspensjon() || uttakErGradertEllerNull()

    fun gjelderEndring() =
        EnumSet.of(
            SimuleringTypeEnum.ENDR_ALDER,
            SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT,
            SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG,
            SimuleringTypeEnum.ENDR_ALDER_M_GJEN
        ).contains(type)

    fun gjelderEndringUtenLivsvarigOffentligAfp() =
        EnumSet.of(
            SimuleringTypeEnum.ENDR_ALDER,
            SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT,
            SimuleringTypeEnum.ENDR_ALDER_M_GJEN
        ).contains(type)

    /**
     * Pensjonstyper som ikke kan tas ut sammen med "gammel" (pre-2025) offentlig AFP.
     * Dersom personen har slik løpende AFP, må denne termineres før annet uttak kan starte.
     */
    fun kreverTermineringAvPre2025OffentligAfp() =
        EnumSet.of(
            SimuleringTypeEnum.ALDER,
            SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            SimuleringTypeEnum.ALDER_M_GJEN
        ).contains(type)

    fun hasSameUttakAs(other: SimuleringSpec) =
        uttakGrad == other.uttakGrad &&
                (foersteUttakDato?.equals(other.foersteUttakDato) ?: (other.foersteUttakDato == null)) &&
                (heltUttakDato?.equals(other.heltUttakDato) ?: (other.heltUttakDato == null))

    /**
     * For 'pre-2025 offentlig AFP etterfulgt av alderspensjon' gjelder:
     * - foersteUttakDato = uttak av AFP
     * - heltUttakDato = uttak av alderspensjon
     * Det er alderspensjonsuttaket (og dermed heltUttakDato) som er relevant for trygdetiden her
     */
    fun foersteAlderspensjonUttaksdato(): LocalDate? =
        if (gjelderPre2025OffentligAfpEtterfulgtAvAlderspensjon())
            heltUttakDato ?: foersteUttakDato // bruker foersteUttakDato som 'backup'-dato
        else
            foersteUttakDato

    private fun gjelderPre2025OffentligAfpEtterfulgtAvAlderspensjon() =
        // NB: Simuleringstype AFP_ETTERF_ALDER har ingen variant for endring av pensjon
        type == SimuleringTypeEnum.AFP_ETTERF_ALDER

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
    val afpOrdning: AFPtypeEnum, // Hvilken AFP-ordning bruker er tilknyttet
    val inntektMaanedenFoerAfpUttakBeloep: Int, // Brukers inntekt måneden før uttak av AFP
    val inntektUnderAfpUttakBeloep: Int
)
