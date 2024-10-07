package no.nav.pensjon.simulator.core.beholdning

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SatsTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import no.nav.pensjon.simulator.core.domain.regler.to.HentGyldigSatsRequest
import no.nav.pensjon.simulator.core.domain.regler.to.RegulerPensjonsbeholdningRequest
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.createDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isSameDay
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findLatest
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findValidForDate
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate
import java.util.*

// Contains refactored code from BeholdningSwitchDecider, BeholdningSwitcherCommand, BeholdningSwitcherHelper,
// OppdaterPensjonsbeholdningerCommand, OppdaterPensjonsbeholdningerHelper, SimpleGrunnlagService.
object BeholdningUpdaterUtil {

    const val OPPTJENING_MINIMUM_ALDER = 13 // år
    const val OPPTJENING_MINIMUM_FODSELSAR = 1997 // OppdaterPensjonsbeholdningerHelper.YEAR_1997
    const val YEAR_2010 = OPPTJENING_MINIMUM_ALDER + OPPTJENING_MINIMUM_FODSELSAR
    private const val YEAR_2009 = 2009 // OppdaterPensjonsbeholdningerHelper.YEAR_2009
    private const val TILVEKST =
        "tilvekst" // OppdaterPensjonsbeholdningerHelper, OppdaterPensjonsbeholdningerRequestFactory

    // BeholdningHelper.updateBeholdningListeObject
    fun updateBeholdninger(
        persongrunnlag: Persongrunnlag,
        beholdningListe: List<Pensjonsbeholdning>
    ) {
        deleteBeholdningerFromPenPersongrunnlagObject(persongrunnlag)
        beholdningListe.forEach(persongrunnlag::addBeholdning)
    }

    // OppdaterPensjonsbeholdningerHelper.updateBeholdningAfterPREG
    fun postprocessBeholdning(
        pensjonBeholdning: Pensjonsbeholdning?,
        code: RestpensjonBeholdningOppdateringAarsak?,
        fom: Date?,
        tom: Date?
    ) {
        pensjonBeholdning?.let {
            /* This does not exist in pensjon-regler domain and is assumed to be irrelevant in simulering:
            it.oppdateringArsak = code
            */
            it.fom = fom
            it.tom = tom
        }
    }

    // BeholdningHelper.openLatestBeholdning
    fun openLatestBeholdning(persongrunnlag: Persongrunnlag) {
        findLatest(persongrunnlag.beholdninger)?.let { it.tom = null }
    }

    // BeholdningHelper.removeAllBeholdningAfter
    fun removeAllBeholdningAfter(beholdningListe: MutableList<Pensjonsbeholdning>, date: LocalDate?) {
        beholdningListe.removeIf {
            isAfterByDay(it.fom, fromLocalDate(date), true)
        }

        findLatest(beholdningListe)?.let { it.tom = null }
    }

    // OppdaterPensjonsbeholdningerCommand.setOmsorgsgrunnlagOnPersongrunnlagIfExists
    fun setOmsorgsgrunnlagOnPersongrunnlagIfExists(
        omsorgGrunnlagListe: List<Omsorgsgrunnlag>,
        soekerGrunnlag: Persongrunnlag
    ) {
        soekerGrunnlag.omsorgsgrunnlagListe = omsorgGrunnlagListe.map(::Omsorgsgrunnlag).toMutableList()
    }

    // OppdaterPensjonsbeholdningerCommand.setOpptjeningsgrunnlagOnPersongrunnlagIfExists
    fun setOpptjeningsgrunnlagOnPersongrunnlagIfExists(
        opptjeningGrunnlagListe: List<Opptjeningsgrunnlag>,
        soekerGrunnlag: Persongrunnlag
    ) {
        soekerGrunnlag.opptjeningsgrunnlagListe =
            opptjeningGrunnlagListe.map(::Opptjeningsgrunnlag).toMutableList()
    }

    // OppdaterPensjonsbeholdningerCommand.addDagpengeGrunnlagIfExists
    fun addDagpengeGrunnlagIfExists(
        beregningGrunnlag: BeholdningBeregningsgrunnlag,
        soekerGrunnlag: Persongrunnlag
    ) {
        beregningGrunnlag.dagpengerGrunnlagListe.forEach {
            soekerGrunnlag.dagpengegrunnlagListe.add(Dagpengegrunnlag(it))
        }
    }

