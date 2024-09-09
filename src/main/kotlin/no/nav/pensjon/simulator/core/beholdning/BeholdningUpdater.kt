package no.nav.pensjon.simulator.core.beholdning

import no.nav.pensjon.simulator.core.exception.BeregningsmotorValidereException
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.YEAR_2010
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.addDagpengeGrunnlagIfExists
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.addForstegangstjenesteIfExists
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.createEmptyBeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.createPersonBeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.createPersongrunnlagFromPerson
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.createWithTilvekst
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.determineBeregnTomYear
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.tidligsteOpptjeningAar
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.findReguleringDato
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.firstDateOf
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.firstDayOf
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.generateHentGyldigSatsConsumerRequest
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.generatePersonPensjonsbeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.getPensjonsbeholdningOnGivenDate
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.getSisteBeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.hasOpptjeningBefore2009
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.isFirstDayOfMay
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.isFirstDayOfYear
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.isSokersPersongrunnlag
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.isVirkFomEligibleForSwitching
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.kravIsAp2016OrAp2025
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.lastDayOf
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.newRegulerPensjonsbeholdningRequest
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.openLatestBeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.removeAllBeholdningAfter
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.setOmsorgsgrunnlagOnPersongrunnlagIfExists
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.setOpptjeningsgrunnlagOnPersongrunnlagIfExists
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.switchExistingBeholdningOnPersongrunnlag
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.updateBeholdningAfterPreg
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.updateBeholdninger
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.verifyBeholdningFom2010
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.verifyEmptyBeholdningAndGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonPensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.kode.SatsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import no.nav.pensjon.simulator.core.domain.regler.to.HentGyldigSatsRequest
import no.nav.pensjon.simulator.core.domain.regler.to.RegulerPensjonsbeholdningRequest
import no.nav.pensjon.simulator.core.domain.regler.to.SatsResponse
import no.nav.pensjon.simulator.core.exception.KanIkkeBeregnesException
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.createDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findLatest
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.person.Pid
import org.springframework.util.StringUtils.hasLength
import java.time.LocalDate
import java.util.*

// Contains refactored code from BeholdningSwitchDecider, BeholdningSwitcherCommand, BeholdningSwitcherHelper,
// OppdaterPensjonsbeholdningerCommand, OppdaterPensjonsbeholdningerHelper, SimpleGrunnlagService.
// Ref. TPEN547 - oppdaterPensjonsbeholdninger
// https://pensjon-dokumentasjon.intern.dev.nav.no/pen/Tjenester/TPEN547_oppdaterPensjonsbeholdninger.html
class BeholdningUpdater(val context: SimulatorContext) {

    // SimpleGrunnlagService.updateBeholdningFromEksisterendePersongrunnlag -> BeholdningSwitcherCommand.updateBeholdningFromEksisterendePersongrunnlag
    fun updateBeholdningFromEksisterendePersongrunnlag(nyttKravhode: Kravhode) {
        val persongrunnlag: Persongrunnlag = nyttKravhode.hentPersongrunnlagForSoker()
        val kopiertPersongrunnlag =
            Persongrunnlag(persongrunnlag).also { it.finishInit() } // NB: Assuming this copies all relevant data
        updateBeholdninger(persongrunnlag, kopiertPersongrunnlag.beholdninger)
        updateBeholdningOnVirk(nyttKravhode, kopiertPersongrunnlag)
    }

    // OppdaterPensjonsbeholdningerHelper.findPenPerson ?.let(Ap2025KjerneToSimuleringDiverseMapper::mapPenPerson)
    private fun findPenPerson(fnrListe: List<Pid>): Map<String, PenPerson> {
        /*ANON
        val personerByPid: MutableMap<String, PenPerson> = HashMap()
        val personer = context.getPersoner(fnrListe).map(Ap2025KjerneToSimuleringDiverseMapper::mapPenPerson)

        for (person in personer) {
            person.pid?.pid?.let { personerByPid[it] = person }
        }

        return personerByPid
        */
        return emptyMap()
    }

    // OppdaterPensjonsbeholdningerHelper.determineFirstVirkOfAP2016andAP2025
    private fun determineFirstVirkOfAP2016andAP2025(pid: Pid): Date? =
    // Only pid and sakType set in legacy BestemGjeldendeVedtakRequest
        /*ANON
        SimulatorVedtakService(context).bestemGjeldendeVedtak(pid) // vedtakServiceBi.bestemGjeldendeVedtak -> BestemGjeldendeVedtakCommand.execute
            .filter(BeholdningUpdaterUtil::vedtakWithRegelverkForAP2016AndAP2025)
            .minOfOrNull { it.gjelderFom }
        */
        null

