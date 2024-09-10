package no.nav.pensjon.simulator.core.krav

import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.SimuleringSpec
import no.nav.pensjon.simulator.core.beholdning.BeholdningType
import no.nav.pensjon.simulator.core.domain.GrunnlagRolle
import no.nav.pensjon.simulator.core.domain.RegelverkType
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpBeholdning
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.core.domain.regler.kode.RegelverkTypeCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getLastDateInYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.lastDayOfMonthUserTurns67
import no.nav.pensjon.simulator.core.trygd.*
import no.nav.pensjon.simulator.core.trygd.TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode
import no.nav.pensjon.simulator.core.trygd.TrygdetidGrunnlagSpec
import no.nav.pensjon.simulator.core.util.toLocalDate
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*

// Corresponds to OppdaterKravhodeForForsteKnekkpunktHelper
class KravhodeUpdater(private val context: SimulatorContext) {

    private val logger = LoggerFactory.getLogger(KravhodeUpdater::class.java)

    fun oppdaterKravHodeForForsteKnekkpunkt(spec: KravhodeUpdateSpec): Kravhode {
        val kravhode = spec.kravhode
        val simuleringSpec = spec.simulering
        val forrigeAlderspensjonBeregningResult = spec.forrigeAlderspensjonBeregningResult
        var soekerGrunnlag = kravhode.hentPersongrunnlagForSoker()
        var avdoedGrunnlag = kravhode.hentPersongrunnlagForRolle(grunnlagsrolle = GrunnlagRolle.AVDOD, checkBruk = false)

        logger.info("STEP 4.1 - Sett trygdetidsgrunnlag")
        soekerGrunnlag = setTrygdetid(
            TrygdetidGrunnlagSpec(
                soekerGrunnlag,
                simuleringSpec.utlandAntallAar,
                null,
                forrigeAlderspensjonBeregningResult,
                simuleringSpec
            ), kravhode
        )

        logger.info("STEP 4.2 - Sett pensjonsbeholdning for bruker")
        when {
            simuleringSpec.gjelderPre2025OffentligAfp() -> Pre2025OffentligAfpBeholdning(context).setPensjonsbeholdning(
                soekerGrunnlag,
                forrigeAlderspensjonBeregningResult
            )

            simuleringSpec.gjelderEndring() -> {} // ref. SimulerEndringAvAPCommand.settPensjonsbeholdning
            else -> soekerGrunnlag.replaceBeholdninger(fetchBeholdninger(soekerGrunnlag, simuleringSpec.foersteUttakDato))
        }

        logger.info("STEP 4.3 - Sett uførehistorikk")
        val ufoerePeriodeTom = uforeperiodeTom(simuleringSpec, soekerGrunnlag)
        setUfoereHistorikk(soekerGrunnlag, ufoerePeriodeTom)

        if (avdoedGrunnlag != null) {
            logger.info("STEP 4.4 - Sett trygdetidsgrunnlag for avdød")
            // Dodsdato set to Dec 31 the previous year
            val lastDayOfYearBeforeDodsdato = getLastDateInYear(getRelativeDateByYear(avdoedGrunnlag.dodsdato!!, -1))

            avdoedGrunnlag = setTrygdetid(
                TrygdetidGrunnlagSpec(
                    persongrunnlag = avdoedGrunnlag,
                    utlandAntallAar = simuleringSpec.avdoed?.antallAarUtenlands,
                    tom = lastDayOfYearBeforeDodsdato.toLocalDate(),
                    forrigeAlderspensjonBeregningResultat = forrigeAlderspensjonBeregningResult,
                    simuleringSpec = simuleringSpec
                ),
                kravhode
            )

            logger.info("STEP 4.5 - Sett uførehistorikk for avdød")
            setUfoereHistorikk(avdoedGrunnlag)
        }

        return kravhode
    }

    // SimulerFleksibelAPCommand.settPensjonsbeholdning (part of)
    private fun fetchBeholdninger(grunnlag: Persongrunnlag, foersteUttakDato: LocalDate?) =
        //context.beregnOpptjening(forsteUttakDato?.noon(), grunnlag)
        context.beregnOpptjening(foersteUttakDato, grunnlag)
            .filter { BeholdningType.PEN_B.name == it.beholdningsType?.kode }

    private fun setUfoereHistorikk(persongrunnlag: Persongrunnlag) {
        setUfoereHistorikk(persongrunnlag, persongrunnlag.dodsdato)
    }