    // OppdaterPensjonsbeholdningerCommand.addForstegangstjenesteIfExists
    fun addForstegangstjenesteIfExists(
        beregningGrunnlag: BeholdningBeregningsgrunnlag,
        soekerGrunnlag: Persongrunnlag
    ) {
        beregningGrunnlag.foerstegangstjenesteGrunnlag?.let {
            soekerGrunnlag.forstegangstjenestegrunnlag = Forstegangstjeneste(it)
        }
    }

    // BeholdningHelper.switchExistingBeholdningOnPersongrunnlag
    fun switchExistingBeholdningOnPersongrunnlag(persongrunnlag: Persongrunnlag, beholdning: Pensjonsbeholdning?) {
        persongrunnlag.beholdninger.removeIf { isSameDay(it.fom, beholdning?.fom) }
        beholdning?.let(persongrunnlag::addBeholdning)
    }

    /*ANON
    // OppdaterPensjonsbeholdningerHelper.vedtakWithRegelverkForAP2016AndAP2025
    fun vedtakWithRegelverkForAP2016AndAP2025(vedtak: Vedtak): Boolean {
        val regelverkType = vedtak.kravhode.regelverkType
        return RegelverkTypeEnum.N_REG_G_N_OPPTJ == regelverkType || RegelverkTypeEnum.N_REG_N_OPPTJ == regelverkType
    }
    */

    // BeholdningHelper.getPensjonsbeholdningOnGivenDate
    fun getPensjonsbeholdningOnGivenDate(beholdningListe: List<Pensjonsbeholdning>, date: LocalDate): Pensjonsbeholdning? {
        val pensjonBeholdningListe: MutableList<Pensjonsbeholdning> = mutableListOf()
        beholdningListe.forEach(pensjonBeholdningListe::add)
        return findValidForDate(pensjonBeholdningListe, fromLocalDate(date)!!) //TODO use filter instead?
    }

    // OppdaterPensjonsbeholdningerCommand.getSisteBeholdning
    fun getSisteBeholdning(beholdningListe: List<Pensjonsbeholdning>): Pensjonsbeholdning =
        beholdningListe[beholdningListe.size - 1]

    // OppdaterPensjonsbeholdningerHelper.createPersonBeholdning
    fun createPersonBeholdning(pid: Pid?, beholdningListe: MutableList<Pensjonsbeholdning>) =
        PersonBeholdning().apply {
            this.pid = pid
            this.beholdningListe = beholdningListe
        }

    // OppdaterPensjonsbeholdningerHelper.createEmptyBeholdning + createPersonBeholdning
    fun createEmptyBeholdning(pid: Pid?, sisteGyldigeOpptjeningAar: Int) =
        PersonBeholdning().apply {
            this.pid = pid
            beholdningListe = pensjonsbeholdningListe(sisteGyldigeOpptjeningAar)
        }

    // OppdaterPensjonsbeholdningerCommand.createPersongrunnlagFromPerson
    fun createPersongrunnlagFromPerson(person: PenPerson): Persongrunnlag =
        Persongrunnlag().apply {
            fodselsdato = person.fodselsdato
            penPerson = person

            person.uforehistorikk?.let {
                uforeHistorikk = it
                generellHistorikk = person.generellHistorikk
            }
        }.also {
            it.personDetaljListe.add(createPersonDetalj(person))
            it.finishInit()
        }

    // OppdaterPensjonsbeholdningerHelper.generatePersonPensjonsbeholdning
    fun generatePersonPensjonsbeholdning(persongrunnlag: Persongrunnlag, beholdning: Pensjonsbeholdning?) =
        PersonPensjonsbeholdning().apply {
            fodselsnummer = persongrunnlag.penPerson?.pid?.value
            pensjonsbeholdning = beholdning
        }

    // OppdaterPensjonsbeholdningerHelper.generateHentGyldigSatsConsumerRequest
    fun generateHentGyldigSatsConsumerRequest(aar: Int) =
        HentGyldigSatsRequest().apply {
            satsTypeEnum = SatsTypeEnum.LONNSVEKST
            fomDato = createDate(aar, Calendar.JANUARY, 1).noon()
            tomDato = createDate(aar, Calendar.DECEMBER, 31).noon()
        }