    // OppdaterPensjonsbeholdningerHelper.getUforeOpptjeningGrunnlagCopy
    private fun getUforeOpptjeningGrunnlagCopy(penPersonId: Long): MutableList<UtbetalingsgradUT> =
        /*ANON
        context.hentUforeOpptjeningGrunnlag(penPersonId)
            ?.let(Ap2025KjerneToSimuleringUforeMapper::mapUforeOpptjeningGrunnlag).orEmpty()
            .toMutableList()
        */
        mutableListOf()

    // OppdaterPensjonsbeholdningerCommand.updatePensjonsbeholdning
    private fun updatePensjonsbeholdning(
        beregningsgrunnlag: BeholdningBeregningsgrunnlag,
        persongrunnlag: Persongrunnlag?,
        sisteGyldigOpptjArPopp: Int
    ): PersonBeholdning {
        val forrigeBeholdning = beregningsgrunnlag.beholdning
        val tomBeholdning = LocalDate.of(sisteGyldigOpptjArPopp + OPPTJENING_ETTERSLEP_ANTALL_AAR, Calendar.JANUARY, 1)
        val beholdninger: MutableList<Pensjonsbeholdning> =
            beregnOpptjening(persongrunnlag, tomBeholdning, forrigeBeholdning)
        val oppdatertBeholdning: Pensjonsbeholdning = getSisteBeholdning(beholdninger)
        val oppdatertBeholdningFomDate = firstDayOf(oppdatertBeholdning)

        updateBeholdningAfterPreg(
            oppdatertBeholdning,
            RestpensjonBeholdningOppdateringAarsak.NY_OPPTJENING,
            oppdatertBeholdningFomDate,
            null
        )
        return createPersonBeholdning(beregningsgrunnlag.pid, mutableListOf(oppdatertBeholdning))
    }

    // OppdaterPensjonsbeholdningerHelper.beregnOpptjening
    private fun beregnOpptjening(
        persongrunnlag: Persongrunnlag?,
        tomBeholdning: LocalDate?,
        forrigeBeholdning: Pensjonsbeholdning?
    ): MutableList<Pensjonsbeholdning> =
        try {
            persongrunnlag?.let { context.beregnOpptjening(tomBeholdning, it, forrigeBeholdning) } ?: mutableListOf()
        } catch (e: KanIkkeBeregnesException) {
            //throw ImplementationUnrecoverableException("The rules engine cannot calculate input for personId:" + persongrunnlag?.penPerson?.penPersonId, e)
            throw RuntimeException(
                "The rules engine cannot calculate input for personId:" + persongrunnlag?.penPerson?.penPersonId,
                e
            )
        } catch (e: BeregningsmotorValidereException) {
            //throw ImplementationUnrecoverableException(("The rules engine cannot calculate input for personId:" + persongrunnlag?.penPerson?.penPersonId), e)
            throw RuntimeException(
                ("The rules engine cannot calculate input for personId:" + persongrunnlag?.penPerson?.penPersonId),
                e
            )
        }

    // OppdaterPensjonsbeholdningerHelper.hentGyldigSats
    private fun hentGyldigSats(year: Int): List<SatsResultat> =
        context.fetchGyldigSats(generateHentGyldigSatsConsumerRequest(year)).satsResultater

    // OppdaterPensjonsbeholdningerCommand.populatePersongrunnlag
    private fun populatePersongrunnlag(
        person: PenPerson,
        beregningsgrunnlag: BeholdningBeregningsgrunnlag,
        opptjeningsModus: String,
        sisteGyldigeOpptjArPopp: String
    ): Persongrunnlag {
        if (opptjeningsModus != INITIERING && beregningsgrunnlag.ufoerFoer2009) {
            //throw ImplementationUnrecoverableException(
            throw RuntimeException(
                "Invalid combination with opptjeningsModus: $opptjeningsModus with UforFoer2009 is TRUE."
            )
        }

        val persongrunnlag: Persongrunnlag = createPersongrunnlagFromPerson(person)
        setOpptjeningsgrunnlagOnPersongrunnlagIfExists(beregningsgrunnlag.opptjeningGrunnlagListe, persongrunnlag)
        setOmsorgsgrunnlagOnPersongrunnlagIfExists(beregningsgrunnlag.omsorgGrunnlagListe, persongrunnlag)
        addForstegangstjenesteIfExists(beregningsgrunnlag, persongrunnlag)
        addDagpengeGrunnlagIfExists(beregningsgrunnlag, persongrunnlag)

        if (hasLength(sisteGyldigeOpptjArPopp)) {
            persongrunnlag.sisteGyldigeOpptjeningsAr = sisteGyldigeOpptjArPopp.toInt()
        }

        persongrunnlag.utbetalingsgradUTListe = getUforeOpptjeningGrunnlagCopy(person.penPersonId)
        return persongrunnlag
    }

