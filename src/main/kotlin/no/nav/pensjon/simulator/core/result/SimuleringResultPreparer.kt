package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.SimuleringSpec
import no.nav.pensjon.simulator.core.ytelse.YtelseKomponentType
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.findElementOfType
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.sortedSubset
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.beholdning.BeholdningType
import no.nav.pensjon.simulator.core.domain.RegelverkType
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.kode.RegelverkTypeCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.ETERNITY
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.MAANEDER_PER_AR
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.calculateAgeInYears
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.firstDayOfMonthAfterUserTurnsGivenAge
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
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
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.setTimeToZero
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.ubetingetPensjoneringDato
import no.nav.pensjon.simulator.core.util.PeriodeUtil
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findLatest
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findValidForDate
import no.nav.pensjon.simulator.core.util.PeriodeUtil.numberOfMonths
import no.nav.pensjon.simulator.core.util.toLocalDate
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OpprettOutputHelper
object SimuleringResultPreparer {

    private const val MAX_OPPTJENING_ALDER = 75
    private const val MAX_KNEKKPUNKT_ALDER = 77

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

    // TODO: Reconsider necessity for this
    private fun forceKap19OutputIfSimulerForTp(kravhode: Kravhode, spec: SimuleringSpec) {
        if (kravhode.regelverkTypeCti?.kode == RegelverkType.N_REG_N_OPPTJ.name && spec.simulerForTp) {
            // Set the regelverktype to 2011 so that the results are parsed and mapped as such
            kravhode.regelverkTypeCti = RegelverkTypeCti(RegelverkType.N_REG_G_OPPTJ.name)
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

        var poengtallListe: List<Poengtall> = emptyList()
        var sisteBeregningsresultat2011: BeregningsResultatAlderspensjon2011? = null

        if (kravhode.regelverkTypeCti?.kode == RegelverkType.N_REG_G_OPPTJ.name) {
            sisteBeregningsresultat2011 = findLatest(beregningResultatListe) as BeregningsResultatAlderspensjon2011
        }

        if (kravhode.regelverkTypeCti?.kode == RegelverkType.N_REG_G_N_OPPTJ.name) {
            val sisteBeregningsresultat2016 = findLatest(beregningResultatListe) as BeregningsResultatAlderspensjon2016
            sisteBeregningsresultat2011 = sisteBeregningsresultat2016.beregningsResultat2011
        }

        if (sisteBeregningsresultat2011 != null) {
            val sluttpoengtall = sisteBeregningsresultat2011.beregningsInformasjonKapittel19?.spt

            if (sluttpoengtall != null) {
                poengtallListe = sluttpoengtall.poengrekke?.poengtallListe.orEmpty()
            }
        }

        // Run from the year of the earliest opptjeningsgrunnlag to the year the user turns MAX_OPPTJENING_ALDER years of age
        val foersteAar: Int = findEarliest(soekerGrunnlag.opptjeningsgrunnlagListe)?.ar ?: return
        val sisteAar = yearUserTurnsGivenAge(soekerGrunnlag.fodselsdato!!, MAX_OPPTJENING_ALDER)

        for (aar in foersteAar..sisteAar) {
            simulatorOutput.opptjeningListe.add(
                SimulatorOutputMapper.mapToSimulertOpptjening(aar, beregningResultatListe, soekerGrunnlag, poengtallListe)
            )
        }
    }

    private fun createAndMapSimulertAlderspensjon(
        simulatorOutput: SimulatorOutput,
        simulatorInput: SimuleringSpec,
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
            simulatorInput,
            soekerGrunnlag,
            forrigeResultat,
            resultatListe
        )

        // Del 3.3
        addSimulertBeregningsinformasjonForKnekkpunkterToSimulertAlder(
            simulertAlderspensjon, simulatorInput, kravhode, soekerGrunnlag, resultatListe, forrigeResultat,
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
        val fodselsdato: LocalDate? = soekerGrunnlag.fodselsdato.toLocalDate()
        val startAlder = calculateStartAlder(simuleringSpec, fodselsdato!!, forrigeAfpBeregningResultat, false)
        var forrigeAfpBeregningsresultatKopi: BeregningsResultatAfpPrivat? = null

        if (forrigeAfpBeregningResultat != null) {
            // Put a copy of the løpende beregningsresultat on the list. Adjust fom/tom.
            forrigeAfpBeregningsresultatKopi = modifiedCopyOfBeregningsresultat(
                beregningResultat = forrigeAfpBeregningResultat,
                resultatListe = privatAfpBeregningResultatListe,
                foedselDato = fodselsdato
            )
            privatAfpBeregningResultatListe.add(forrigeAfpBeregningsresultatKopi)
        }

        for (alder in startAlder..MAX_OPPTJENING_ALDER) {
            val beloepPeriode: BeloepPeriode = beloepPeriode(fodselsdato, alder, privatAfpBeregningResultatListe)
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
                    alder = null
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
            when (kravhode.regelverkTypeCti?.kode) {
                RegelverkType.N_REG_G_OPPTJ.name -> {
                    // 2011
                    this.kapittel19Andel = 1.0
                    this.kapittel20Andel = 0.0
                }

                RegelverkType.N_REG_N_OPPTJ.name -> {
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

    // OpprettOutputHelper.addPensjonsperioderToSimulertAlder
    private fun addPensjonsperioderToSimulertAlder(
        simulertAlderspensjon: SimulertAlderspensjon,
        spec: SimuleringSpec,
        soekerGrunnlag: Persongrunnlag,
        forrigeResultat: AbstraktBeregningsResultat?,
        resultatListe: MutableList<AbstraktBeregningsResultat>
    ) {
        val foedselDato = soekerGrunnlag.fodselsdato.toLocalDate()!!
        val startAlder = calculateStartAlder(spec, foedselDato, forrigeResultat, true)
        var forrigeResultatKopi: AbstraktBeregningsResultat? = null

        if (forrigeResultat != null) {
            // Put a copy of the løpende beregningsresultat on the list. Remove ET/BT from totalbeløp and set fom/tom
            forrigeResultatKopi = modifiedCopyOfBeregningsresultat(forrigeResultat, resultatListe, foedselDato)
            resultatListe.add(forrigeResultatKopi)
        }

        for (alder in startAlder..MAX_KNEKKPUNKT_ALDER) {
            val beloepPeriode: BeloepPeriode = beloepPeriode(foedselDato, alder, resultatListe)
            simulertAlderspensjon.addPensjonsperiode(pensjonPeriode(alder, beloepPeriode.beloep))
        }

        forrigeResultatKopi?.let {
            // Add a periode representing løpende ytelser (tagged with alder=null)
            val beloep = (it.pensjonUnderUtbetaling?.totalbelopNetto ?: 0) * MAANEDER_PER_AR
            simulertAlderspensjon.addPensjonsperiode(pensjonPeriode(null, beloep))
        }
    }

    private fun <T : AbstraktBeregningsResultat> beloepPeriode(
        foedselDato: LocalDate,
        alderAar: Int,
        resultatListe: MutableList<T>
    ): BeloepPeriode {
        val periodeStart: Date =
            getRelativeDateByMonth(getFirstDayOfMonth(getRelativeDateByYear(foedselDato, alderAar)), 1)
        val periodeSlutt: Date = getLastDayOfMonth(fromLocalDate(getRelativeDateByYear(foedselDato, alderAar + 1))!!)
        var beloep = 0

        for (resultat in resultatListe) {
            val fom: Date = setTimeToZero(resultat.virkFom!!)
            val tom: Date = setTimeToZero(resultat.virkTom ?: ETERNITY)

            if (intersects(periodeStart, periodeSlutt, fom, tom, true)) {
                beloep += getBeloep(periodeStart, periodeSlutt, resultat, fom, tom)
            }
        }

        return BeloepPeriode(beloep, periodeStart, periodeSlutt)
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

        val fodselsdato = soekerGrunnlag.fodselsdato.toLocalDate()
        val punkter: SortedSet<LocalDate> = findKnekkpunkter(spec, fodselsdato)

        createSimulertBeregningsinfoForKnekkpunkter(
            kravhode,
            soekerGrunnlag,
            resultatListe,
            forrigeResultat,
            simulertAlderspensjon,
            forrigeBasispensjon,
            forrigeBeholdning,
            punkter,
            fodselsdato!!
        )

        if (outputSimulertBeregningsinfoForAlleKnekkpunkter) {
            simulertAlderspensjon.simulertBeregningInformasjonListe =
                createSimulertBeregningsinfoForAlleKnekkpunkter(
                    kravhode,
                    resultatListe,
                    simulertAlderspensjon,
                    fodselsdato,
                    spec
                )
        }

        simulertAlderspensjon.pensjonBeholdningListe = pensjonBeholdningPeriodeListe
    }

    private fun calculateStartAlder(
        spec: SimuleringSpec,
        foedselDato: LocalDate?,
        forrigeResultat: AbstraktBeregningsResultat?,
        handlePre2025OffentligAfpEtterfulgtAvAlderspensjon: Boolean
    ): Int {
        val alderVedFoersteUttak = calculateAgeInYears(foedselDato, spec.foersteUttakDato)

        return if (forrigeResultat == null) {
            if (handlePre2025OffentligAfpEtterfulgtAvAlderspensjon && spec.gjelderPre2025OffentligAfp())
                calculateAgeInYears(foedselDato, spec.heltUttakDato!!)
            else
                alderVedFoersteUttak
        } else {
            val alderToday = calculateAgeInYears(foedselDato, LocalDate.now())
            if (alderVedFoersteUttak == alderToday) alderToday else alderToday + 1
        }
    }

    // OpprettOutputHelper.createModifiedCopyOfForrigeAlderBeregningsresultat
    private fun modifiedCopyOfBeregningsresultat(
        beregningResultat: AbstraktBeregningsResultat,
        resultatListe: List<AbstraktBeregningsResultat>,
        foedselDato: LocalDate
    ): AbstraktBeregningsResultat {
        val alderToday: Int = calculateAgeInYears(foedselDato, LocalDate.now())

        return copy(beregningResultat).apply {
            virkFom = firstDayOfMonthAfterUserTurnsGivenAge(foedselDato, alderToday)
            virkTom = dayBefore(earliestVirkningFom(resultatListe))
            removeEktefelleAndBarnetilleggFromTotalbeloep(this)
        }
    }

    // OpprettOutputHelper.createModifiedCopyOfForrigeAfpBeregningsresultat
    private fun modifiedCopyOfBeregningsresultat(
        beregningResultat: BeregningsResultatAfpPrivat,
        resultatListe: List<BeregningsResultatAfpPrivat>,
        foedselDato: LocalDate
    ): BeregningsResultatAfpPrivat {
        val alderToday: Int = calculateAgeInYears(foedselDato, LocalDate.now())

        return copy(beregningResultat).apply {
            virkFom = firstDayOfMonthAfterUserTurnsGivenAge(foedselDato, alderToday)
            virkTom = if (resultatListe.isEmpty()) null else dayBefore(earliestVirkningFom(resultatListe))
        } as BeregningsResultatAfpPrivat
    }

    private fun earliestVirkningFom(resultater: List<AbstraktBeregningsResultat>): Date? =
        PeriodeUtil.findEarliest(resultater)?.virkFom

    // Extracted from OpprettOutputHelper.createModifiedCopyOfForrigeAlderBeregningsresultat
    private fun copy(original: AbstraktBeregningsResultat): AbstraktBeregningsResultat =
        when (original) {
            is BeregningsResultatAlderspensjon2011 -> BeregningsResultatAlderspensjon2011(original)
            is BeregningsResultatAlderspensjon2016 -> BeregningsResultatAlderspensjon2016(original)
            is BeregningsResultatAlderspensjon2025 -> BeregningsResultatAlderspensjon2025(original)
            is BeregningsResultatAfpPrivat -> BeregningsResultatAfpPrivat(original)
            is BeregningsresultatUforetrygd -> BeregningsresultatUforetrygd(original)
            else -> throw IllegalArgumentException("Unexpected AbstraktBeregningsResultat subclass: $original")
        }

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
            periodeStart.toLocalDate()!!,
            periodeSlutt.toLocalDate()!!,
            gjeldendeResultat,
            fom.toLocalDate()!!,
            tom.toLocalDate()!!
        )

    private fun findKnekkpunkter(spec: SimuleringSpec, foedselDato: LocalDate?): SortedSet<LocalDate> {
        val knekkpunkter: SortedSet<LocalDate> = TreeSet()

        if (spec.type != SimuleringType.AFP_ETTERF_ALDER) {
            addBeregningsinfoKnekkpunkt(
                knekkpunkter,
                spec.foersteUttakDato!!
            ) //TODO: Can forsteUttakDato be null?
        }

        spec.heltUttakDato?.let { addBeregningsinfoKnekkpunkt(knekkpunkter, it) }
        val betingelseslosPensjoneringsdato = foedselDato?.let { ubetingetPensjoneringDato(it) }

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
        foedselDato: LocalDate
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
                    foedselDato,
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
                findPensjonPeriodeForAlder(simulertAlderspensjon.pensjonPeriodeListe, calculateAgeInYears(foedselDato, punkt))
            periodeForAge?.simulertBeregningInformasjonListe?.add(simulertBeregningsinfo)
        }
    }

    private fun createSimulertBeregningsinfoForAlleKnekkpunkter(
        kravhode: Kravhode,
        resultatListe: List<AbstraktBeregningsResultat>,
        simulertAlderspensjon: SimulertAlderspensjon,
        foedselDato: LocalDate,
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
            foersteHeleUttak = spec.heltUttakDato!!
        }

        val ubetingetPensjoneringDato = ubetingetPensjoneringDato(foedselDato)
        val firstKnekkpunkt = gradertUttak ?: foersteHeleUttak
        val lastKnekkpunkt = getFirstDateInYear(getRelativeDateByYear(foedselDato, MAX_KNEKKPUNKT_ALDER))
        val simulertBeregningInformasjonMap: SortedMap<LocalDate, SimulertBeregningInformasjon> = TreeMap()

        if (isBeforeByDay(firstKnekkpunkt, ubetingetPensjoneringDato, true)) {
            simulertBeregningInformasjonMap[ubetingetPensjoneringDato] = createSimulertBeregningsinformasjonForKnekkpunkt(
                kravhode,
                resultatListe,
                simulertAlderspensjon,
                foedselDato,
                ubetingetPensjoneringDato
            )
        }

        simulertBeregningInformasjonMap[foersteHeleUttak] = createSimulertBeregningsinformasjonForKnekkpunkt(
            kravhode,
            resultatListe,
            simulertAlderspensjon,
            foedselDato,
            foersteHeleUttak
        )

        if (gradertUttak != null) {
            simulertBeregningInformasjonMap[gradertUttak] = createSimulertBeregningsinformasjonForKnekkpunkt(
                kravhode,
                resultatListe,
                simulertAlderspensjon,
                foedselDato,
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
                    foedselDato,
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
        foedselDato: LocalDate,
        knekkpunkt: LocalDate
    ): SimulertBeregningInformasjon? =
        findValidForDate(resultatListe, knekkpunkt)?.let {
            SimulatorOutputMapper.mapToSimulertBeregningsinformasjon(
                kravhode,
                it,
                simulertAlderspensjon,
                foedselDato,
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
            YtelseKomponentType.ET,
            YtelseKomponentType.TFB,
            YtelseKomponentType.TSB
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
            type = BeholdningType.PEN_B
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
            findForrigeBeregningsresultatMedBeholdning(forrigeResultat, dagenFoerBeregningsresultatVirkFom, resultatListe)
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
        kravhode.regelverkTypeCti?.kode?.let {
            if (beregningsresultat == null)
                findBeholdningFraPersongrunnlag(
                    soekerGrunnlag,
                    RegelverkType.valueOf(it),
                    dagenFoerBeregningsresultatVirkFom
                )
            else
                findBeholdningFraBeregningsresultat(beregningsresultat, RegelverkType.valueOf(it))
        }

    // OpprettOutputHelper.findBeholdningFraBeregningsresultat
    private fun findBeholdningFraBeregningsresultat(
        beregningsresultat: AbstraktBeregningsResultat,
        regelverkType: RegelverkType
    ): Pensjonsbeholdning? =
        when (regelverkType) {
            RegelverkType.N_REG_G_N_OPPTJ -> findElementOfType(
                (beregningsresultat as? BeregningsResultatAlderspensjon2016)?.beregningsResultat2025?.beregningKapittel20?.beholdninger?.beholdninger.orEmpty(),
                BeholdningType.PEN_B
            )

            RegelverkType.N_REG_N_OPPTJ -> findElementOfType(
                (beregningsresultat as? BeregningsResultatAlderspensjon2025)?.beregningKapittel20?.beholdninger?.beholdninger.orEmpty(),
                BeholdningType.PEN_B
            )

            else -> null
        }

    private fun findBeholdningFraPersongrunnlag(
        soekerGrunnlag: Persongrunnlag,
        regelverkType: RegelverkType,
        dagenFoerBeregningsresultatVirkFom: Date
    ): Pensjonsbeholdning? {
        var beholdning: Pensjonsbeholdning? = null

        if (EnumSet.of(RegelverkType.N_REG_G_N_OPPTJ, RegelverkType.N_REG_N_OPPTJ).contains(regelverkType)) {
            val aar = getYear(dagenFoerBeregningsresultatVirkFom)
            val beholdninger = sortedSubset(soekerGrunnlag.beholdninger, aar)
            beholdning = findLatest(beholdninger)
        }

        return beholdning
    }

    private fun findPensjonPeriodeForAlder(pensjonsperiodeListe: List<PensjonPeriode>, alderAar: Int): PensjonPeriode? {
        var result: PensjonPeriode? = null

        for (periode in pensjonsperiodeListe) {
            if (periode.alderAar == alderAar) {
                result = periode
                break
            }
        }

        return result
    }

    private fun pensjonPeriode(alderAar: Int?, beloep: Int) =
        PensjonPeriode().apply {
            this.alderAar = alderAar
            this.beloep = beloep
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
    private fun subsetOfTypes(list: List<Ytelseskomponent>, vararg types: YtelseKomponentType): List<Ytelseskomponent> =
        list.filter {
            listOf(*types).any { t -> t.name == it.ytelsekomponentType.kode }
        }

    private data class BeloepPeriode(val beloep: Int, val start: Date?, val slutt: Date?)
}