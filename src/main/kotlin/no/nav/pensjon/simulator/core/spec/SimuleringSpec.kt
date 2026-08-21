package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.alder.PensjonAlderDato
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.inntekt.InntektUtil.heltUttakInntektTom
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.RegisterData
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import no.nav.pensjon.simulator.uttak.Uttaksgrad.HUNDRE_PROSENT
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

    @Deprecated("Bruk perioden f.o.m. heltUttakDato t.o.m. inntektEtterHeltUttakTom")
    val inntektEtterHeltUttakAntallAar: Int?,

    val inntektEtterHeltUttakTom: LocalDate?,
    val foedselAar: Int,
    val utlandAntallAar: Int, // PEN: SimuleringEtter2011.utenlandsopphold
    val utlandPeriodeListe: MutableList<UtlandPeriode>,
    val fremtidigInntektListe: MutableList<FremtidigInntekt>,
    val brukFremtidigInntekt: Boolean,
    val inntektOver1GAntallAar: Int,
    val flyktning: Boolean?,
    val epsHarInntektOver2G: Boolean,
    val livsvarigOffentligAfp: LivsvarigOffentligAfpSpec?,
    val tidsbegrensetOffentligAfp: TidsbegrensetOffentligAfpSpec?,
    val erAnonym: Boolean, // støtter uinnlogget kalkulator
    val ignoreAvslag: Boolean, // simulering fullføres selv med for lav opptjening/trygdetid
    val isHentPensjonsbeholdninger: Boolean,
    val isOutputSimulertBeregningsinformasjonForAllKnekkpunkter: Boolean,
    val onlyVilkaarsproeving: Boolean,
    val epsKanOverskrives: Boolean,
    val tillatSenereFoersteuttakForUfoere: Boolean = false,
    val registerData: RegisterData? = null
) {
    init {
        if (erAnonym) require(foedselAar > 0) { "For anonym simulering må fødselsår være angitt" }
        else require(pid != null) { "For personlig simulering må person-ID (pid) være angitt" }
    }

    // PEN: SimuleringEtter2011.isBoddIUtlandet()
    val brukSoekersUtenlandsperioder: Boolean = utlandPeriodeListe.isNotEmpty()

    val erHeltUttak: Boolean = uttakGrad == UttakGradKode.P_100

    val gjelderPrivatAfp: Boolean =
        EnumSet.of(
            SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT
        ).contains(type)

    /**
     * Uføretrygd må avsluttes før uttak av pensjon i følgende tilfeller:
     * - Ved uttak (helt eller gradert) av alderspensjon i kombinasjon med privat AFP
     * - Ved helt uttak av alderspensjon
     */
    val kreverAvsluttetUfoeretrygd: Boolean =
        gjelderPrivatAfp || simuleringstyperSomVedHeltUttakKreverAvsluttetUfoeretrygd.contains(type) && erHeltUttak

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

        val uttakFom = PensjonAlderDato(foedselDato!!, uttakDato)

        return HeltUttakSimuleringSpec(
            uttakFom = uttakFom,
            aarligInntektBeloep = inntektEtterHeltUttakBeloep,
            inntektTom = inntektEtterHeltUttakTom?.let { PensjonAlderDato(foedselDato, it) } ?: uttakFom
        )
    }

    fun heltUttak(heltUttakFom: PensjonAlderDato) =
        HeltUttakSimuleringSpec(
            uttakFom = heltUttakFom,
            aarligInntektBeloep = inntektEtterHeltUttakBeloep,
            inntektTom = inntektEtterHeltUttakTom?.let { PensjonAlderDato(foedselDato!!, it) } ?: heltUttakFom
        )

    fun withAvdoed(avdoed: Avdoed) =
        copy(
            avdoed = avdoed,
            type = if (type == SimuleringTypeEnum.ENDR_ALDER) SimuleringTypeEnum.ENDR_ALDER_M_GJEN else type
        )

    fun withUttak(
        foersteUttakDato: LocalDate?,
        uttaksgrad: UttakGradKode,
        heltUttakDato: LocalDate?,
        inntektEtterHeltUttakTom: LocalDate?,
        inntektEtterHeltUttakAntallAar: Int?
    ) =
        copy(
            foersteUttakDato = foersteUttakDato,
            uttakGrad = uttaksgrad,
            heltUttakDato = heltUttakDato,
            inntektEtterHeltUttakTom = inntektEtterHeltUttakTom,
            inntektEtterHeltUttakAntallAar = inntektEtterHeltUttakAntallAar
        )

    fun withFoersteUttakDato(dato: LocalDate?) =
        withUttak(
            foersteUttakDato = dato,
            uttakGrad,
            heltUttakDato,
            inntektEtterHeltUttakTom = heltUttakInntektTom(
                foersteUttakDato = dato,
                heltUttakDato,
                inntektEtterHeltUttakAntallAar
            ),
            inntektEtterHeltUttakAntallAar = inntektEtterHeltUttakAntallAar
        )

    fun withHeltUttakDato(dato: LocalDate?) =
        withUttak(
            foersteUttakDato,
            uttakGrad,
            heltUttakDato = dato,
            inntektEtterHeltUttakTom = heltUttakInntektTom(
                foersteUttakDato = dato,
                heltUttakDato,
                inntektEtterHeltUttakAntallAar
            ),
            inntektEtterHeltUttakAntallAar = inntektEtterHeltUttakAntallAar
        )

    fun gjelderLivsvarigAfp() =
        gjelderPrivatAfp || gjelderLivsvarigOffentligAfp()

    fun gjelderLivsvarigOffentligAfp() =
        EnumSet.of(
            SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
            SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG
        ).contains(type)

    fun gjelderTidsbegrensetOffentligAfp() =
        // NB: Simuleringstype AFP_FPP har ingen variant for endring av pensjon
        gjelderTidsbegrensetOffentligAfpEtterfulgtAvAlderspensjon() || type == SimuleringTypeEnum.AFP_FPP

    /**
     * "2-fase-simulering" er simulering som innbefatter to forskjellige pensjonsuttak, separert i tid.
     * Uttrykket brukes for:
     * - Gradert uttak (eller 0 %) etterfulgt av helt uttak
     * - Offentlig AFP (før 2025) etterfulgt av alderspensjon
     */
    fun gjelder2FaseSimulering() =
        gjelderTidsbegrensetOffentligAfpEtterfulgtAvAlderspensjon() || uttakErGradertEllerNull()

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
     * Pensjonstyper som ikke kan tas ut sammen med tidsbegrenset offentlig AFP.
     * Dersom personen har slik løpende AFP, må denne termineres før annet uttak kan starte.
     */
    fun kreverTermineringAvTidsbegrensetOffentligAfp() =
        EnumSet.of(
            SimuleringTypeEnum.ALDER,
            SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            SimuleringTypeEnum.ALDER_M_GJEN
        ).contains(type)

    fun uttak(): UttakSpec {
        val uttaksgrad = uttakGrad.value.toInt()
        val gradert = uttaksgrad < HUNDRE_PROSENT.prosentsats

        return UttakSpec(
            uttaksgrad,
            gradert,
            foersteUttakFom = foersteUttakDato!!,
            andreUttakFom = if (gradert) heltUttakDato else null
        )
    }

    fun hasSameUttakAs(other: SimuleringSpec) =
        uttakGrad == other.uttakGrad &&
                (foersteUttakDato?.equals(other.foersteUttakDato) ?: (other.foersteUttakDato == null)) &&
                (heltUttakDato?.equals(other.heltUttakDato) ?: (other.heltUttakDato == null))

    /**
     * For 'tidsbegrenset offentlig AFP etterfulgt av alderspensjon' gjelder:
     * - foersteUttakDato = uttak av AFP
     * - heltUttakDato = uttak av alderspensjon
     * Det er alderspensjonsuttaket (og dermed heltUttakDato) som er relevant for trygdetiden her
     */
    fun foersteAlderspensjonUttaksdato(): LocalDate? =
        if (gjelderTidsbegrensetOffentligAfpEtterfulgtAvAlderspensjon())
            heltUttakDato ?: foersteUttakDato // bruker foersteUttakDato som 'backup'-dato
        else
            foersteUttakDato

    private fun gjelderTidsbegrensetOffentligAfpEtterfulgtAvAlderspensjon() =
        // NB: Simuleringstype AFP_ETTERF_ALDER har ingen variant for endring av pensjon
        type == SimuleringTypeEnum.AFP_ETTERF_ALDER

    private companion object {

        private val simuleringstyperSomVedHeltUttakKreverAvsluttetUfoeretrygd: Set<SimuleringTypeEnum> =
            setOf(
                SimuleringTypeEnum.ALDER,
                SimuleringTypeEnum.ALDER_M_GJEN,
                SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
                SimuleringTypeEnum.ENDR_ALDER,
                SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG
            )

        //TODO move to UttakGradKode?
        private fun isGradert(grad: UttakGradKode) =
            grad != UttakGradKode.P_0 && grad != UttakGradKode.P_100

        // PEN: SimulerFleksibelAPCommand.isUttaksgradLessThan100Percent
        private fun isGradertOrZero(grad: UttakGradKode) =
            grad != UttakGradKode.P_100
    }
}