    // OppdaterPensjonsbeholdningerCommand.createPensjonsbeholdning2010
    private fun createPensjonsbeholdning2010(
        beregningsgrunnlag: BeholdningBeregningsgrunnlag,
        persongrunnlag: Persongrunnlag
    ): PersonBeholdning {
        val tomBeholdning = LocalDate.of(YEAR_2010, Calendar.JANUARY, 1)
        val beholdninger: MutableList<Pensjonsbeholdning> = beregnOpptjening(persongrunnlag, tomBeholdning, null)

        for (beholdning in beholdninger) {
            val fom = firstDayOf(beholdning)
            val tom = if (beholdning.ar == YEAR_2010) null else lastDayOf(beholdning)
            updateBeholdningAfterPreg(beholdning, RestpensjonBeholdningOppdateringAarsak.NY_OPPTJENING, fom, tom)
        }

        return createPersonBeholdning(beregningsgrunnlag.pid, beholdninger)
    }

    // BeholdningSwitcherHelper.updateBeholdningOnVirkWithTilvekstBeholdning
    private fun updateBeholdningOnVirkWithTilvekstBeholdning(
        persongrunnlag: Persongrunnlag,
        kravhode: Kravhode,
        pensjonsbeholdning: Pensjonsbeholdning?
    ) {
        val grunnlag = BeholdningBeregningsgrunnlag().apply {
            pid = persongrunnlag.penPerson?.pid
            dagpengerGrunnlagListe = persongrunnlag.dagpengegrunnlagListe
            foerstegangstjenesteGrunnlag = persongrunnlag.forstegangstjenestegrunnlag
            opptjeningGrunnlagListe = persongrunnlag.opptjeningsgrunnlagListe
            omsorgGrunnlagListe = persongrunnlag.omsorgsgrunnlagListe
            beholdning = pensjonsbeholdning
        }

        val sisteGyldigeOpptjArPopp =
            kravhode.onsketVirkningsdato.toLocalDate()?.minusYears(
                OPPTJENING_ETTERSLEP_ANTALL_AAR.toLong()
            )?.year.toString()

        val request = createWithTilvekst(grunnlag, sisteGyldigeOpptjArPopp)
        val personBeholdningListe = oppdaterPensjonsbeholdninger(request).personBeholdningListe

        if (personBeholdningListe.isNotEmpty()) {
            val personBeholdning: PersonBeholdning = personBeholdningListe[0]
            val beholdningListe = personBeholdning.beholdningListe

            if (beholdningListe.isNotEmpty()) {
                // Since "tilvekst" then PREG will only return one Beholdning.
                switchExistingBeholdningOnPersongrunnlag(persongrunnlag, beholdningListe[0])
            }
        }
    }

    // OppdaterPensjonsbeholdningerCommand.execute
    private fun oppdaterPensjonsbeholdninger(request: BeholdningUpdateSpec): BeholdningUpdateResult {
        //helper.validateInput(request) TODO
        val personBeholdningListe: MutableList<PersonBeholdning> = mutableListOf()
        val fnrIkkeFunnetListe: MutableList<Pid> = mutableListOf()
        oppdaterBeholdning(request, personBeholdningListe, fnrIkkeFunnetListe)
        return BeholdningUpdateResult(personBeholdningListe, fnrIkkeFunnetListe)
    }

