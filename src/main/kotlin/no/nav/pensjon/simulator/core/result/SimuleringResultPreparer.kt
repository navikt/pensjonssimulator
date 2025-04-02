package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.findElementOfType
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.sortedSubset
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.ETERNITY
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.MAANEDER_PER_AAR
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.calculateAgeInYears
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.firstDayOfMonthAfterUserTurnsGivenAge
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getFirstDateInYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getFirstDayOfMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getLastDayOfMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.intersects
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.intersectsWithPossiblyOpenEndings
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.ubetingetPensjoneringDato
import no.nav.pensjon.simulator.core.util.PeriodeUtil
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findLatest
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findValidForDate
import no.nav.pensjon.simulator.core.util.PeriodeUtil.numberOfMonths
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.core.util.toNorwegianNoon
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OpprettOutputHelper
@Component
class SimuleringResultPreparer(private val time: Time) {

    fun opprettOutput(preparerSpec: ResultPreparerSpec): SimulatorOutput {
        val kravhode = preparerSpec.kravhode
        val simuleringSpec = preparerSpec.simuleringSpec
        val soekerGrunnlag = kravhode.hentPersongrunnlagForSoker()
        val grunnbeloep = preparerSpec.grunnbeloep

        // If simulation is called from eksterne ordninger, then force the output to be mapped as
        // Kap 19 (2011). This line of code should be removed as soon as they're able to receive Kap 20 results.
        forceKap19OutputIfSimulerForTp(kravhode, simuleringSpec)

        // Del 1
        val simuleringResult = createAndMapSimuleringEtter2011Resultat(simuleringSpec, soekerGrunnlag, grunnbeloep)

        // Del 2
        createAndMapSimulertOpptjeningListe(
            simuleringResult,
            preparerSpec.alderspensjonBeregningResultatListe,
            soekerGrunnlag,
            kravhode
        )

        // Del 3
        createAndMapSimulertAlderspensjon(
            simuleringResult,
            simuleringSpec,
            kravhode,
            soekerGrunnlag,
            preparerSpec.alderspensjonBeregningResultatListe,
            preparerSpec.forrigeAlderspensjonBeregningResultat,
            preparerSpec.pensjonBeholdningPeriodeListe,
            preparerSpec.outputSimulertBeregningsInformasjonForAllKnekkpunkter
        )

        // Del 4
        if (preparerSpec.privatAfpBeregningResultatListe.isNotEmpty() || preparerSpec.forrigePrivatAfpBeregningResultat != null) {
            createAndMapSimulertPrivatAfpPeriodeListe(
                simuleringResult,
                simuleringSpec,
                preparerSpec.privatAfpBeregningResultatListe,
                soekerGrunnlag,
                preparerSpec.forrigePrivatAfpBeregningResultat
            )
        }

        // Del 5
        simuleringResult.pre2025OffentligAfp = preparerSpec.pre2025OffentligAfpBeregningResultat
        simuleringResult.livsvarigOffentligAfp = preparerSpec.livsvarigOffentligAfpBeregningResultatListe
        simuleringResult.sisteGyldigeOpptjeningAar = preparerSpec.sisteGyldigeOpptjeningAar
        return simuleringResult
    }

    // OpprettOutputHelper.addPensjonsperioderToSimulertAlder
    private fun addPensjonsperioderToSimulertAlder(
        simulertAlderspensjon: SimulertAlderspensjon,
        spec: SimuleringSpec,
        soekerGrunnlag: Persongrunnlag,
        forrigeResultat: AbstraktBeregningsResultat?,
        resultatListe: MutableList<AbstraktBeregningsResultat>
    ) {
        val foedselsdato = soekerGrunnlag.fodselsdato!!.toNorwegianLocalDate()
        val startAlder = calculateStartAlder(spec, foedselsdato, forrigeResultat, true)
        var forrigeResultatCopy: AbstraktBeregningsResultat? = null

        if (forrigeResultat != null) {
            // Put a copy of the løpende beregningsresultat on the list. Remove ET/BT from totalbeløp and set fom/tom
            forrigeResultatCopy = modifiedCopyOfBeregningsresultat(forrigeResultat, resultatListe, foedselsdato)
            resultatListe.add(forrigeResultatCopy)
        }

        for (alder in startAlder..MAX_KNEKKPUNKT_ALDER) {
            val beloepPeriode: BeloepPeriode = beloepPeriode(foedselsdato, alder, resultatListe)
            simulertAlderspensjon.addPensjonsperiode(pensjonPeriode(alder, beloepPeriode.beloep, beloepPeriode.maanedsutbetalinger))
        }

        forrigeResultatCopy?.let {
            // Add a periode representing løpende ytelser (tagged with alder=null)
            val maanedsbeloepVedPeriodeStart = it.pensjonUnderUtbetaling?.totalbelopNetto ?: 0
            val beloep = maanedsbeloepVedPeriodeStart * MAANEDER_PER_AAR
            simulertAlderspensjon.addPensjonsperiode(pensjonPeriode(
                null,
                beloep,
                listOf(Maanedsutbetaling(maanedsbeloepVedPeriodeStart, it.virkFom!!.toNorwegianLocalDate()))
            ))
        }
    }

