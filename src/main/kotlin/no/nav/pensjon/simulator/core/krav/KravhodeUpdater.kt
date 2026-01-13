package no.nav.pensjon.simulator.core.krav

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpBeholdning
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getLastDateInYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.lastDayOfMonthUserTurnsGivenAge
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.trygdetid.InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland
import no.nav.pensjon.simulator.trygdetid.Kapittel19TrygdetidsgrunnlagCreator.kapittel19TrygdetidsperiodeListe
import no.nav.pensjon.simulator.trygdetid.Kapittel20TrygdetidsgrunnlagCreator.kapittel20TrygdetidsperiodeListe
import no.nav.pensjon.simulator.trygdetid.TrygdeavtaleFactory.newTrygdeavtaleForSimuleringUtland
import no.nav.pensjon.simulator.trygdetid.TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagSpec
import no.nav.pensjon.simulator.trygdetid.TrygdetidSetter
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// PEN:
// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OppdaterKravhodeForForsteKnekkpunktHelper
@Component
class KravhodeUpdater(
    private val context: SimulatorContext,
    private val normalderService: NormertPensjonsalderService,
    private val tidsbegrensetOffentligAfpBeholdning: Pre2025OffentligAfpBeholdning,
    private val trygdetidSetter: TrygdetidSetter,
    private val time: Time
) {
    private val log = KotlinLogging.logger {}

    // OppdaterKravhodeForForsteKnekkpunktHelper.oppdaterKravHodeForForsteKnekkpunkt
    fun updateKravhodeForFoersteKnekkpunkt(spec: KravhodeUpdateSpec): Kravhode {
        val kravhode = spec.kravhode
        val simuleringSpec = spec.simulering
        val forrigeAlderspensjonBeregningResult = spec.forrigeAlderspensjonBeregningResult
        var soekerGrunnlag = kravhode.hentPersongrunnlagForSoker()
        var avdoedGrunnlag =
            kravhode.hentPersongrunnlagForRolle(rolle = GrunnlagsrolleEnum.AVDOD, checkBruk = false)

        log.debug { "STEP 4.1 - Sett trygdetidsgrunnlag" }
        soekerGrunnlag = setTrygdetid(
            TrygdetidGrunnlagSpec(
                soekerGrunnlag,
                simuleringSpec.utlandAntallAar,
                null,
                forrigeAlderspensjonBeregningResult,
                simuleringSpec
            ), kravhode
        )

        log.debug { "STEP 4.2 - Sett pensjonsbeholdning for bruker" }
        when {
            simuleringSpec.gjelderPre2025OffentligAfp() -> tidsbegrensetOffentligAfpBeholdning.setPensjonsbeholdning(
                soekerGrunnlag,
                forrigeAlderspensjonBeregningResult
            )

            simuleringSpec.gjelderEndring() -> {} // ref. SimulerEndringAvAPCommand.settPensjonsbeholdning
            else -> soekerGrunnlag.replaceBeholdninger(
                fetchBeholdninger(
                    soekerGrunnlag,
                    simuleringSpec.foersteUttakDato
                )
            )
        }

        log.debug { "STEP 4.3 - Sett uførehistorikk" }
        val ufoerePeriodeTom = ufoerePeriodeTom(simuleringSpec, soekerGrunnlag)
        setUfoereHistorikk(soekerGrunnlag, ufoerePeriodeTom)

        if (avdoedGrunnlag != null) {
            log.debug { "STEP 4.4 - Sett trygdetidsgrunnlag for avdød" }
            // Dodsdato set to Dec 31 the previous year
            val lastDayOfYearBeforeDoedDato = getLastDateInYear(getRelativeDateByYear(avdoedGrunnlag.dodsdato!!, -1))

            avdoedGrunnlag = setTrygdetid(
                TrygdetidGrunnlagSpec(
                    persongrunnlag = avdoedGrunnlag,
                    utlandAntallAar = simuleringSpec.avdoed?.antallAarUtenlands,
                    tom = lastDayOfYearBeforeDoedDato.toNorwegianLocalDate(),
                    forrigeAlderspensjonBeregningResultat = forrigeAlderspensjonBeregningResult,
                    simuleringSpec = simuleringSpec
                ),
                kravhode
            )

            log.debug { "STEP 4.5 - Sett uførehistorikk for avdød" }
            setUfoereHistorikk(avdoedGrunnlag)
        }

        return kravhode
    }

    // SimulerFleksibelAPCommand.settPensjonsbeholdning (part of)
    private fun fetchBeholdninger(grunnlag: Persongrunnlag, foersteUttakDato: LocalDate?) =
        context.beregnOpptjening(foersteUttakDato, grunnlag)
            .filter { BeholdningtypeEnum.PEN_B == it.beholdningsTypeEnum }

    private fun setUfoereHistorikk(persongrunnlag: Persongrunnlag) {
        setUfoereHistorikk(persongrunnlag, tom = persongrunnlag.dodsdato)
    }

    // PEN: SettUforehistorikkHelper.settUforehistorikk
    private fun setUfoereHistorikk(persongrunnlag: Persongrunnlag, tom: Date?) {
        val historikk = persongrunnlag.uforeHistorikk
        if (historikk?.uforeperiodeListe == null) return

        val historikkCopy = Uforehistorikk(historikk)

        historikkCopy.uforeperiodeListe.forEach {
            if (it.ufgTom == null) {
                it.ufgTom = tom
            }
        }

        persongrunnlag.uforeHistorikk = historikkCopy
    }

    private fun ufoerePeriodeTom(spec: SimuleringSpec, soekerGrunnlag: Persongrunnlag): Date {
        val sisteDagIMaanedenForNormalder: Date = sisteDagIMaanedenForNormalder(soekerGrunnlag.fodselsdato!!)

        return if (spec.type == SimuleringTypeEnum.ALDER_M_AFP_PRIVAT &&
            isBeforeByDay(
                thisDate = spec.foersteUttakDato,
                thatDate = sisteDagIMaanedenForNormalder,
                allowSameDay = false
            )
        )
            spec.foersteUttakDato!!.minusDays(1).toNorwegianDateAtNoon()
        else
            sisteDagIMaanedenForNormalder
    }

    // no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SimuleringEtter2011Utils.lastDayOfMonthUserTurns67
    private fun sisteDagIMaanedenForNormalder(foedselsdato: Date): Date =
        lastDayOfMonthUserTurnsGivenAge(
            foedselsdato,
            alder = normalderService.normalder(foedselsdato.toNorwegianLocalDate())
        )

    // SimulerFleksibelAPCommand.settTrygdetid
    private fun setTrygdetid(spec: TrygdetidGrunnlagSpec, kravhode: Kravhode): Persongrunnlag {
        val persongrunnlag = spec.persongrunnlag
        val simuleringSpec = spec.simuleringSpec

        if (simuleringSpec.erAnonym) {
            with(anonymSimuleringTrygdetidPeriode(spec)) {
                persongrunnlag.trygdetidPerioder.add(this)
                persongrunnlag.trygdetidPerioderKapittel20.add(this)
            }
            return persongrunnlag
        }

        if (simuleringSpec.boddUtenlands) {
            kravhode.boddEllerArbeidetIUtlandet = true
            val regelverkType = kravhode.regelverkTypeEnum!!

            if (regelverkType.isAlderspensjon2011) {
                addKapittel19Trygdetid(persongrunnlag, simuleringSpec)
            }

            if (regelverkType.isAlderspensjon2016) {
                addKapittel19Trygdetid(persongrunnlag, simuleringSpec)
                addKapittel20Trygdetid(persongrunnlag, simuleringSpec)
            }

            if (regelverkType.isAlderspensjon2025) {
                addKapittel20Trygdetid(persongrunnlag, simuleringSpec)
            }

            persongrunnlag.trygdeavtale = newTrygdeavtaleForSimuleringUtland(avtalelandKravdato = time.today())
            persongrunnlag.trygdeavtaledetaljer = newTrygdeavtaledetaljerForSimuleringUtland()

            persongrunnlag.inngangOgEksportGrunnlag =
                newInngangOgEksportGrunnlagForSimuleringUtland(persongrunnlag, kravhode)

            return persongrunnlag
        }

        return trygdetidSetter.settTrygdetid(spec)
    }

    // SimulerFleksibelAPCommand.setTrygetidKap19
    private fun addKapittel19Trygdetid(persongrunnlag: Persongrunnlag, spec: SimuleringSpec) {
        val periodeListe = kapittel19TrygdetidsperiodeListe(
            opptjeningsgrunnlagListe = persongrunnlag.opptjeningsgrunnlagListe,
            utlandPeriodeListe = spec.utlandPeriodeListe,
            foedselsdato = persongrunnlag.fodselsdato!!.toNorwegianLocalDate(),
            foersteUttakDato = spec.foersteAlderspensjonUttaksdato()
        )

        periodeListe.forEach { persongrunnlag.trygdetidPerioder.add(it) }
    }

    // SimulerFleksibelAPCommand.setTrygdetidKap20
    private fun addKapittel20Trygdetid(persongrunnlag: Persongrunnlag, spec: SimuleringSpec) {
        val periodeListe = kapittel20TrygdetidsperiodeListe(
            utlandPeriodeListe = spec.utlandPeriodeListe,
            foedselsdato = persongrunnlag.fodselsdato!!.toNorwegianLocalDate(),
            foersteUttakDato = spec.foersteAlderspensjonUttaksdato()
        )

        periodeListe.forEach { persongrunnlag.trygdetidPerioderKapittel20.add(it) }
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