    // OppdaterPensjonsbeholdningerCommand.oppdaterBeholdning
    private fun oppdaterBeholdning(
        request: BeholdningUpdateSpec,
        personBeholdningListe: MutableList<PersonBeholdning>,
        fnrIkkeFunnetListe: MutableList<Pid>
    ) {
        val fnrListe: List<Pid> =
            request.pensjonBeholdningBeregningGrunnlag.mapNotNull { it.pid } // OppdaterPensjonsbeholdningerHelper.getFnrListe
        val penPersonMap: Map<String, PenPerson> = findPenPerson(fnrListe)

        for (beregningsgrunnlag in request.pensjonBeholdningBeregningGrunnlag) {
            var personBeholdning: PersonBeholdning? = null
            val penPerson = beregningsgrunnlag.pid?.value?.let { penPersonMap[it] }

            // If penPerson does not exist, add the person in fnrIkkeFunnet list:
            if (penPerson == null) {
                beregningsgrunnlag.pid?.let(fnrIkkeFunnetListe::add)
            } else {
                // Call 5.1.1 - Popular persongrunnlag
                val persongrunnlag: Persongrunnlag = populatePersongrunnlag(
                    penPerson,
                    beregningsgrunnlag,
                    request.opptjeningModus,
                    request.sisteGyldigeOpptjeningAar
                )
                val sisteGyldigOpptjArPopp = request.sisteGyldigeOpptjeningAar.toInt()

                // Scenario 0: If beholdningsyear is before 2010, thrown exception
                val beholdning = beregningsgrunnlag.beholdning
                if (beholdning?.ar?.let { it < YEAR_2010 } == true) {
                    //throw ImplementationUnrecoverableException("berGrForPensjonsbeholdning.beholdning.ar er FØR 2010")
                    throw RuntimeException("berGrForPensjonsbeholdning.beholdning.ar er FØR 2010")
                } else if (verifyEmptyBeholdningAndGrunnlag(beholdning, persongrunnlag)) {
                    personBeholdning = createEmptyBeholdning(beregningsgrunnlag.pid, sisteGyldigOpptjArPopp)

                    // Scenario 1: Initering - initial pensjonsbeholdning from 01.01.2010
                } else if (request.opptjeningModus == INITIERING && beholdning == null) {
                    // Call 5.2
                    personBeholdning = createPensjonsbeholdning2010(beregningsgrunnlag, persongrunnlag)

                    // Scenario 2: Tilvekst - update pensjonsbeholdning by new opptjening
                } else if (request.opptjeningModus == TILVEKST) {
                    // Call 5.3
                    personBeholdning =
                        updatePensjonsbeholdning(beregningsgrunnlag, persongrunnlag, sisteGyldigOpptjArPopp)

                    // Scenario 3: Korrigering - uupdate pensjonsbeholdning by corrected opptjening
                } else if (request.opptjeningModus == KORRIGERING) {
                    // Call 5.4 - if beholdning informasjon is sent as input to the service applies before last year
                    personBeholdning = adjustedPensjonsbeholdning(
                        beregningsgrunnlag,
                        persongrunnlag,
                        sisteGyldigOpptjArPopp,
                        request.beregnBeholdningUtenUttak
                    )
                }

                // Sjekk at beholdning er oppdatert for bruker
                if (personBeholdning == null) {
                    //throw ImplementationUnrecoverableException("The user exists but the pensjonsbeholdning has not been updated.")
                    throw RuntimeException("The user exists but the pensjonsbeholdning has not been updated.")
                }

                personBeholdningListe.add(personBeholdning)
            }
        }
    }

    // OppdaterPensjonsbeholdningerCommand.adjustedPensjonsbeholdning
    private fun adjustedPensjonsbeholdning(
        beregningsgrunnlag: BeholdningBeregningsgrunnlag,
        persongrunnlag: Persongrunnlag,
        sisteGyldigOpptjArPopp: Int,
        beregnBeholdningUtenUttak: Boolean
    ): PersonBeholdning {
        var forrigeBeholdning: Pensjonsbeholdning? = beregningsgrunnlag.beholdning
        val beholdninger: MutableList<Pensjonsbeholdning> = mutableListOf()

        if (beregningsgrunnlag.beholdning == null && hasOpptjeningBefore2009(beregningsgrunnlag)) {
            beholdninger.addAll(calculateTheInventoryTo2010(persongrunnlag))
            // Update forrigebeholdning with the newest beholdning
            forrigeBeholdning = getSisteBeholdning(beholdninger)
        } else {
            beregningsgrunnlag.beholdning?.let { forrigeBeholdning = it }
        }

        // Call 5.4.2 calculate beregning after 2010
        beholdninger.addAll(
            calculateTheInventoryAfter2010(
                beregningsgrunnlag,
                persongrunnlag,
                forrigeBeholdning,
                sisteGyldigOpptjArPopp,
                beregnBeholdningUtenUttak
            )
        )
        return createPersonBeholdning(beregningsgrunnlag.pid, beholdninger)
    }