    // From SettUforehistorikkHelper
    private fun setUfoereHistorikk(persongrunnlag: Persongrunnlag, tom: Date?) {
        val ufoereHistorikk = persongrunnlag.uforeHistorikk
        if (ufoereHistorikk?.uforeperiodeListe == null) return

        val historikkCopy = Uforehistorikk(ufoereHistorikk)

        historikkCopy.uforeperiodeListe.forEach {
            if (it.ufgTom == null) {
                it.ufgTom = tom
            }
        }

        persongrunnlag.uforeHistorikk = historikkCopy
    }

    private fun uforeperiodeTom(spec: SimuleringSpec, soekerGrunnlag: Persongrunnlag): Date {
        val sisteDagIManedenSokerBlir67 = lastDayOfMonthUserTurns67(soekerGrunnlag.fodselsdato)

        return if (spec.type == SimuleringType.ALDER_M_AFP_PRIVAT && isBeforeByDay(
                spec.foersteUttakDato,
                sisteDagIManedenSokerBlir67,
                false
            )
        )
            getRelativeDateByDays(fromLocalDate(spec.foersteUttakDato)!!, -1)
        else
            sisteDagIManedenSokerBlir67
    }

    // SimulerFleksibelAPCommand.settTrygdetid
    private fun setTrygdetid(spec: TrygdetidGrunnlagSpec, kravhode: Kravhode): Persongrunnlag {
        val persongrunnlag = spec.persongrunnlag
        val simuleringSpec = spec.simuleringSpec

        if (simuleringSpec.erAnonym) {
            val trygdetidsgrunnlag: TTPeriode = createTrygdetidsgrunnlagForenkletSimulering(spec)
            persongrunnlag.trygdetidPerioder.add(trygdetidsgrunnlag)
            persongrunnlag.trygdetidPerioderKapittel20.add(trygdetidsgrunnlag)
            return persongrunnlag
        }

        if (simuleringSpec.boddUtenlands) {
            kravhode.boddEllerArbeidetIUtlandet = true
            val regelverkType = kravhode.regelverkTypeCti!!

            if (regelverkType.isAlderspensjon2011) {
                setTrygdetidKapittel19(persongrunnlag, simuleringSpec)
            }

            if (regelverkType.isAlderspensjon2016) {
                setTrygdetidKapittel19(persongrunnlag, simuleringSpec)
                setTrygdetidKapittel20(persongrunnlag, simuleringSpec)
            }

            if (regelverkType.isAlderspensjon2025) {
                setTrygdetidKapittel20(persongrunnlag, simuleringSpec)
            }

            persongrunnlag.trygdeavtale = TrygdeavtaleFactory.newTrygdeavtaleForSimuleringUtland()
            persongrunnlag.trygdeavtaledetaljer = TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland()
            persongrunnlag.inngangOgEksportGrunnlag =
                InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(persongrunnlag, kravhode)
            return persongrunnlag
        }

        return TrygdetidSetter.settTrygdetid(spec)
    }

    private fun setTrygdetidKapittel19(persongrunnlag: Persongrunnlag, spec: SimuleringSpec) {
        val trygdetidsgrunnlagWithPensjonspoengList =
            mapOpptjeningsgrunnlagToTrygdetid(persongrunnlag.opptjeningsgrunnlagListe)
        val utenlandsperioder = spec.utlandPeriodeListe

        val trygdetidGrunnlagUtlandOppholdListe =
            if (trygdetidsgrunnlagWithPensjonspoengList.isEmpty())
                UtlandPeriodeTrygdetidMapper.newTrygdetidsgrunnlagForUtenlandsperioder(utenlandsperioder)
            else
                UtlandPeriodeTrygdetidMapper.newTrygdetidsgrunnlagForUtenlandsperioder(
                    utenlandsperioder,
                    trygdetidsgrunnlagWithPensjonspoengList
                )

        val trygdetidGrunnlagListe = createTrygdetidsgrunnlagList(
            trygdetidGrunnlagUtlandOppholdListe,
            foedselDato = persongrunnlag.penPerson?.fodselsdato.toLocalDate()!!,
            spec.foersteUttakDato
        )

        trygdetidGrunnlagListe.forEach { persongrunnlag.trygdetidPerioder.add(it) }
    }

    private fun setTrygdetidKapittel20(persongrunnlag: Persongrunnlag, spec: SimuleringSpec) {
        val trygdetidGrunnlagUtlandOppholdListe =
            UtlandPeriodeTrygdetidMapper.newTrygdetidsgrunnlagForUtenlandsperioder(spec.utlandPeriodeListe)

        val trygdetidGrunnlagListe = createTrygdetidsgrunnlagList(
            trygdetidGrunnlagUtlandOppholdListe,
            foedselDato = persongrunnlag.penPerson?.fodselsdato.toLocalDate()!!,
            spec.foersteUttakDato
        )

        trygdetidGrunnlagListe.forEach { persongrunnlag.trygdetidPerioderKapittel20.add(it) }
    }