    private fun createAndMapSimulertAlderspensjon(
        simulatorOutput: SimulatorOutput,
        simuleringSpec: SimuleringSpec,
        kravhode: Kravhode,
        soekerGrunnlag: Persongrunnlag,
        resultatListe: MutableList<AbstraktBeregningsResultat>,
        forrigeResultat: AbstraktBeregningsResultat?,
        pensjonBeholdningPeriodeListe: List<BeholdningPeriode>,
        outputSimulertBeregningInformasjonForAllKnekkpunkter: Boolean
    ) {

        // Del 3.1
        val simulertAlderspensjon = simulertAlderspensjonMedRegelverkAndel(kravhode, resultatListe)

        // Del 3.2
        addPensjonsperioderToSimulertAlder(
            simulertAlderspensjon,
            simuleringSpec,
            soekerGrunnlag,
            forrigeResultat,
            resultatListe
        )

        // Del 3.3
        addSimulertBeregningsinformasjonForKnekkpunkterToSimulertAlder(
            simulertAlderspensjon, simuleringSpec, kravhode, soekerGrunnlag, resultatListe, forrigeResultat,
            pensjonBeholdningPeriodeListe, outputSimulertBeregningInformasjonForAllKnekkpunkter
        )

        // Del 3.4
        simulertAlderspensjon.uttakGradListe = kravhode.uttaksgradListe
        simulatorOutput.alderspensjon = simulertAlderspensjon
    }

    private fun createAndMapSimulertPrivatAfpPeriodeListe(
        simulatorOutput: SimulatorOutput,
        simuleringSpec: SimuleringSpec,
        privatAfpBeregningResultatListe: MutableList<BeregningsResultatAfpPrivat>,
        soekerGrunnlag: Persongrunnlag,
        forrigeAfpBeregningResultat: BeregningsResultatAfpPrivat?
    ) {
        val foedselsdato: LocalDate? = soekerGrunnlag.fodselsdato?.toNorwegianLocalDate()
        val startAlder = calculateStartAlder(simuleringSpec, foedselsdato!!, forrigeAfpBeregningResultat, false)
        var forrigeAfpBeregningsresultatKopi: BeregningsResultatAfpPrivat? = null

        if (forrigeAfpBeregningResultat != null) {
            // Put a copy of the løpende beregningsresultat on the list. Adjust fom/tom.
            forrigeAfpBeregningsresultatKopi = modifiedCopyOfPrivatAfpBeregningResultat(
                beregningResultat = forrigeAfpBeregningResultat,
                resultatListe = privatAfpBeregningResultatListe,
                foedselsdato
            )
            privatAfpBeregningResultatListe.add(forrigeAfpBeregningsresultatKopi)
        }

        for (alder in startAlder..MAX_OPPTJENING_ALDER) {
            val beloepPeriode: BeloepPeriode = beloepPeriode(foedselsdato, alder, privatAfpBeregningResultatListe)
            val afpResultat: BeregningsResultatAfpPrivat? =
                findEarliestIntersecting(privatAfpBeregningResultatListe, beloepPeriode.start, beloepPeriode.slutt)

            afpResultat?.let {
                simulatorOutput.privatAfpPeriodeListe.add(
                    SimulatorOutputMapper.mapToSimulertAfpPrivatPeriode(
                        aarligBeloep = beloepPeriode.beloep,
                        resultat = it,
                        alder = alder
                    )
                )
            }
        }

        forrigeAfpBeregningsresultatKopi?.let {
            simulatorOutput.privatAfpPeriodeListe.add(
                SimulatorOutputMapper.mapToSimulertAfpPrivatPeriode(
                    aarligBeloep = it.pensjonUnderUtbetaling?.totalbelopNettoAr?.toInt() ?: 0,
                    resultat = it,
                    alder = 0 // github.com/navikt/pensjon-pen/pull/14903
                )
            )
        }
    }