    // OppdaterPensjonsbeholdningerCommand.calculateTheInventoryTo2010
    private fun calculateTheInventoryTo2010(persongrunnlag: Persongrunnlag): List<Pensjonsbeholdning> {
        val beholdningTom = LocalDate.of(YEAR_2010, Calendar.JANUARY, 1)
        val beholdningerFor2010: List<Pensjonsbeholdning> = beregnOpptjening(persongrunnlag, beholdningTom, null)

        for (beholdning in beholdningerFor2010) {
            val fom = firstDayOf(beholdning)
            val tom = lastDayOf(beholdning)
            updateBeholdningAfterPreg(beholdning, RestpensjonBeholdningOppdateringAarsak.NY_OPPTJENING, fom, tom)
        }

        return beholdningerFor2010
    }

    // OppdaterPensjonsbeholdningerCommand.calculateTheInventoryAfter2010
    private fun calculateTheInventoryAfter2010(
        beregningsgrunnlag: BeholdningBeregningsgrunnlag,
        persongrunnlag: Persongrunnlag,
        beholdning: Pensjonsbeholdning?,
        sisteGyldigOpptjArPopp: Int,
        beregnBeholdningUtenUttak: Boolean
    ): List<Pensjonsbeholdning> {
        val beholdningerEtter2010: MutableList<Pensjonsbeholdning> = mutableListOf()
        val firstVirkFom: Date? =
            if (beregnBeholdningUtenUttak) null else beregningsgrunnlag.pid?.let(::determineFirstVirkOfAP2016andAP2025)
        val beregnTomYear: Int = determineBeregnTomYear(firstVirkFom, sisteGyldigOpptjArPopp)
        var forrigeBeholdning: Pensjonsbeholdning? = beholdning

        val startYear: Int =
            if (forrigeBeholdning != null) {
                if (verifyBeholdningFom2010(forrigeBeholdning.fom)) {
                    // Call 5.4.3 reguler beholdning for 2010
                    val regulertBeholdning2010: Pensjonsbeholdning? =
                        regulerPensjonsbeholdning2010(forrigeBeholdning, persongrunnlag)
                    regulertBeholdning2010?.let(beholdningerEtter2010::add)
                }

                forrigeBeholdning.ar + 1
            } else {
                tidligsteOpptjeningAar(beregningsgrunnlag, persongrunnlag) + 2
            }

        // Calculate from year 2011 to currentYear and regular beholdning
        for (year in startYear..beregnTomYear) {
            var pensjonsbeholdning: Pensjonsbeholdning

            // Call BEF3270 - Beregn opptjening
            val beholdningTom = LocalDate.of(year, Calendar.JANUARY, 1)
            val beholdninger: List<Pensjonsbeholdning> =
                beregnOpptjening(persongrunnlag, beholdningTom, forrigeBeholdning)
            pensjonsbeholdning = getSisteBeholdning(beholdninger)

            // Call BEF3173 - HentGyldigSats
            val reguleringDatoer: List<SatsResultat> = hentGyldigSats(year)
            val forsteReguleringDato: Date? = findReguleringDato(reguleringDatoer)
            val pensjonsbeholdningFom = firstDayOf(pensjonsbeholdning)
            updateBeholdningAfterPreg(
                pensjonsbeholdning,
                RestpensjonBeholdningOppdateringAarsak.NY_OPPTJENING,
                pensjonsbeholdningFom,
                null
            )
            val isReguleringsdatoListeEmpty = reguleringDatoer.isEmpty()

            // Senario 1: PREG return empty sats, and it is not the last beholdning which shall be calculated.
            if (isReguleringsdatoListeEmpty && pensjonsbeholdning.ar != beregnTomYear) {
                pensjonsbeholdning.tom = lastDayOf(pensjonsbeholdning)
                beholdningerEtter2010.add(pensjonsbeholdning)
            } else if (isReguleringsdatoListeEmpty && pensjonsbeholdning.ar == beregnTomYear) {
                // Senario 2: PREG return empty sats, and it is the last beholdning which shall be calculated. If bruker has AP2016/2025,
                // firstVirkFom will not be null and the last pensjonsbeholdning must get a tomDate the day before firstVirkFom.
                pensjonsbeholdning.tom = firstVirkFom?.let { getRelativeDateByDays(it, -1) }
                beholdningerEtter2010.add(pensjonsbeholdning)
            } else if (!isReguleringsdatoListeEmpty) {
                // Senario 3: PREG return sats
                if (firstVirkFom?.let { isBeforeByDay(it, forsteReguleringDato, true) } == true) {
                    pensjonsbeholdning.tom = getRelativeDateByDays(firstVirkFom, -1)
                    beholdningerEtter2010.add(pensjonsbeholdning)
                } else {
                    pensjonsbeholdning.tom = getRelativeDateByDays(forsteReguleringDato!!, -1)
                    beholdningerEtter2010.add(pensjonsbeholdning)

                    val personPensjonsbeholdning: PersonPensjonsbeholdning =
                        generatePersonPensjonsbeholdning(persongrunnlag, pensjonsbeholdning)

                    val regulerteBeholdninger: List<Pensjonsbeholdning> = calculateBeholdningAfterRegulering(
                        reguleringDatoer,
                        personPensjonsbeholdning,
                        beregnTomYear,
                        year,
                        firstVirkFom
                    )

                    forrigeBeholdning = findLatest(regulerteBeholdninger)
                    beregningsgrunnlag.beholdning = forrigeBeholdning
                    beholdningerEtter2010.addAll(regulerteBeholdninger)
                }
            }
        }

        return beholdningerEtter2010
    }