    // OppdaterPensjonsbeholdningerHelper.generateRegulerPensjonsbeholdningConsumerRequest
    fun newRegulerPensjonsbeholdningRequest(personPensjonBeholdningListe: List<PersonPensjonsbeholdning>, fom: Date?) =
        RegulerPensjonsbeholdningRequest().apply {
            beregningsgrunnlagForPensjonsbeholdning = ArrayList(personPensjonBeholdningListe)
            virkFom = fom
        }

    // OppdaterPensjonsbeholdningerHelper.hasOpptjeningBefore2009
    fun hasOpptjeningBefore2009(beregningGrunnlag: BeholdningBeregningsgrunnlag): Boolean {
        var hasOpptjeningBefore2009 = false

        for (omsorgsgrunnlag in beregningGrunnlag.omsorgGrunnlagListe) {
            if (omsorgsgrunnlag.ar < YEAR_2009) {
                hasOpptjeningBefore2009 = true
                break
            }
        }

        for (opptjeningsgrunnlag in beregningGrunnlag.opptjeningGrunnlagListe) {
            if (opptjeningsgrunnlag.ar < YEAR_2009) {
                hasOpptjeningBefore2009 = true
            }
        }

        for (dagpengegrunnlag in beregningGrunnlag.dagpengerGrunnlagListe) {
            if (dagpengegrunnlag.ar < YEAR_2009) {
                hasOpptjeningBefore2009 = true
                break
            }
        }

        val tjenestePerioder = beregningGrunnlag.foerstegangstjenesteGrunnlag?.periodeListe.orEmpty()

        for (tjenestePeriode in tjenestePerioder) {
            val aar = tjenestePeriode.ar()

            if (aar != null && aar < YEAR_2009) {
                hasOpptjeningBefore2009 = true
            }
        }

        return hasOpptjeningBefore2009 //TODO 2009
    }

    // OppdaterPensjonsbeholdningerHelper.verifyEmptyBeholdningAndGrunnlag
    // + OppdaterPensjonsbeholdningerHelper.isEmptyList
    fun verifyEmptyBeholdningAndGrunnlag(beholdning: Pensjonsbeholdning?, persongrunnlag: Persongrunnlag): Boolean =
        (beholdning == null
                && persongrunnlag.omsorgsgrunnlagListe.isEmpty()
                && persongrunnlag.opptjeningsgrunnlagListe.isEmpty()
                && persongrunnlag.forstegangstjenestegrunnlag == null)
                && persongrunnlag.dagpengegrunnlagListe.isEmpty()
                && persongrunnlag.uforeHistorikk == null

    // OppdaterPensjonsbeholdningerHelper.verifyBeholdningFom2010
    fun verifyBeholdningFom2010(fom: Date?): Boolean {
        val beholdningFom = createDate(YEAR_2010, Calendar.JANUARY, 1)
        return isSameDay(fom, beholdningFom)
    }

    // OppdaterPensjonsbeholdningerCommand.determineBeregnTomYear
    fun determineBeregnTomYear(foersteVirkningFom: Date?, sisteGyldigeOpptjeningAar: Int): Int =
        if (foersteVirkningFom != null) {
            if (getMonth(foersteVirkningFom) == Calendar.JANUARY) getYear(foersteVirkningFom) - 1 else getYear(foersteVirkningFom)
        } else
            sisteGyldigeOpptjeningAar + OPPTJENING_ETTERSLEP_ANTALL_AAR