    private fun calculateStartAlder(
        spec: SimuleringSpec,
        foedselsdato: LocalDate?,
        forrigeResultat: AbstraktBeregningsResultat?,
        handlePre2025OffentligAfpEtterfulgtAvAlderspensjon: Boolean
    ): Int {
        val alderVedFoersteUttak = calculateAgeInYears(foedselsdato, spec.foersteUttakDato)

        return if (forrigeResultat == null) {
            if (handlePre2025OffentligAfpEtterfulgtAvAlderspensjon && spec.gjelderPre2025OffentligAfp())
                calculateAgeInYears(foedselsdato, spec.heltUttakDato!!)
            else
                alderVedFoersteUttak
        } else {
            val alderToday = calculateAgeInYears(foedselsdato, time.today())
            if (alderVedFoersteUttak == alderToday) alderToday else alderToday + 1
        }
    }

    // OpprettOutputHelper.createModifiedCopyOfForrigeAlderBeregningsresultat
    private fun modifiedCopyOfBeregningsresultat(
        beregningResultat: AbstraktBeregningsResultat,
        resultatListe: List<AbstraktBeregningsResultat>,
        foedselsdato: LocalDate
    ): AbstraktBeregningsResultat {
        val alderToday: Int = calculateAgeInYears(foedselsdato, time.today())

        return copy(beregningResultat).apply {
            virkFom = firstDayOfMonthAfterUserTurnsGivenAge(foedselsdato, alderToday)
            virkTom = dayBefore(earliestVirkningFom(resultatListe))
            removeEktefelleAndBarnetilleggFromTotalbeloep(this)
        }
    }

    // OpprettOutputHelper.createModifiedCopyOfForrigeAfpBeregningsresultat
    private fun modifiedCopyOfPrivatAfpBeregningResultat(
        beregningResultat: BeregningsResultatAfpPrivat,
        resultatListe: List<BeregningsResultatAfpPrivat>,
        foedselsdato: LocalDate
    ): BeregningsResultatAfpPrivat {
        val alderToday: Int = calculateAgeInYears(foedselsdato, time.today())

        // Make a copy in order to preserve the original beregningResultat:
        return beregningResultat.copy().apply {
            virkFom = firstDayOfMonthAfterUserTurnsGivenAge(foedselsdato, alderToday)
            virkTom = if (resultatListe.isEmpty()) null else dayBefore(earliestVirkningFom(resultatListe))
        }
    }