    // OppdaterPensjonsbeholdningerHelper.calculateBeholdningAfterRegulering
    private fun calculateBeholdningAfterRegulering(
        reguleringDatoListe: List<SatsResultat>,
        personPensjonsbeholdning: PersonPensjonsbeholdning?,
        beregnTomYear: Int,
        year: Int,
        firstVirkFom: Date?
    ): List<Pensjonsbeholdning> {
        val regulerteBeholdninger: MutableList<Pensjonsbeholdning> = mutableListOf()

        for (index in reguleringDatoListe.indices) {
            val satsResultat = reguleringDatoListe[index]
            val reguleringDato = satsResultat.fom

            if (firstVirkFom == null || isBeforeByDay(reguleringDato, firstVirkFom, false)) {
                val regulertPersonPensjonsbeholdningListe: List<PersonPensjonsbeholdning> =
                    regulerPensjonsbeholdning(personPensjonsbeholdning, reguleringDato)

                if (regulertPersonPensjonsbeholdningListe.isNotEmpty()) {
                    val regulertBeholdning = regulertPersonPensjonsbeholdningListe[0].pensjonsbeholdning
                    var beholdningTomDato: Date? = null

                    if (reguleringDatoListe.size > index + 1) {
                        val nextReguleringDato: Date? =
                            reguleringDatoListe[index + 1].fom // OppdaterPensjonsbeholdningerHelper.getNextReguleringsdato
                        beholdningTomDato = getRelativeDateByDays(firstDateOf(nextReguleringDato, firstVirkFom)!!, -1)
                    } else if (beregnTomYear != year) {
                        beholdningTomDato = createDate(year, Calendar.DECEMBER, 31)
                    } else if (firstVirkFom != null) {
                        beholdningTomDato = getRelativeDateByDays(firstVirkFom, -1)
                    }

                    updateBeholdningAfterPreg(
                        regulertBeholdning,
                        RestpensjonBeholdningOppdateringAarsak.REGULERING,
                        reguleringDato,
                        beholdningTomDato
                    )
                    regulertBeholdning?.let(regulerteBeholdninger::add)
                }
            }
        }

        return regulerteBeholdninger
    }