    // OppdaterPensjonsbeholdningerHelper.findEarliestOpptjeningsAr
    fun tidligsteOpptjeningAar(
        beregningsgrunnlag: BeholdningBeregningsgrunnlag,
        persongrunnlag: Persongrunnlag
    ): Int {
        var tidligsteOpptjeningAar = 0

        if (beregningsgrunnlag.omsorgGrunnlagListe.isNotEmpty()) {
            tidligsteOpptjeningAar = beregningsgrunnlag.omsorgGrunnlagListe[0].ar
            for (omsorgsgrunnlag in beregningsgrunnlag.omsorgGrunnlagListe) {
                if (omsorgsgrunnlag.ar < tidligsteOpptjeningAar) {
                    tidligsteOpptjeningAar = omsorgsgrunnlag.ar
                }
            }
        }

        if (beregningsgrunnlag.opptjeningGrunnlagListe.isNotEmpty()) {
            tidligsteOpptjeningAar = beregningsgrunnlag.opptjeningGrunnlagListe[0].ar

            for (opptjening in beregningsgrunnlag.opptjeningGrunnlagListe) {
                if (opptjening.ar < tidligsteOpptjeningAar) {
                    tidligsteOpptjeningAar = opptjening.ar
                }
            }
        }

        if (beregningsgrunnlag.dagpengerGrunnlagListe.isNotEmpty()) {
            if (tidligsteOpptjeningAar == 0) {
                tidligsteOpptjeningAar = beregningsgrunnlag.dagpengerGrunnlagListe[0].ar
            }

            for (dagpengegrunnlag in beregningsgrunnlag.dagpengerGrunnlagListe) {
                if (dagpengegrunnlag.ar < tidligsteOpptjeningAar) {
                    tidligsteOpptjeningAar = dagpengegrunnlag.ar
                }
            }
        }

        val tjenestePerioder = beregningsgrunnlag.foerstegangstjenesteGrunnlag?.periodeListe.orEmpty()

        if (tjenestePerioder.isNotEmpty()) {
            if (tidligsteOpptjeningAar == 0) {
                tidligsteOpptjeningAar = tjenestePerioder[0].ar() ?: 0
            }

            tjenestePerioder.forEach {
                val aar = it.ar()

                if (aar != null && aar < tidligsteOpptjeningAar) {
                    tidligsteOpptjeningAar = aar
                }
            }
        }

        //PENPORT-273: Tar utgangspunkt i uførehistorikken dersom ingen opptjening finnes
        if (tidligsteOpptjeningAar == 0 && persongrunnlag.uforeHistorikk != null) {
            val uforeperiodeListe = persongrunnlag.uforeHistorikk?.realUforePeriodeList().orEmpty()

            if (uforeperiodeListe.isNotEmpty()) {
                val calendar = Calendar.getInstance().apply { time = uforeperiodeListe[0].ufgFom }
                tidligsteOpptjeningAar = calendar[Calendar.YEAR]

                for (uforeperiode in uforeperiodeListe) {
                    calendar.time = uforeperiode.ufgFom

                    if (calendar[Calendar.YEAR] < tidligsteOpptjeningAar) {
                        tidligsteOpptjeningAar = calendar[Calendar.YEAR]
                    }
                }

                val foersteMuligeOpptjeningAar: Int =
                    calculateFirstPossibleOpptjeningsar(persongrunnlag.penPerson?.fodselsdato)

                if (tidligsteOpptjeningAar < foersteMuligeOpptjeningAar) {
                    tidligsteOpptjeningAar = foersteMuligeOpptjeningAar
                }
            }
        }

        if (tidligsteOpptjeningAar < YEAR_2009) {
            tidligsteOpptjeningAar = YEAR_2009
        }

        return tidligsteOpptjeningAar
    }

    // OppdaterPensjonsbeholdningerHelper.findRegularDate
    fun findReguleringDato(reguleringDatoListe: List<SatsResultat>): Date? {
        var foersteReguleringDato: Date? = null

        for (satsResultat in reguleringDatoListe) {
            if (foersteReguleringDato == null) {
                foersteReguleringDato = satsResultat.fom
            } else {
                if (isBeforeByDay(satsResultat.fom, foersteReguleringDato, false)) {
                    foersteReguleringDato = satsResultat.fom
                }
            }
        }

        return foersteReguleringDato
    }

    // OppdaterPensjonsbeholdningerHelper.firstDateOf
    fun firstDateOf(reguleringDato: Date?, foersteVirkningFom: Date?): Date? =
        if (foersteVirkningFom == null) {
            reguleringDato
        } else {
            if (isBeforeByDay(foersteVirkningFom, reguleringDato, true)) foersteVirkningFom else reguleringDato
        }

    fun firstDayOf(beholdning: Pensjonsbeholdning): Date = createDate(beholdning.ar, Calendar.JANUARY, 1)

    fun lastDayOf(beholdning: Pensjonsbeholdning): Date = createDate(beholdning.ar, Calendar.DECEMBER, 31)

    // BeholdningSwitchDecider.beholdningShouldBeSwitched
    fun isVirkFomEligibleForSwitching(kravhode: Kravhode): Boolean {
        val onsketVirkningsDato = kravhode.onsketVirkningsdato
        return isFirstDayOfYear(onsketVirkningsDato) || isFirstDayOfMay(onsketVirkningsDato)
    }