    private companion object {
        private const val MAX_OPPTJENING_ALDER = 75
        private const val MAX_KNEKKPUNKT_ALDER = 77

        // TODO: Reconsider necessity for this
        private fun forceKap19OutputIfSimulerForTp(kravhode: Kravhode, spec: SimuleringSpec) {
            if (kravhode.regelverkTypeEnum == RegelverkTypeEnum.N_REG_N_OPPTJ && spec.simulerForTp) {
                // Set the regelverktype to 2011 so that the results are parsed and mapped as such
                kravhode.regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            }
        }

        private fun createAndMapSimuleringEtter2011Resultat(
            spec: SimuleringSpec,
            soekerGrunnlag: Persongrunnlag,
            grunnbeloep: Int
        ) =
            SimulatorOutputMapper.mapToSimulatorOutput(spec, soekerGrunnlag, grunnbeloep)

        // OpprettOutputHelper.createAndMapSimulertOpptjeningListe
        private fun createAndMapSimulertOpptjeningListe(
            simulatorOutput: SimulatorOutput,
            beregningResultatListe: List<AbstraktBeregningsResultat>,
            soekerGrunnlag: Persongrunnlag,
            kravhode: Kravhode
        ) {
            if (soekerGrunnlag.opptjeningsgrunnlagListe.isEmpty()) return

            var sisteBeregningsresultat2011: BeregningsResultatAlderspensjon2011? = null

            if (kravhode.regelverkTypeEnum == RegelverkTypeEnum.N_REG_G_OPPTJ) {
                sisteBeregningsresultat2011 = findLatest(beregningResultatListe) as BeregningsResultatAlderspensjon2011
            }

            if (kravhode.regelverkTypeEnum == RegelverkTypeEnum.N_REG_G_N_OPPTJ) {
                val sisteBeregningsresultat2016 =
                    findLatest(beregningResultatListe) as BeregningsResultatAlderspensjon2016
                sisteBeregningsresultat2011 = sisteBeregningsresultat2016.beregningsResultat2011
            }

            val poengtallListe: List<Poengtall>? =
                sisteBeregningsresultat2011?.beregningsInformasjonKapittel19?.spt?.poengrekke?.poengtallListe

            // Run from the year of the earliest opptjeningsgrunnlag to the year the user turns MAX_OPPTJENING_ALDER years of age
            val foersteAar: Int = findEarliest(soekerGrunnlag.opptjeningsgrunnlagListe)?.ar ?: return
            val sisteAar = yearUserTurnsGivenAge(soekerGrunnlag.fodselsdato!!, MAX_OPPTJENING_ALDER)

            for (aar in foersteAar..sisteAar) {
                simulatorOutput.opptjeningListe.add(
                    SimulatorOutputMapper.mapToSimulertOpptjening(
                        kalenderAar = aar,
                        resultatListe = beregningResultatListe,
                        soekerGrunnlag,
                        poengtallListe = poengtallListe.orEmpty(),
                        useNullAsDefaultPensjonspoeng = poengtallListe == null
                    )
                )
            }
        }

        // OpprettOutputHelper.setRatioKap19Kap20OnSimulertAlder
        private fun simulertAlderspensjonMedRegelverkAndel(
            kravhode: Kravhode,
            resultatListe: List<AbstraktBeregningsResultat>
        ) =
            SimulertAlderspensjon().apply {
                when (kravhode.regelverkTypeEnum) {
                    RegelverkTypeEnum.N_REG_G_OPPTJ -> {
                        // 2011
                        this.kapittel19Andel = 1.0
                        this.kapittel20Andel = 0.0
                    }

                    RegelverkTypeEnum.N_REG_N_OPPTJ -> {
                        // 2025
                        this.kapittel19Andel = 0.0
                        this.kapittel20Andel = 1.0
                    }

                    else -> {
                        // 2016
                        val beregningResultat2016 = resultatListe[0] as? BeregningsResultatAlderspensjon2016

                        // Andelen som kommer fra PREG er et heltall, for eksempel 5 betyr 50 %
                        // andelKapittel19. Må dele denne på 10 slik at man får desimaltall siden det forventes desimaltall i resten av
                        // løsning. Både andelDagens og andelNytt skal være mindre enn 1 for AP2016.
                        val kapittel19Andel = beregningResultat2016?.andelKapittel19?.let { it / 10.0 } ?: 0.0
                        this.kapittel19Andel = kapittel19Andel
                        this.kapittel20Andel = 1.0 - kapittel19Andel
                    }
                }
            }

    private fun <T : AbstraktBeregningsResultat> beloepPeriode(
        foedselsdato: LocalDate,
        alderAar: Int,
        resultatListe: MutableList<T>
    ): BeloepPeriode {
        val periodeStart: Date =
            getRelativeDateByMonth(getFirstDayOfMonth(getRelativeDateByYear(foedselsdato, alderAar)), 1)
        val periodeSlutt: Date = getLastDayOfMonth(getRelativeDateByYear(foedselsdato, alderAar + 1).toNorwegianDateAtNoon())
        var beloep = 0
        val maanedsutbetalinger = mutableListOf<Maanedsutbetaling>()
        for (resultat in resultatListe) {
            val fom: Date = resultat.virkFom!!.toNorwegianNoon()
            val tom: Date = resultat.virkTom?.toNorwegianNoon() ?: ETERNITY

            if (intersects(periodeStart, periodeSlutt, fom, tom, true)) {
                beloep += getBeloep(periodeStart, periodeSlutt, resultat, fom, tom)

                maanedsutbetalinger.add(
                    Maanedsutbetaling(
                        resultat.pensjonUnderUtbetaling?.totalbelopNetto ?: 0, resultat.virkFom!!.toNorwegianLocalDate())
                )
            }
        }

        return BeloepPeriode(beloep, periodeStart, periodeSlutt, maanedsutbetalinger)
    }

        // OpprettOutputHelper.addSimulertBeregningsinformasjonForKnekkpunkterToSimulertAlder
        //       + fetchAldersberegningKapittel19FromAlder2016 + getAldersberegningKapittel19
        private fun addSimulertBeregningsinformasjonForKnekkpunkterToSimulertAlder(
            simulertAlderspensjon: SimulertAlderspensjon,
            spec: SimuleringSpec,
            kravhode: Kravhode,
            soekerGrunnlag: Persongrunnlag,
            resultatListe: List<AbstraktBeregningsResultat>,
            forrigeResultat: AbstraktBeregningsResultat?,
            pensjonBeholdningPeriodeListe: List<BeholdningPeriode>,
            outputSimulertBeregningsinfoForAlleKnekkpunkter: Boolean
        ) {
            val forrigeBasispensjon: Int
            val forrigeBeholdning: Int

            if (forrigeResultat != null) {
                when (forrigeResultat) {
                    is BeregningsResultatAlderspensjon2011 -> {
                        forrigeBasispensjon = basispensjon(forrigeResultat.beregningKapittel19)
                        forrigeBeholdning = 0
                    }

                    is BeregningsResultatAlderspensjon2016 -> {
                        forrigeBasispensjon = basispensjon(forrigeResultat.beregningsResultat2011?.beregningKapittel19)
                        forrigeBeholdning = pensjonsbeholdning(forrigeResultat.beregningsResultat2025)
                    }

                    is BeregningsResultatAlderspensjon2025 -> {
                        forrigeBasispensjon = 0
                        forrigeBeholdning = pensjonsbeholdning(forrigeResultat)
                    }

                    else -> throw RuntimeException("Unexpected type of forrigeResultat: $forrigeResultat")
                }
            } else {
                forrigeBasispensjon = 0
                forrigeBeholdning = 0
            }

            val foedselsdato = soekerGrunnlag.fodselsdato?.toNorwegianLocalDate()
            val punkter: SortedSet<LocalDate> = findKnekkpunkter(spec, foedselsdato)

            createSimulertBeregningsinfoForKnekkpunkter(
                kravhode,
                soekerGrunnlag,
                resultatListe,
                forrigeResultat,
                simulertAlderspensjon,
                forrigeBasispensjon,
                forrigeBeholdning,
                punkter,
                foedselsdato!!
            )

            if (outputSimulertBeregningsinfoForAlleKnekkpunkter) {
                simulertAlderspensjon.simulertBeregningInformasjonListe =
                    createSimulertBeregningsinfoForAlleKnekkpunkter(
                        kravhode,
                        resultatListe,
                        simulertAlderspensjon,
                        foedselsdato,
                        spec
                    )
            }

            simulertAlderspensjon.pensjonBeholdningListe = pensjonBeholdningPeriodeListe
        }

        private fun earliestVirkningFom(resultater: List<AbstraktBeregningsResultat>): Date? =
            PeriodeUtil.findEarliest(resultater)?.virkFom

        // OpprettOutputHelper.getBelop
        private fun getBeloep(
            periodeStart: LocalDate,
            periodeSlutt: LocalDate,
            gjeldendeResultat: AbstraktBeregningsResultat,
            fom: LocalDate,
            tom: LocalDate
        ): Int {
            val antallManeder = numberOfMonths(periodeStart, periodeSlutt, fom, tom)
            return gjeldendeResultat.pensjonUnderUtbetaling?.totalbelopNetto?.let { it * antallManeder } ?: 0
        }

        private fun getBeloep(
            periodeStart: Date,
            periodeSlutt: Date,
            gjeldendeResultat: AbstraktBeregningsResultat,
            fom: Date,
            tom: Date
        ): Int =
            getBeloep(
                periodeStart.toNorwegianLocalDate(),
                periodeSlutt.toNorwegianLocalDate(),
                gjeldendeResultat,
                fom.toNorwegianLocalDate(),
                tom.toNorwegianLocalDate()
            )

        private fun findKnekkpunkter(spec: SimuleringSpec, foedselsdato: LocalDate?): SortedSet<LocalDate> {
            val knekkpunkter: SortedSet<LocalDate> = TreeSet()

            if (spec.type != SimuleringType.AFP_ETTERF_ALDER) {
                addBeregningsinfoKnekkpunkt(
                    knekkpunkter,
                    spec.foersteUttakDato!!
                ) //TODO: Can forsteUttakDato be null?
            }

            spec.heltUttakDato?.let { addBeregningsinfoKnekkpunkt(knekkpunkter, it) }
            val betingelseslosPensjoneringsdato = foedselsdato?.let { ubetingetPensjoneringDato(it) }

            if (isBeforeByDay(spec.foersteUttakDato, betingelseslosPensjoneringsdato, false)) {
                addBeregningsinfoKnekkpunkt(knekkpunkter, betingelseslosPensjoneringsdato)
            }

            return knekkpunkter
        }

        private fun createSimulertBeregningsinfoForKnekkpunkter(
            kravhode: Kravhode,
            soekerGrunnlag: Persongrunnlag,
            resultatListe: List<AbstraktBeregningsResultat>,
            forrigeResultat: AbstraktBeregningsResultat?,
            simulertAlderspensjon: SimulertAlderspensjon,
            initiellForrigeBasispensjon: Int,
            initiellForrigeBeholdning: Int?,
            punkter: SortedSet<LocalDate>,
            foedselsdato: LocalDate
        ) {
            var forrigeBasispensjon = initiellForrigeBasispensjon
            var forrigeBeholdning = initiellForrigeBeholdning

            for (punkt in punkter) {
                val beregningsresultat = findValidForDate(resultatListe, punkt)

                val simulertBeregningsinfo = beregningsresultat?.let {
                    SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
                        kravhode,
                        beregningsresultat,
                        simulertAlderspensjon,
                        foedselsdato,
                        punkt
                    )
                } ?: continue

                val pensjonsbeholdning = findPensjonsbeholdningFoerUttak(
                    kravhode,
                    soekerGrunnlag,
                    resultatListe,
                    forrigeResultat,
                    beregningsresultat
                )
                simulertBeregningsinfo.pensjonBeholdningFoerUttak = pensjonsbeholdning?.totalbelop?.toInt()
                simulertBeregningsinfo.nOkap19 = (simulertBeregningsinfo.basispensjon ?: 0) - forrigeBasispensjon
                simulertBeregningsinfo.nOkap20 =
                    (simulertBeregningsinfo.pensjonBeholdningEtterUttak ?: 0) - (forrigeBeholdning ?: 0)
                forrigeBasispensjon = simulertBeregningsinfo.basispensjon ?: 0
                forrigeBeholdning = simulertBeregningsinfo.pensjonBeholdningEtterUttak ?: 0
                val periodeForAge =
                    findPensjonPeriodeForAlder(
                        simulertAlderspensjon.pensjonPeriodeListe,
                        calculateAgeInYears(foedselsdato, punkt)
                    )
                periodeForAge?.simulertBeregningInformasjonListe?.add(simulertBeregningsinfo)
            }
        }

