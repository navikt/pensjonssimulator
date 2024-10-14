package no.nav.pensjon.simulator.core.krav

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpBeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningType
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getLastDateInYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.lastDayOfMonthUserTurns67
import no.nav.pensjon.simulator.core.trygd.*
import no.nav.pensjon.simulator.core.trygd.TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode
import no.nav.pensjon.simulator.core.util.toLocalDate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OppdaterKravhodeForForsteKnekkpunktHelper
@Component
class KravhodeUpdater(
    private val context: SimulatorContext,
    private val pre2025OffentligAfpBeholdning: Pre2025OffentligAfpBeholdning
) {
    private val logger = LoggerFactory.getLogger(KravhodeUpdater::class.java)

    // OppdaterKravhodeForForsteKnekkpunktHelper.oppdaterKravHodeForForsteKnekkpunkt
    fun updateKravhodeForFoersteKnekkpunkt(spec: KravhodeUpdateSpec): Kravhode {
        val kravhode = spec.kravhode
        val simuleringSpec = spec.simulering
        val forrigeAlderspensjonBeregningResult = spec.forrigeAlderspensjonBeregningResult
        var soekerGrunnlag = kravhode.hentPersongrunnlagForSoker()
        var avdoedGrunnlag =
            kravhode.hentPersongrunnlagForRolle(grunnlagsrolle = GrunnlagsrolleEnum.AVDOD, checkBruk = false)

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
            simuleringSpec.gjelderPre2025OffentligAfp() -> pre2025OffentligAfpBeholdning.setPensjonsbeholdning(
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
            val trygdetidGrunnlag: TTPeriode = anonymSimuleringTrygdetidPeriode(spec)
            persongrunnlag.trygdetidPerioder.add(trygdetidGrunnlag)
            persongrunnlag.trygdetidPerioderKapittel20.add(trygdetidGrunnlag)
            return persongrunnlag
        }

        if (simuleringSpec.boddUtenlands) {
            kravhode.boddEllerArbeidetIUtlandet = true
            val regelverkType = kravhode.regelverkTypeEnum!!

            if (regelverkType.isAlderspensjon2011) {
                setKapittel19Trygdetid(persongrunnlag, simuleringSpec)
            }

            if (regelverkType.isAlderspensjon2016) {
                setKapittel19Trygdetid(persongrunnlag, simuleringSpec)
                setKapittel20Trygdetid(persongrunnlag, simuleringSpec)
            }

            if (regelverkType.isAlderspensjon2025) {
                setKapittel20Trygdetid(persongrunnlag, simuleringSpec)
            }

            persongrunnlag.trygdeavtale = TrygdeavtaleFactory.newTrygdeavtaleForSimuleringUtland()
            persongrunnlag.trygdeavtaledetaljer = TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland()
            persongrunnlag.inngangOgEksportGrunnlag =
                InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(persongrunnlag, kravhode)
            return persongrunnlag
        }

        return TrygdetidSetter.settTrygdetid(spec)
    }

    // SimulerFleksibelAPCommand.setTrygetidKap19
    private fun setKapittel19Trygdetid(persongrunnlag: Persongrunnlag, spec: SimuleringSpec) {
        val trygdetidGrunnlagMedPensjonspoengListe =
            mapOpptjeningGrunnlagToTrygdetid(persongrunnlag.opptjeningsgrunnlagListe)
        val utlandPeriodeListe = spec.utlandPeriodeListe

        val trygdetidGrunnlagUtlandOppholdListe =
            if (trygdetidGrunnlagMedPensjonspoengListe.isEmpty())
                UtlandPeriodeTrygdetidMapper.utlandTrygdetidGrunnlag(utlandPeriodeListe)
            else
                UtlandPeriodeTrygdetidMapper.utlandTrygdetidGrunnlag(
                    utlandPeriodeListe,
                    trygdetidGrunnlagMedPensjonspoengListe
                )

        val trygdetidGrunnlagListe = createTrygdetidsgrunnlagList(
            trygdetidGrunnlagUtlandOppholdListe,
            foedselDato = persongrunnlag.penPerson?.fodselsdato.toLocalDate()!!,
            spec.foersteUttakDato
        )

        trygdetidGrunnlagListe.forEach { persongrunnlag.trygdetidPerioder.add(it) }
    }

    // SimulerFleksibelAPCommand.setTrygdetidKap20
    private fun setKapittel20Trygdetid(persongrunnlag: Persongrunnlag, spec: SimuleringSpec) {
        val trygdetidGrunnlagUtlandOppholdListe =
            UtlandPeriodeTrygdetidMapper.utlandTrygdetidGrunnlag(spec.utlandPeriodeListe)

        val trygdetidGrunnlagListe = createTrygdetidsgrunnlagList(
            trygdetidGrunnlagUtlandOppholdListe,
            foedselDato = persongrunnlag.penPerson?.fodselsdato.toLocalDate()!!,
            spec.foersteUttakDato
        )

        trygdetidGrunnlagListe.forEach { persongrunnlag.trygdetidPerioderKapittel20.add(it) }
    }

    private fun mapOpptjeningGrunnlagToTrygdetid(opptjeningListe: List<Opptjeningsgrunnlag>): List<TrygdetidOpphold> {
        val trygdetidListe: MutableList<TrygdetidOpphold> = mutableListOf()
        val addedAarListe: MutableSet<Int> = HashSet()

        for (opptjening in opptjeningListe) {
            val aar = opptjening.ar
            if (addedAarListe.contains(aar) || opptjening.pp <= 0) continue

            val trygdetidGrunnlag = TrygdetidGrunnlagFactory.trygdetidPeriode(
                fom = LocalDate.of(aar, 1, 1),
                tom = LocalDate.of(aar, 12, 31),
                land = LandkodeEnum.NOR
            )

            trygdetidListe.add(TrygdetidOpphold(periode = trygdetidGrunnlag, arbeidet = true))
            addedAarListe.add(aar)
        }

        return trygdetidListe
    }

    private companion object {

        //TODO: Should this value be 17 instead?
        // According to documentation it is 16: https://pensjon-dokumentasjon.intern.dev.nav.no/pen/Fellestjenester/FPEN028_abstraktSimulerAPFra2011.html#_setttrygdetidsgrunnlag
        // but that may be an outdated fact
        private const val AGE_ADULTHOOD_OPPTJENING = 16

        // SimulerFleksibelAPCommand.createTrygdetidsgrunnlagForenkletSimulering
        private fun anonymSimuleringTrygdetidPeriode(spec: TrygdetidGrunnlagSpec): TTPeriode {
            val fom = LocalDate.of(
                spec.simuleringSpec.foedselAar + AGE_ADULTHOOD_OPPTJENING + (spec.utlandAntallAar ?: 0),
                1,
                1
            )

            return anonymSimuleringTrygdetidPeriode(fom, spec.tom)
        }

        // SimulerFleksibelAPCommand.createTrygdetidsgrunnlagList
        private fun createTrygdetidsgrunnlagList(
            trygdetidGrunnlagUtlandOppholdListe: List<TrygdetidOpphold>,
            foedselDato: LocalDate,
            foersteUttakDato: LocalDate?
        ): List<TTPeriode> {
            // Step 2 Gap-fill domestic basis for pension
            val trygdetidGrunnlagMedInnlandBasisListe: List<TrygdetidOpphold> =
                InnlandTrygdetidGrunnlagInserter.createTrygdetidGrunnlagForInnlandPerioder(
                    trygdetidGrunnlagUtlandOppholdListe,
                    foedselDato
                )

            // Step 3 Remove periods of non-contributing countries (ikkeAvtaleLand)
            val trygdetidGrunnlagOpptjeningListe: List<TTPeriode> =
                TrygdetidTrimmer.removeIkkeAvtaleland(trygdetidGrunnlagMedInnlandBasisListe)

            // Step 4a Remove periods before age of adulthood
            val trygdetidGrunnlagPendingEndListe: List<TTPeriode> =
                TrygdetidTrimmer.removePeriodBeforeAdulthood(trygdetidGrunnlagOpptjeningListe, foedselDato)

            // Step 4b Remove periods after obtained pension age
            return TrygdetidTrimmer.removePeriodAfterPensionAge(
                trygdetidPeriodeListe = trygdetidGrunnlagPendingEndListe.toMutableList(),
                foersteUttakDato = foersteUttakDato!!,
                foedselDato = foedselDato
            )
        }
    }
}

//TODO move this into Enum?
val RegelverkTypeEnum.isAlderspensjon2011: Boolean
    get() {
        return this.kap19 && !this.kap20
    }

val RegelverkTypeEnum.isAlderspensjon2016: Boolean
    get() {
        return this.kap19 && this.kap20
    }

val RegelverkTypeEnum.isAlderspensjon2025: Boolean
    get() {
        return !this.kap19 && this.kap20
    }

private val RegelverkTypeEnum.kap19: Boolean
    get() {
        return this == RegelverkTypeEnum.N_REG_G_N_OPPTJ
                || this == RegelverkTypeEnum.N_REG_G_OPPTJ
    }

private val RegelverkTypeEnum.kap20: Boolean
    get() {
        return this == RegelverkTypeEnum.N_REG_G_N_OPPTJ
                || this == RegelverkTypeEnum.N_REG_N_OPPTJ
    }