    // BeholdningSwitchDecider.isSokersPersongrunnlag
    fun isSokersPersongrunnlag(kravhode: Kravhode, persongrunnlag: Persongrunnlag): Boolean =
        // persongrunnlag.gjeldendeFodselsnummer == kravHode.sak.penPerson.fnr
        persongrunnlag.penPerson?.pid == kravhode.sakPenPersonFnr //TODO where is sakPenPersonFnr assigned?

    // BeholdningSwitchDecider.kravIsAp2016OrAp2025
    fun kravIsAp2016OrAp2025(kravhode: Kravhode): Boolean {
        val regelverkType = kravhode.regelverkTypeEnum
        return RegelverkTypeEnum.N_REG_G_N_OPPTJ == regelverkType || RegelverkTypeEnum.N_REG_N_OPPTJ == regelverkType
    }

    // RevurderingHelper.isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling
    fun isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling(kravhode: Kravhode): Boolean =
        with(kravhode.sakForsteVirkningsdato()) {
            this == null || isSameDay(this, kravhode.onsketVirkningsdato)
        }

    // RevurderingHelper.isFirstDayOfYear
    //fun isFirstDayOfYear(date: Date?): Boolean = DateTime(date).dayOfYear == 1
    fun isFirstDayOfYear(date: Date?): Boolean =
        date.toLocalDate()?.dayOfYear == 1

    // RevurderingHelper.isFirstDayOfMay
    fun isFirstDayOfMay(date: Date?): Boolean =
        with(date.toLocalDate()!!) {
            dayOfMonth == 1 && monthValue == 5
            //TODO use const
        }

    // OppdaterPensjonsbeholdningerRequestFactory.createWithTilvekst
    fun createWithTilvekst(grunnlag: BeholdningBeregningsgrunnlag, sisteGyldigeOpptjeningAar: String) =
        BeholdningUpdateSpec().apply {
            pensjonBeholdningBeregningGrunnlag = listOf(grunnlag)
            this.sisteGyldigeOpptjeningAar = sisteGyldigeOpptjeningAar
            opptjeningModus = TILVEKST
        }

    // OppdaterPensjonsbeholdningerHelper.calculateFirstPossibleOpptjeningsar
    private fun calculateFirstPossibleOpptjeningsar(foedselDato: Date?): Int {
        val calendar = Calendar.getInstance()
        calendar[OPPTJENING_MINIMUM_FODSELSAR, 0] = 0
        val minimumDato = calendar.time

        return if (isBeforeDay(foedselDato, minimumDato)) {
            OPPTJENING_MINIMUM_FODSELSAR + OPPTJENING_MINIMUM_ALDER
        } else {
            calendar.time = foedselDato
            calendar.add(Calendar.YEAR, OPPTJENING_MINIMUM_ALDER)
            calendar[Calendar.YEAR]
        }
    }

    // Extract from OppdaterPensjonsbeholdningerHelper.createEmptyBeholdning + createPersonBeholdning
    private fun pensjonsbeholdningListe(sisteGyldigeOpptjeningAar: Int): MutableList<Pensjonsbeholdning> =
        mutableListOf(
            Pensjonsbeholdning().apply {
                ar = sisteGyldigeOpptjeningAar + 2
                fom = createDate(sisteGyldigeOpptjeningAar + 2, Calendar.JANUARY, 1)
                tom = null
                totalbelop = 0.0
                /* This does not exist in pensjon-regler domain and is assumed to be irrelevant in simulering:
                oppdateringArsak = RestpensjonBeholdningOppdateringAarsak.NY_OPPTJENING
                */
            })

    // OppdaterPensjonsbeholdningerHelper.createPersonDetalj (cf. Ap2025PersongrunnlagMapper.createPersonDetalj)
    private fun createPersonDetalj(person: PenPerson) =
        PersonDetalj().apply {
            bruk = true
            rolleFomDato = person.fodselsdato
            grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
            grunnlagKildeEnum = GrunnlagkildeEnum.PEN
        }.also { it.finishInit() } //NB: Assuming finishInit is appropriate here

    // BeholdningHelper.deleteBeholdningerFromPenPersongrunnlagObject
    private fun deleteBeholdningerFromPenPersongrunnlagObject(persongrunnlag: Persongrunnlag) {
        for (beholdning in persongrunnlag.beholdninger) {
            persongrunnlag.beholdninger.remove(beholdning) //TODO check this
        }

        persongrunnlag.clearBeholdningListe()
    }
}