        private fun createSimulertBeregningsinfoForAlleKnekkpunkter(
            kravhode: Kravhode,
            resultatListe: List<AbstraktBeregningsResultat>,
            simulertAlderspensjon: SimulertAlderspensjon,
            foedselsdato: LocalDate,
            spec: SimuleringSpec
        ): List<SimulertBeregningInformasjon> {
            val foersteHeleUttak: LocalDate
            val gradertUttak: LocalDate?

            if (spec.gjelderPre2025OffentligAfp()) {
                gradertUttak = null // AFP-simulering does not contain gradert uttak
                foersteHeleUttak = spec.heltUttakDato!! // Assuming heltUttakDato cannot be null in this context
            } else if (spec.heltUttakDato == null) {
                gradertUttak = null
                foersteHeleUttak =
                    spec.foersteUttakDato!! // Assuming forsteUttakDato cannot be null in this context
            } else {
                gradertUttak = spec.foersteUttakDato
                foersteHeleUttak = spec.heltUttakDato
            }

            val ubetingetPensjoneringDato = ubetingetPensjoneringDato(foedselsdato)
            val firstKnekkpunkt = gradertUttak ?: foersteHeleUttak
            val lastKnekkpunkt = getFirstDateInYear(getRelativeDateByYear(foedselsdato, MAX_KNEKKPUNKT_ALDER))
            val simulertBeregningInformasjonMap: SortedMap<LocalDate, SimulertBeregningInformasjon> = TreeMap()

            if (isBeforeByDay(firstKnekkpunkt, ubetingetPensjoneringDato, true)) {
                simulertBeregningInformasjonMap[ubetingetPensjoneringDato] =
                    createSimulertBeregningsinformasjonForKnekkpunkt(
                        kravhode,
                        resultatListe,
                        simulertAlderspensjon,
                        foedselsdato,
                        ubetingetPensjoneringDato
                    )
            }

            simulertBeregningInformasjonMap[foersteHeleUttak] = createSimulertBeregningsinformasjonForKnekkpunkt(
                kravhode,
                resultatListe,
                simulertAlderspensjon,
                foedselsdato,
                foersteHeleUttak
            )

            if (gradertUttak != null) {
                simulertBeregningInformasjonMap[gradertUttak] = createSimulertBeregningsinformasjonForKnekkpunkt(
                    kravhode,
                    resultatListe,
                    simulertAlderspensjon,
                    foedselsdato,
                    gradertUttak
                )
            }

            var knekkpunkt = getFirstDateInYear(firstKnekkpunkt)

            while (isBeforeByDay(knekkpunkt, lastKnekkpunkt, true)) {
                if (isAfterByDay(knekkpunkt, firstKnekkpunkt, false)) {
                    simulertBeregningInformasjonMap[knekkpunkt] = createSimulertBeregningsinformasjonForKnekkpunkt(
                        kravhode,
                        resultatListe,
                        simulertAlderspensjon,
                        foedselsdato,
                        knekkpunkt
                    )
                }

                knekkpunkt = getRelativeDateByYear(knekkpunkt, 1)
            }

            return ArrayList(simulertBeregningInformasjonMap.values)
        }