    // OppdaterPensjonsbeholdningerHelper.regulerPensjonsbeholdning
    private fun regulerPensjonsbeholdning(
        personPensjonsbeholdning: PersonPensjonsbeholdning?,
        virkFom: Date?
    ): List<PersonPensjonsbeholdning> {
        val personPensjonsbeholdninger: MutableList<PersonPensjonsbeholdning> = mutableListOf()
        personPensjonsbeholdning?.let(personPensjonsbeholdninger::add)
        val request: RegulerPensjonsbeholdningRequest =
            newRegulerPensjonsbeholdningRequest(personPensjonsbeholdninger, virkFom)

        try {
            // DefaultBeregningConsumerService.regulerPensjonsbeholdning -> RegulerPensjonsbeholdningConsumerCommand.execute
            return context.regulerPensjonsbeholdning(request).regulertBeregningsgrunnlagForPensjonsbeholdning.toList()
        } catch (e: KanIkkeBeregnesException) {
            //throw ImplementationUnrecoverableException("The rules engine cannot calculate input for beholdning: " + personPensjonsbeholdning?.pensjonsbeholdning, e)
            throw RuntimeException(
                "The rules engine cannot calculate input for beholdning: " + personPensjonsbeholdning?.pensjonsbeholdning,
                e
            )
        } catch (e: BeregningsmotorValidereException) {
            //throw ImplementationUnrecoverableException("The rules engine cannot calculate input for beholdning: " + personPensjonsbeholdning?.pensjonsbeholdning, e)
            throw RuntimeException(
                "The rules engine cannot calculate input for beholdning: " + personPensjonsbeholdning?.pensjonsbeholdning,
                e
            )
        }
    }

    // OppdaterPensjonsbeholdningerCommand.regulerPensjonsbeholdning2010
    private fun regulerPensjonsbeholdning2010(
        forrigeBeholdning: Pensjonsbeholdning,
        persongrunnlag: Persongrunnlag
    ): Pensjonsbeholdning? {
        val reguleringDato = createDate(YEAR_2010, Calendar.MAY, 1)
        forrigeBeholdning.tom = getRelativeDateByDays(reguleringDato, -1)
        val personPensjonsbeholdning: PersonPensjonsbeholdning =
            generatePersonPensjonsbeholdning(persongrunnlag, forrigeBeholdning)
        val regulertPersonPensjonsbeholdningListe: List<PersonPensjonsbeholdning> =
            regulerPensjonsbeholdning(personPensjonsbeholdning, reguleringDato)

        if (regulertPersonPensjonsbeholdningListe.isEmpty()) {
            return null
        }

        val regulertBeholdning = regulertPersonPensjonsbeholdningListe[0].pensjonsbeholdning
        updateBeholdningAfterPreg(
            regulertBeholdning,
            RestpensjonBeholdningOppdateringAarsak.REGULERING,
            reguleringDato,
            createDate(YEAR_2010, Calendar.DECEMBER, 31)
        )
        return regulertBeholdning
    }

    // BeholdningSwitcherHelper.switchBeholdningOnVirk
    private fun switchBeholdningOnVirk(persongrunnlag: Persongrunnlag, kravhode: Kravhode) {
        val dayBefore = kravhode.onsketVirkningsdato.toLocalDate()?.minusDays(1)
        val beholdning: Pensjonsbeholdning? = getPensjonsbeholdningOnGivenDate(persongrunnlag.beholdninger, dayBefore!!)

        if (isFirstDayOfYear(kravhode.onsketVirkningsdato)) {
            updateBeholdningOnVirkWithTilvekstBeholdning(persongrunnlag, kravhode, beholdning)
        } else if (isFirstDayOfMay(kravhode.onsketVirkningsdato)) {
            updateBeholdningOnVirkWithRegulertBeholdning(persongrunnlag, kravhode, beholdning)
        }
    }

    // BeholdningSwitcherHelper.updateBeholdningOnVirkWithRegulertBeholdning
    private fun updateBeholdningOnVirkWithRegulertBeholdning(
        persongrunnlag: Persongrunnlag,
        kravhode: Kravhode,
        beholdning: Pensjonsbeholdning?
    ) {
        val personPensjonsbeholdning = PersonPensjonsbeholdning().apply {
            fodselsnummer = persongrunnlag.penPerson?.pid?.value
            pensjonsbeholdning = beholdning
        }

        val regulerPensjonsbeholdningConsumerRequest = RegulerPensjonsbeholdningRequest().apply {
            beregningsgrunnlagForPensjonsbeholdning = ArrayList(listOf(personPensjonsbeholdning))
            virkFom = kravhode.onsketVirkningsdato
        }

        val regulertBeholdning: Pensjonsbeholdning? = try {
            // DefaultBeregningConsumerService.regulerPensjonsbeholdning -> RegulerPensjonsbeholdningConsumerCommand.execute
            val regulertBeholdningListe: ArrayList<PersonPensjonsbeholdning> =
                context.regulerPensjonsbeholdning(regulerPensjonsbeholdningConsumerRequest).regulertBeregningsgrunnlagForPensjonsbeholdning
            if (regulertBeholdningListe.isEmpty()) null else regulertBeholdningListe[0].pensjonsbeholdning
        } catch (e: KanIkkeBeregnesException) {
            //throw ImplementationUnrecoverableException(CALL_TO_PREG_FAILED, e)
            throw RuntimeException(CALL_TO_PREG_FAILED, e)
        } catch (e: BeregningsmotorValidereException) {
            //throw ImplementationUnrecoverableException(CALL_TO_PREG_FAILED, e)
            throw RuntimeException(CALL_TO_PREG_FAILED, e)
        }

        regulertBeholdning?.fom = kravhode.onsketVirkningsdato
        /* This does not exist in pensjon-regler domain and is assumed to be irrelevant in simulering:
        regulertBeholdning.oppdateringArsak = RestpensjonBeholdningOppdateringAarsak.REGULERING
        */
        switchExistingBeholdningOnPersongrunnlag(persongrunnlag, regulertBeholdning)
    }