    private fun mapOpptjeningsgrunnlagToTrygdetid(opptjeningsgrunnlagListe: List<Opptjeningsgrunnlag>): List<TrygdetidOpphold> {
        val grunnlagListe: MutableList<TrygdetidOpphold> = mutableListOf()
        val addedYears: MutableSet<Int> = HashSet()

        for (opptjeningsgrunnlag in opptjeningsgrunnlagListe) {
            if (!addedYears.contains(opptjeningsgrunnlag.ar) && opptjeningsgrunnlag.pp > 0) {
                val trygdetidGrunnlag = TrygdetidGrunnlagFactory.newTrygdetidPeriode(
                    //TODO check use of Calendar.XXX together with LocalDate.of
                    fom = LocalDate.of(opptjeningsgrunnlag.ar, 1, 1),
                    tom = LocalDate.of(opptjeningsgrunnlag.ar, 12, 31),
                    land = Land.NOR
                )

                grunnlagListe.add(TrygdetidOpphold(trygdetidGrunnlag, true))
                addedYears.add(opptjeningsgrunnlag.ar)
            }
        }

        return grunnlagListe
    }

    private companion object {

        //TODO: Should this value be 17 instead?
        // According to documentation it is 16: https://pensjon-dokumentasjon.intern.dev.nav.no/pen/Fellestjenester/FPEN028_abstraktSimulerAPFra2011.html#_setttrygdetidsgrunnlag
        // but that may be an outdated fact
        private const val AGE_ADULTHOOD_OPPTJENING = 16

        // SimulerFleksibelAPCommand.createTrygdetidsgrunnlagForenkletSimulering
        private fun createTrygdetidsgrunnlagForenkletSimulering(spec: TrygdetidGrunnlagSpec): TTPeriode {
            //val fom = createDate(input.simulering.fodselsar + AGE_ADULTHOOD_OPPTJENING + (input.antallAarUtenlands ?: 0), Calendar.JANUARY, 1)
            val fom = LocalDate.of(
                spec.simuleringSpec.foedselAar + AGE_ADULTHOOD_OPPTJENING + (spec.utlandAntallAar ?: 0),
                1,
                1
            )
            return anonymSimuleringTrygdetidPeriode(fom, spec.tom)
        }

        private fun createTrygdetidsgrunnlagList(
            trygdetidGrunnlagUtlandOppholdListe: List<TrygdetidOpphold>,
            foedselDato: LocalDate,
            foersteUttakDato: LocalDate?
        ): List<TTPeriode> {
            // Step 2 Gap-fill domestic basis for pension
            //val birthDate = DateUtils.fromLocalDate(simulerFleksibelAPContext.getPersonConsumerService().hentFoedselsdato(pid.pid))
            val trygdetidsgrunnlagWithDomesticBasisList =
                InnlandTrygdetidGrunnlagInserter.createTrygdetidGrunnlagForInnlandPerioder(
                    trygdetidGrunnlagUtlandOppholdListe,
                    foedselDato
                )

            // Step 3 Remove periods of non-contributing countries (ikkeAvtaleLand)
            val trygdetidsgrunnlagOpptjeningList =
                TrygdetidTrimmer.removeIkkeAvtaleland(trygdetidsgrunnlagWithDomesticBasisList)

            // Step 4a Remove periods before age of adulthood
            val trygdetidsgrunnlagPendingEndList =
                TrygdetidTrimmer.removePeriodBeforeAdulthood(trygdetidsgrunnlagOpptjeningList, foedselDato)

            // Step 4b Remove periods after obtained pension age
            return TrygdetidTrimmer.removePeriodAfterPensionAge(
                trygdetidsgrunnlagPendingEndList.toMutableList(),
                foersteUttakDato!!,
                foedselDato
            )
        }
    }
}

val RegelverkTypeCti.isAlderspensjon2011: Boolean
    get() {
        return this.kap19 && !this.kap20
    }

val RegelverkTypeCti.isAlderspensjon2016: Boolean
    get() {
        return this.kap19 && this.kap20
    }

val RegelverkTypeCti.isAlderspensjon2025: Boolean
    get() {
        return !this.kap19 && this.kap20
    }

private val RegelverkTypeCti.kap19: Boolean
    get() {
        return this.kode == RegelverkType.N_REG_G_N_OPPTJ.name
                || this.kode == RegelverkType.N_REG_G_OPPTJ.name
    }

private val RegelverkTypeCti.kap20: Boolean
    get() {
        return this.kode == RegelverkType.N_REG_G_N_OPPTJ.name
                || this.kode == RegelverkType.N_REG_N_OPPTJ.name
    }