        private fun createSimulertBeregningsinformasjonForKnekkpunkt(
            kravhode: Kravhode,
            resultatListe: List<AbstraktBeregningsResultat>,
            simulertAlderspensjon: SimulertAlderspensjon,
            foedselsdato: LocalDate,
            knekkpunkt: LocalDate
        ): SimulertBeregningInformasjon? =
            findValidForDate(resultatListe, knekkpunkt)?.let {
                SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
                    kravhode,
                    it,
                    simulertAlderspensjon,
                    foedselsdato,
                    knekkpunkt
                )
            }

        // OpprettOutputHelper.removeEktefelleAndBarnetilleggFromTotalbelop
        private fun removeEktefelleAndBarnetilleggFromTotalbeloep(
            alderspensjonBeregningResultat: AbstraktBeregningsResultat
        ) {
            // Identify the ytelseskomponenter to subtract from totalbeløp
            val tilleggsytelser = subsetOfTypes(
                alderspensjonBeregningResultat.pensjonUnderUtbetaling?.ytelseskomponenter.orEmpty(),
                YtelseskomponentTypeEnum.ET,
                YtelseskomponentTypeEnum.TFB,
                YtelseskomponentTypeEnum.TSB
            )

            val pensjon = alderspensjonBeregningResultat.pensjonUnderUtbetaling

            // Subtract the identified ytelseskomponenter which are in use from totalbeløp
            for (ytelseKomponent in tilleggsytelser) {
                if (ytelseKomponent.brukt) {
                    pensjon?.let { it.totalbelopNetto -= ytelseKomponent.netto }
                }
            }
        }

        private fun addBeregningsinfoKnekkpunkt(punktSett: MutableSet<LocalDate>, punkt: LocalDate?) {
            punkt?.let(punktSett::add)
        }

        private fun basispensjon(beregningKapittel19: AldersberegningKapittel19?): Int =
            beregningKapittel19?.basispensjon?.totalbelop?.toInt() ?: 0

        // OpprettOutputHelper.fetchForrigePenB
        private fun pensjonsbeholdning(beregningResultat: BeregningsResultatAlderspensjon2025?): Int =
            findElementOfType(
                list = beregningResultat?.beregningKapittel20?.beholdninger?.beholdninger.orEmpty(),
                type = BeholdningtypeEnum.PEN_B
            )?.totalbelop?.toInt() ?: 0

        private fun findPensjonsbeholdningFoerUttak(
            kravhode: Kravhode,
            soekerGrunnlag: Persongrunnlag,
            resultatListe: List<AbstraktBeregningsResultat>,
            forrigeResultat: AbstraktBeregningsResultat?,
            beregningResultat: AbstraktBeregningsResultat
        ): Pensjonsbeholdning? {
            val dagenFoerBeregningsresultatVirkFom = getRelativeDateByDays(beregningResultat.virkFom!!, -1)
            val forrigeResultatMedBeholdning =
                findForrigeBeregningsresultatMedBeholdning(
                    forrigeResultat,
                    dagenFoerBeregningsresultatVirkFom,
                    resultatListe
                )
            return findPensjonsbeholdning(
                kravhode,
                soekerGrunnlag,
                dagenFoerBeregningsresultatVirkFom,
                forrigeResultatMedBeholdning
            )
        }

        private fun dayBefore(earliestVirkningFom: Date?): Date = getRelativeDateByDays(earliestVirkningFom!!, -1)

        private fun findForrigeBeregningsresultatMedBeholdning(
            forrigeResultat: AbstraktBeregningsResultat?,
            dagenFoerBeregningResultatVirkningFom: Date,
            resultatListe: List<AbstraktBeregningsResultat>
        ): AbstraktBeregningsResultat? =
            findValidForDate(resultatListe, dagenFoerBeregningResultatVirkningFom) ?: forrigeResultat

        private fun findPensjonsbeholdning(
            kravhode: Kravhode,
            soekerGrunnlag: Persongrunnlag,
            dagenFoerBeregningsresultatVirkFom: Date,
            beregningsresultat: AbstraktBeregningsResultat?
        ): Pensjonsbeholdning? =
            kravhode.regelverkTypeEnum?.let {
                if (beregningsresultat == null)
                    findBeholdningFraPersongrunnlag(
                        soekerGrunnlag,
                        it,
                        dagenFoerBeregningsresultatVirkFom
                    )
                else
                    findBeholdningFraBeregningsresultat(beregningsresultat, it)
            }

        // OpprettOutputHelper.findBeholdningFraBeregningsresultat
        private fun findBeholdningFraBeregningsresultat(
            beregningsresultat: AbstraktBeregningsResultat,
            regelverkType: RegelverkTypeEnum
        ): Pensjonsbeholdning? =
            when (regelverkType) {
                RegelverkTypeEnum.N_REG_G_N_OPPTJ -> findElementOfType(
                    (beregningsresultat as? BeregningsResultatAlderspensjon2016)?.beregningsResultat2025?.beregningKapittel20?.beholdninger?.beholdninger.orEmpty(),
                    BeholdningtypeEnum.PEN_B
                )

                RegelverkTypeEnum.N_REG_N_OPPTJ -> findElementOfType(
                    (beregningsresultat as? BeregningsResultatAlderspensjon2025)?.beregningKapittel20?.beholdninger?.beholdninger.orEmpty(),
                    BeholdningtypeEnum.PEN_B
                )

                else -> null
            }

        private fun findBeholdningFraPersongrunnlag(
            soekerGrunnlag: Persongrunnlag,
            regelverkType: RegelverkTypeEnum,
            dagenFoerBeregningsresultatVirkFom: Date
        ): Pensjonsbeholdning? {
            var beholdning: Pensjonsbeholdning? = null

            if (EnumSet.of(RegelverkTypeEnum.N_REG_G_N_OPPTJ, RegelverkTypeEnum.N_REG_N_OPPTJ)
                    .contains(regelverkType)
            ) {
                val aar = getYear(dagenFoerBeregningsresultatVirkFom)
                val beholdninger = sortedSubset(soekerGrunnlag.beholdninger, aar)
                beholdning = findLatest(beholdninger)
            }

            return beholdning
        }

        private fun findPensjonPeriodeForAlder(
            pensjonsperiodeListe: List<PensjonPeriode>,
            alderAar: Int
        ): PensjonPeriode? {
            var result: PensjonPeriode? = null

            for (periode in pensjonsperiodeListe) {
                if (periode.alderAar == alderAar) {
                    result = periode
                    break
                }
            }

            return result
        }

    private fun pensjonPeriode(alderAar: Int?, beloep: Int, maanedsutbetalinger: List<Maanedsutbetaling>) =
        PensjonPeriode().apply {
            this.alderAar = alderAar
            this.beloep = beloep
            this.maanedsutbetalinger = maanedsutbetalinger
        }

        // From ArligInformasjonListeUtils
        private fun findEarliest(list: List<Opptjeningsgrunnlag>): Opptjeningsgrunnlag? {
            var result: Opptjeningsgrunnlag? = null
            var earliestAar = Int.MAX_VALUE

            for (element in list) {
                val aar = element.ar

                if (aar < earliestAar) {
                    result = element
                    earliestAar = aar
                }
            }

            return result
        }

        // From PeriodisertInformasjonListeUtils
        private fun findEarliestIntersecting(
            list: List<BeregningsResultatAfpPrivat>,
            startDate: Date?,
            endDate: Date?
        ): BeregningsResultatAfpPrivat? {
            var result: BeregningsResultatAfpPrivat? = null
            var earliestDate: Date? = ETERNITY

            for (element in list) {
                if (intersectsWithPossiblyOpenEndings(startDate, endDate, element.virkFom, element.virkTom, true)) {
                    if (isBeforeByDay(element.virkFom, earliestDate, false)) {
                        earliestDate = element.virkFom
                        result = element
                    }
                }
            }

            return result
        }

        // TypedInformationListeUtils.subsetOfTypes
        private fun subsetOfTypes(
            list: List<Ytelseskomponent>,
            vararg types: YtelseskomponentTypeEnum
        ): List<Ytelseskomponent> =
            list.filter {
                listOf(*types).any { t -> t == it.ytelsekomponentTypeEnum }
            }

        private fun copy(original: AbstraktBeregningsResultat): AbstraktBeregningsResultat =
            when (original) {
                is BeregningsResultatAlderspensjon2011 -> original.copy()
                is BeregningsResultatAlderspensjon2016 -> original.copy()
                is BeregningsResultatAlderspensjon2025 -> original.copy()
                is BeregningsResultatAfpPrivat -> original.copy()
                is BeregningsresultatUforetrygd -> original.copy()
                else -> throw IllegalArgumentException("Unexpected AbstraktBeregningsResultat subclass: $original")
            }

        private data class BeloepPeriode(val beloep: Int, val start: Date?, val slutt: Date?, val maanedsutbetalinger: List<Maanedsutbetaling>)
    }
}