    // BeholdningSwitcherCommand.updateBeholdningOnVirk
    private fun updateBeholdningOnVirk(kravhode: Kravhode, persongrunnlag: Persongrunnlag) {
        updateBeholdning(kravhode, persongrunnlag)
        openLatestBeholdning(persongrunnlag)
    }

    // BeholdningSwitcherCommand.updateBeholdning
    private fun updateBeholdning(nyttKravhode: Kravhode, persongrunnlag: Persongrunnlag) {
        if (beholdningShouldBeSwitched(nyttKravhode, persongrunnlag)) {
            val dayBefore = nyttKravhode.onsketVirkningsdato.toLocalDate()?.minusDays(1)
            removeAllBeholdningAfter(persongrunnlag.beholdninger, dayBefore)
            switchBeholdningOnVirk(persongrunnlag, nyttKravhode)
        } else {
            removeAllBeholdningAfter(persongrunnlag.beholdninger, nyttKravhode.onsketVirkningsdato.toLocalDate())
        }
    }

    // BeholdningSwitchDecider.beholdningShouldBeSwitched
    private fun beholdningShouldBeSwitched(kravhode: Kravhode, persongrunnlag: Persongrunnlag): Boolean =
        isVirkFomEligibleForSwitching(kravhode)
                && isSokersPersongrunnlag(kravhode, persongrunnlag)
                && kravIsAp2016OrAp2025(kravhode)
                && isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling(kravhode)
                && canProduceValidDataForSwitching(kravhode, persongrunnlag)

    // BeholdningSwitchDecider.canProduceValidDataForSwitching
    private fun canProduceValidDataForSwitching(kravhode: Kravhode, persongrunnlag: Persongrunnlag): Boolean {
        val oensketVirkningDato = kravhode.onsketVirkningsdato
        val virkningAar = oensketVirkningDato?.let(::getYear)

        if (isFirstDayOfYear(oensketVirkningDato)) {
            return virkningAar!! - persongrunnlag.sisteGyldigeOpptjeningsAr <= OPPTJENING_ETTERSLEP_ANTALL_AAR
        }

        if (isFirstDayOfMay(oensketVirkningDato)) {
            val response: SatsResponse = context.fetchGyldigSats(satsRequest(oensketVirkningDato))
            val sisteGyldigeGrunnbeloepAar: Int? = response.satsResultater.iterator().next().fom?.let(::getYear)
            return virkningAar!! - sisteGyldigeGrunnbeloepAar!! <= MAX_DIFFERENCE_IN_YEARS_SISTEGYLDIGEGRUNNBELOP
        }

        return false
    }

    private companion object {

        private const val INITIERING = "initiering" // OppdaterPensjonsbeholdningerHelper
        private const val TILVEKST =
            "tilvekst" // OppdaterPensjonsbeholdningerHelper, OppdaterPensjonsbeholdningerRequestFactory
        private const val KORRIGERING = "korrigering" // OppdaterPensjonsbeholdningerHelper
        private const val CALL_TO_PREG_FAILED = "Call to PREG failed"
        private const val MAX_DIFFERENCE_IN_YEARS_SISTEGYLDIGEGRUNNBELOP =
            0 // BeholdningSwitchDecider, specified in PK-25114

        private fun satsRequest(virkningDato: Date?) =
            HentGyldigSatsRequest(
                fomDato = virkningDato,
                tomDato = virkningDato,
                satsType = SatsTypeCti(SatsType.GRUNNBELOP.name)
            )
    }
}
