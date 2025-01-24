package no.nav.pensjon.simulator.core.beholdning

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.INITIERING_OPPTJENINGMODUS
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.KORRIGERING_OPPTJENINGMODUS
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.OPPTJENING_MINIMUM_AAR
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.TILVEKST_OPPTJENINGMODUS
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.addDagpengeGrunnlagIfExists
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.addForstegangstjenesteIfExists
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.createWithTilvekst
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.findBeregnTomAar
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.findReguleringDato
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.firstDateOf
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.firstDayOf
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.hasOpptjeningBefore2009
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.isFirstDayOfMay
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.isFirstDayOfYear
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.isSokersPersongrunnlag
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.isVirkFomEligibleForSwitching
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.kravIsAp2016OrAp2025
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.lastDayOf
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.newGyldigSatsRequest
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.newPersonPensjonBeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.newPersonbeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.newPersongrunnlag
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.newRegulerPensjonBeholdningRequest
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.openLatestBeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.pensjonsbeholdningForDato
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.postprocessBeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.removeAllBeholdningAfter
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.setOmsorgsgrunnlagOnPersongrunnlagIfExists
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.setOpptjeningsgrunnlagOnPersongrunnlagIfExists
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.sisteBeholdning
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.switchExistingBeholdningOnPersongrunnlag
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.tidligsteOpptjeningAar
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.updateBeholdninger
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.verifyBeholdningFom2010
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdaterUtil.verifyEmptyBeholdningAndGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SatsTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonPensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import no.nav.pensjon.simulator.core.domain.regler.to.HentGyldigSatsRequest
import no.nav.pensjon.simulator.core.domain.regler.to.RegulerPensjonsbeholdningRequest
import no.nav.pensjon.simulator.core.domain.regler.to.SatsResponse
import no.nav.pensjon.simulator.core.exception.ImplementationUnrecoverableException
import no.nav.pensjon.simulator.core.exception.KanIkkeBeregnesException
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.createDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findLatest
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.ufoere.UfoeretrygdUtbetalingService
import no.nav.pensjon.simulator.vedtak.VedtakService
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils.hasLength
import java.time.LocalDate
import java.util.*

// Contains refactored code from BeholdningSwitchDecider, BeholdningSwitcherCommand, BeholdningSwitcherHelper,
// OppdaterPensjonsbeholdningerCommand, OppdaterPensjonsbeholdningerHelper, SimpleGrunnlagService.
// Ref. TPEN547 - oppdaterPensjonsbeholdninger
// https://pensjon-dokumentasjon.intern.dev.nav.no/pen/Tjenester/TPEN547_oppdaterPensjonsbeholdninger.html
@Component
class BeholdningUpdater(
    private val context: SimulatorContext,
    private val vedtakService: VedtakService,
    private val personService: PersonService,
    private val ufoereService: UfoeretrygdUtbetalingService
) {
    // SimpleGrunnlagService.updateBeholdningFromEksisterendePersongrunnlag -> BeholdningSwitcherCommand.updateBeholdningFromEksisterendePersongrunnlag
    fun updateBeholdningFromEksisterendePersongrunnlag(nyttKravhode: Kravhode) {
        val persongrunnlag: Persongrunnlag = nyttKravhode.hentPersongrunnlagForSoker()
        val persongrunnlagCopy = Persongrunnlag(source = persongrunnlag)
        updateBeholdninger(persongrunnlag, persongrunnlagCopy.beholdninger)
        updateBeholdningOnVirk(nyttKravhode, persongrunnlagCopy)
    }

    // OppdaterPensjonsbeholdningerCommand.updatePensjonsbeholdning
    private fun updatePensjonBeholdning(
        beregningGrunnlag: BeholdningBeregningsgrunnlag,
        persongrunnlag: Persongrunnlag?,
        sisteGyldigeOpptjeningAar: Int
    ): PersonBeholdning {
        val forrigeBeholdning = beregningGrunnlag.beholdning
        val beholdningTom = LocalDate.of(sisteGyldigeOpptjeningAar + OPPTJENING_ETTERSLEP_ANTALL_AAR, 1, 1)
        val beholdninger: MutableList<Pensjonsbeholdning> =
            beregnOpptjening(persongrunnlag, beholdningTom, forrigeBeholdning)
        val oppdatertBeholdning: Pensjonsbeholdning = sisteBeholdning(beholdninger)
        val oppdatertBeholdningFom = firstDayOf(oppdatertBeholdning)

        postprocessBeholdning(
            pensjonBeholdning = oppdatertBeholdning,
            code = RestpensjonBeholdningOppdateringAarsak.NY_OPPTJENING,
            fom = oppdatertBeholdningFom,
            tom = null
        )

        return newPersonbeholdning(beregningGrunnlag.pid, mutableListOf(oppdatertBeholdning))
    }

    // OppdaterPensjonsbeholdningerHelper.beregnOpptjening
    private fun beregnOpptjening(
        persongrunnlag: Persongrunnlag?,
        beholdningTom: LocalDate?,
        forrigeBeholdning: Pensjonsbeholdning?
    ): MutableList<Pensjonsbeholdning> =
        try {
            persongrunnlag?.let { context.beregnOpptjening(beholdningTom, it, forrigeBeholdning) } ?: mutableListOf()
        } catch (e: KanIkkeBeregnesException) {
            throw ImplementationUnrecoverableException("The rules engine cannot calculate input for personId:" + persongrunnlag?.penPerson?.penPersonId, e)
        } catch (e: RegelmotorValideringException) {
            throw ImplementationUnrecoverableException(("The rules engine cannot calculate input for personId:" + persongrunnlag?.penPerson?.penPersonId), e)
        }

    // OppdaterPensjonsbeholdningerHelper.hentGyldigSats
    private fun hentGyldigSats(aar: Int): List<SatsResultat> =
        context.fetchGyldigSats(newGyldigSatsRequest(aar)).satsResultater

    // OppdaterPensjonsbeholdningerCommand.populatePersongrunnlag
    private fun populatePersongrunnlag(
        person: PenPerson,
        beregningGrunnlag: BeholdningBeregningsgrunnlag,
        opptjeningModus: String,
        sisteGyldigeOpptjeningAar: String
    ): Persongrunnlag {
        if (opptjeningModus != INITIERING_OPPTJENINGMODUS && beregningGrunnlag.ufoerFoer2009) {
            throw ImplementationUnrecoverableException(
                "Invalid combination with opptjeningModus: $opptjeningModus with UforFoer2009 is TRUE."
            )
        }

        val persongrunnlag: Persongrunnlag = newPersongrunnlag(person)
        setOpptjeningsgrunnlagOnPersongrunnlagIfExists(beregningGrunnlag.opptjeningGrunnlagListe, persongrunnlag)
        setOmsorgsgrunnlagOnPersongrunnlagIfExists(beregningGrunnlag.omsorgGrunnlagListe, persongrunnlag)
        addForstegangstjenesteIfExists(beregningGrunnlag, persongrunnlag)
        addDagpengeGrunnlagIfExists(beregningGrunnlag, persongrunnlag)

        if (hasLength(sisteGyldigeOpptjeningAar)) {
            persongrunnlag.sisteGyldigeOpptjeningsAr = sisteGyldigeOpptjeningAar.toInt()
        }

        // OppdaterPensjonsbeholdningerHelper.getUforeOpptjeningGrunnlagCopy
        persongrunnlag.utbetalingsgradUTListe = ufoereService.getUtbetalingGradListe(person.penPersonId).toMutableList()

        return persongrunnlag
    }

    // OppdaterPensjonsbeholdningerCommand.createPensjonsbeholdning2010
    private fun createPensjonsbeholdning2010(
        beregningGrunnlag: BeholdningBeregningsgrunnlag,
        persongrunnlag: Persongrunnlag
    ): PersonBeholdning {
        val beholdningTom = LocalDate.of(OPPTJENING_MINIMUM_AAR, 1, 1)
        val beholdninger: MutableList<Pensjonsbeholdning> = beregnOpptjening(persongrunnlag, beholdningTom, null)

        for (beholdning in beholdninger) {
            val fom = firstDayOf(beholdning)
            val tom = if (beholdning.ar == OPPTJENING_MINIMUM_AAR) null else lastDayOf(beholdning)
            postprocessBeholdning(beholdning, RestpensjonBeholdningOppdateringAarsak.NY_OPPTJENING, fom, tom)
        }

        return newPersonbeholdning(beregningGrunnlag.pid, beholdninger)
    }

    // BeholdningSwitcherHelper.updateBeholdningOnVirkWithTilvekstBeholdning
    private fun updateBeholdningOnVirkWithTilvekstBeholdning(
        persongrunnlag: Persongrunnlag,
        kravhode: Kravhode,
        pensjonBeholdning: Pensjonsbeholdning?
    ) {
        val grunnlag = BeholdningBeregningsgrunnlag().apply {
            pid = persongrunnlag.penPerson?.pid
            dagpengerGrunnlagListe = persongrunnlag.dagpengegrunnlagListe
            foerstegangstjenesteGrunnlag = persongrunnlag.forstegangstjenestegrunnlag
            opptjeningGrunnlagListe = persongrunnlag.opptjeningsgrunnlagListe
            omsorgGrunnlagListe = persongrunnlag.omsorgsgrunnlagListe
            beholdning = pensjonBeholdning
        }

        val sisteGyldigeOpptjeningAar =
            kravhode.onsketVirkningsdato?.toNorwegianLocalDate()?.minusYears(
                OPPTJENING_ETTERSLEP_ANTALL_AAR.toLong()
            )?.year.toString()

        val request = createWithTilvekst(grunnlag, sisteGyldigeOpptjeningAar)
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
    private fun oppdaterPensjonsbeholdninger(spec: BeholdningUpdateSpec): BeholdningUpdateResult {
        //helper.validateInput(request) TODO
        val personBeholdningListe: MutableList<PersonBeholdning> = mutableListOf()
        val ikkeFunnetPidListe: MutableList<Pid> = mutableListOf()
        oppdaterBeholdning(spec, personBeholdningListe, ikkeFunnetPidListe)
        return BeholdningUpdateResult(personBeholdningListe, ikkeFunnetPidListe)
    }

    // OppdaterPensjonsbeholdningerCommand.oppdaterBeholdning
    private fun oppdaterBeholdning(
        spec: BeholdningUpdateSpec,
        personBeholdningListe: MutableList<PersonBeholdning>,
        ikkeFunnetPidListe: MutableList<Pid>
    ) {
        val pidListe: List<Pid> = spec.pensjonBeholdningBeregningGrunnlag.mapNotNull { it.pid }
        val personerVedPid: Map<Pid, PenPerson> = personService.personListe(pidListe)

        for (beregningsgrunnlag in spec.pensjonBeholdningBeregningGrunnlag) {
            var personBeholdning: PersonBeholdning? = null
            val penPerson: PenPerson? = beregningsgrunnlag.pid?.let { personerVedPid[it] }

            if (penPerson == null) {
                beregningsgrunnlag.pid?.let(ikkeFunnetPidListe::add)
            } else {
                // Call 5.1.1 - Popular persongrunnlag
                val persongrunnlag: Persongrunnlag = populatePersongrunnlag(
                    person = penPerson,
                    beregningGrunnlag = beregningsgrunnlag,
                    opptjeningModus = spec.opptjeningModus,
                    sisteGyldigeOpptjeningAar = spec.sisteGyldigeOpptjeningAar
                )

                val sisteGyldigeOpptjeningAar = spec.sisteGyldigeOpptjeningAar.toInt()

                // Scenario 0: If beholdningsyear is before 2010, thrown exception
                val beholdning = beregningsgrunnlag.beholdning

                if (beholdning?.ar?.let { it < OPPTJENING_MINIMUM_AAR } == true) {
                    throw ImplementationUnrecoverableException("berGrForPensjonsbeholdning.beholdning.ar er FØR 2010")
                } else if (verifyEmptyBeholdningAndGrunnlag(beholdning, persongrunnlag)) {
                    personBeholdning = newPersonbeholdning(beregningsgrunnlag.pid, sisteGyldigeOpptjeningAar)

                    // Scenario 1: Initering - initial pensjonsbeholdning from 01.01.2010
                } else if (spec.opptjeningModus == INITIERING_OPPTJENINGMODUS && beholdning == null) {
                    // Call 5.2
                    personBeholdning = createPensjonsbeholdning2010(beregningsgrunnlag, persongrunnlag)

                    // Scenario 2: Tilvekst - update pensjonsbeholdning by new opptjening
                } else if (spec.opptjeningModus == TILVEKST_OPPTJENINGMODUS) {
                    // Call 5.3
                    personBeholdning =
                        updatePensjonBeholdning(beregningsgrunnlag, persongrunnlag, sisteGyldigeOpptjeningAar)

                    // Scenario 3: Korrigering - uupdate pensjonsbeholdning by corrected opptjening
                } else if (spec.opptjeningModus == KORRIGERING_OPPTJENINGMODUS) {
                    // Call 5.4 - if beholdning informasjon is sent as input to the service applies before last year
                    personBeholdning = adjustedPensjonsbeholdning(
                        beregningsgrunnlag,
                        persongrunnlag,
                        sisteGyldigeOpptjeningAar,
                        spec.beregnBeholdningUtenUttak
                    )
                }

                // Sjekk at beholdning er oppdatert for bruker
                if (personBeholdning == null) {
                    throw ImplementationUnrecoverableException("The user exists but the pensjonsbeholdning has not been updated.")
                }

                personBeholdningListe.add(personBeholdning)
            }
        }
    }

    // OppdaterPensjonsbeholdningerCommand.adjustedPensjonsbeholdning
    private fun adjustedPensjonsbeholdning(
        beregningGrunnlag: BeholdningBeregningsgrunnlag,
        persongrunnlag: Persongrunnlag,
        sisteGyldigeOpptjeningAar: Int,
        beregnBeholdningUtenUttak: Boolean
    ): PersonBeholdning {
        var forrigeBeholdning: Pensjonsbeholdning? = beregningGrunnlag.beholdning
        val beholdninger: MutableList<Pensjonsbeholdning> = mutableListOf()

        if (beregningGrunnlag.beholdning == null && hasOpptjeningBefore2009(beregningGrunnlag)) {
            beholdninger.addAll(beregnBeholdningerTil2010(persongrunnlag))
            forrigeBeholdning = sisteBeholdning(beholdninger)
        } else {
            beregningGrunnlag.beholdning?.let { forrigeBeholdning = it }
        }

        // Call 5.4.2 calculate beregning after 2010
        beholdninger.addAll(
            beregnBeholdningerEtter2010(
                beregningGrunnlag,
                persongrunnlag,
                forrigeBeholdning,
                sisteGyldigeOpptjeningAar,
                beregnBeholdningUtenUttak
            )
        )

        return newPersonbeholdning(beregningGrunnlag.pid, beholdninger)
    }

    // OppdaterPensjonsbeholdningerCommand.calculateTheInventoryTo2010
    private fun beregnBeholdningerTil2010(persongrunnlag: Persongrunnlag): List<Pensjonsbeholdning> {
        val beholdningTom = LocalDate.of(OPPTJENING_MINIMUM_AAR, 1, 1)
        val beholdningerFor2010: List<Pensjonsbeholdning> = beregnOpptjening(persongrunnlag, beholdningTom, null)

        for (beholdning in beholdningerFor2010) {
            postprocessBeholdning(
                pensjonBeholdning = beholdning,
                code = RestpensjonBeholdningOppdateringAarsak.NY_OPPTJENING,
                fom = firstDayOf(beholdning),
                tom = lastDayOf(beholdning)
            )
        }

        return beholdningerFor2010
    }

    // OppdaterPensjonsbeholdningerCommand.calculateTheInventoryAfter2010
    private fun beregnBeholdningerEtter2010(
        beregningGrunnlag: BeholdningBeregningsgrunnlag,
        persongrunnlag: Persongrunnlag,
        beholdning: Pensjonsbeholdning?,
        sisteGyldigeOpptjeningAar: Int,
        beregnBeholdningUtenUttak: Boolean
    ): List<Pensjonsbeholdning> {
        val beholdningerEtter2010: MutableList<Pensjonsbeholdning> = mutableListOf()

        val foersteVirkningFom: LocalDate? =
            if (beregnBeholdningUtenUttak)
                null
            else
                beregningGrunnlag.pid?.let {
                    vedtakService.tidligsteKapittel20VedtakGjelderFom(it, SakTypeEnum.ALDER)
                }

        val beregnTomAar: Int = findBeregnTomAar(foersteVirkningFom, sisteGyldigeOpptjeningAar)
        var forrigeBeholdning: Pensjonsbeholdning? = beholdning

        val startAar: Int =
            if (forrigeBeholdning != null) {
                if (verifyBeholdningFom2010(forrigeBeholdning.fom)) {
                    // Call 5.4.3 reguler beholdning for 2010
                    val regulertBeholdning2010: Pensjonsbeholdning? =
                        regulerPensjonsbeholdning2010(forrigeBeholdning, persongrunnlag)
                    regulertBeholdning2010?.let(beholdningerEtter2010::add)
                }

                forrigeBeholdning.ar + 1
            } else {
                tidligsteOpptjeningAar(beregningGrunnlag, persongrunnlag) + 2
            }

        // Calculate from year 2011 to currentYear and regular beholdning
        for (aar in startAar..beregnTomAar) {
            val beholdningTom = LocalDate.of(aar, 1, 1)

            val beholdninger: List<Pensjonsbeholdning> =
                beregnOpptjening(persongrunnlag, beholdningTom, forrigeBeholdning)

            val pensjonsbeholdning = sisteBeholdning(beholdninger)
            val reguleringDatoer: List<SatsResultat> = hentGyldigSats(aar)
            val forsteReguleringDato: Date? = findReguleringDato(reguleringDatoer)
            val pensjonsbeholdningFom = firstDayOf(pensjonsbeholdning)

            postprocessBeholdning(
                pensjonBeholdning = pensjonsbeholdning,
                code = RestpensjonBeholdningOppdateringAarsak.NY_OPPTJENING,
                fom = pensjonsbeholdningFom,
                tom = null
            )

            val ingenReguleringDatoer = reguleringDatoer.isEmpty()

            // Senario 1: PREG return empty sats, and it is not the last beholdning which shall be calculated.
            if (ingenReguleringDatoer && pensjonsbeholdning.ar != beregnTomAar) {
                pensjonsbeholdning.tom = lastDayOf(pensjonsbeholdning)
                beholdningerEtter2010.add(pensjonsbeholdning)
            } else if (ingenReguleringDatoer && pensjonsbeholdning.ar == beregnTomAar) {
                // Senario 2: PREG return empty sats, and it is the last beholdning which shall be calculated. If bruker has AP2016/2025,
                // firstVirkFom will not be null and the last pensjonsbeholdning must get a tomDate the day before firstVirkFom.
                pensjonsbeholdning.tom =
                    foersteVirkningFom?.let { getRelativeDateByDays(date = it, days = -1) }?.toNorwegianDateAtNoon()
                beholdningerEtter2010.add(pensjonsbeholdning)
            } else if (!ingenReguleringDatoer) {
                // Senario 3: PREG return sats
                if (foersteVirkningFom?.let { isBeforeByDay(it, forsteReguleringDato, allowSameDay = true) } == true) {
                    pensjonsbeholdning.tom = getRelativeDateByDays(date = foersteVirkningFom, days = -1).toNorwegianDateAtNoon()
                    beholdningerEtter2010.add(pensjonsbeholdning)
                } else {
                    pensjonsbeholdning.tom = getRelativeDateByDays(date = forsteReguleringDato!!, days = -1)
                    beholdningerEtter2010.add(pensjonsbeholdning)

                    val personPensjonsbeholdning: PersonPensjonsbeholdning =
                        newPersonPensjonBeholdning(persongrunnlag, pensjonsbeholdning)

                    val regulerteBeholdninger: List<Pensjonsbeholdning> =
                        calculateBeholdningAfterRegulering(
                            reguleringDatoListe = reguleringDatoer,
                            personPensjonsbeholdning,
                            beregnTomAar = beregnTomAar,
                            aar = aar,
                            foersteVirkningFom = foersteVirkningFom?.toNorwegianDateAtNoon()
                        )

                    forrigeBeholdning = findLatest(regulerteBeholdninger)
                    beregningGrunnlag.beholdning = forrigeBeholdning
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
        beregnTomAar: Int,
        aar: Int,
        foersteVirkningFom: Date?
    ): List<Pensjonsbeholdning> {
        val regulertBeholdningListe: MutableList<Pensjonsbeholdning> = mutableListOf()

        for (index in reguleringDatoListe.indices) {
            val satsResultat = reguleringDatoListe[index]
            val reguleringDato = satsResultat.fom

            if (foersteVirkningFom == null || isBeforeByDay(reguleringDato, foersteVirkningFom, false)) {
                val regulertPersonPensjonsbeholdningListe: List<PersonPensjonsbeholdning> =
                    regulerPensjonsbeholdning(personPensjonsbeholdning, reguleringDato)

                if (regulertPersonPensjonsbeholdningListe.isNotEmpty()) {
                    val regulertBeholdning = regulertPersonPensjonsbeholdningListe[0].pensjonsbeholdning
                    var beholdningTomDato: Date? = null

                    if (reguleringDatoListe.size > index + 1) {
                        val nextReguleringDato: Date? =
                            reguleringDatoListe[index + 1].fom // OppdaterPensjonsbeholdningerHelper.getNextReguleringsdato
                        beholdningTomDato =
                            getRelativeDateByDays(firstDateOf(nextReguleringDato, foersteVirkningFom)!!, -1)
                    } else if (beregnTomAar != aar) {
                        beholdningTomDato = createDate(aar, Calendar.DECEMBER, 31)
                    } else if (foersteVirkningFom != null) {
                        beholdningTomDato = getRelativeDateByDays(foersteVirkningFom, -1)
                    }

                    postprocessBeholdning(
                        regulertBeholdning,
                        RestpensjonBeholdningOppdateringAarsak.REGULERING,
                        reguleringDato,
                        beholdningTomDato
                    )

                    regulertBeholdning?.let(regulertBeholdningListe::add)
                }
            }
        }

        return regulertBeholdningListe
    }

    // OppdaterPensjonsbeholdningerHelper.regulerPensjonsbeholdning
    private fun regulerPensjonsbeholdning(
        personPensjonsbeholdning: PersonPensjonsbeholdning?,
        virkningFom: Date?
    ): List<PersonPensjonsbeholdning> {
        val personPensjonsbeholdninger: MutableList<PersonPensjonsbeholdning> = mutableListOf()
        personPensjonsbeholdning?.let(personPensjonsbeholdninger::add)
        val request: RegulerPensjonsbeholdningRequest =
            newRegulerPensjonBeholdningRequest(personPensjonsbeholdninger, virkningFom)

        try {
            // DefaultBeregningConsumerService.regulerPensjonsbeholdning -> RegulerPensjonsbeholdningConsumerCommand.execute
            return context.regulerPensjonsbeholdning(request).regulertBeregningsgrunnlagForPensjonsbeholdning.toList()
        } catch (e: KanIkkeBeregnesException) {
            throw ImplementationUnrecoverableException("The rules engine cannot calculate input for beholdning: " + personPensjonsbeholdning?.pensjonsbeholdning, e)
        } catch (e: RegelmotorValideringException) {
            throw ImplementationUnrecoverableException("The rules engine cannot calculate input for beholdning: " + personPensjonsbeholdning?.pensjonsbeholdning, e)
        }
    }

    // OppdaterPensjonsbeholdningerCommand.regulerPensjonsbeholdning2010
    private fun regulerPensjonsbeholdning2010(
        forrigeBeholdning: Pensjonsbeholdning,
        persongrunnlag: Persongrunnlag
    ): Pensjonsbeholdning? {
        val reguleringDato = createDate(OPPTJENING_MINIMUM_AAR, Calendar.MAY, 1)
        forrigeBeholdning.tom = getRelativeDateByDays(reguleringDato, -1)
        val personPensjonsbeholdning: PersonPensjonsbeholdning =
            newPersonPensjonBeholdning(persongrunnlag, forrigeBeholdning)
        val regulertPersonPensjonsbeholdningListe: List<PersonPensjonsbeholdning> =
            regulerPensjonsbeholdning(personPensjonsbeholdning, reguleringDato)

        if (regulertPersonPensjonsbeholdningListe.isEmpty()) {
            return null
        }

        val regulertBeholdning = regulertPersonPensjonsbeholdningListe[0].pensjonsbeholdning
        postprocessBeholdning(
            regulertBeholdning,
            RestpensjonBeholdningOppdateringAarsak.REGULERING,
            reguleringDato,
            createDate(OPPTJENING_MINIMUM_AAR, Calendar.DECEMBER, 31)
        )
        return regulertBeholdning
    }

    // BeholdningSwitcherHelper.switchBeholdningOnVirk
    private fun switchBeholdningOnVirk(persongrunnlag: Persongrunnlag, kravhode: Kravhode) {
        val virkningDato = kravhode.onsketVirkningsdato
        val dayBefore: LocalDate? = virkningDato?.toNorwegianLocalDate()?.minusDays(1)

        val beholdning: Pensjonsbeholdning? =
            dayBefore?.let { pensjonsbeholdningForDato(persongrunnlag.beholdninger, it) }

        if (virkningDato?.let(::isFirstDayOfYear) == true) {
            updateBeholdningOnVirkWithTilvekstBeholdning(persongrunnlag, kravhode, beholdning)
        } else if (virkningDato?.let(::isFirstDayOfMay) == true) {
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
            throw ImplementationUnrecoverableException(CALL_TO_PREG_FAILED, e)
        } catch (e: RegelmotorValideringException) {
            throw ImplementationUnrecoverableException(CALL_TO_PREG_FAILED, e)
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
            val dayBefore = nyttKravhode.onsketVirkningsdato?.toNorwegianLocalDate()?.minusDays(1)
            removeAllBeholdningAfter(persongrunnlag.beholdninger, dayBefore)
            switchBeholdningOnVirk(persongrunnlag, nyttKravhode)
        } else {
            removeAllBeholdningAfter(
                persongrunnlag.beholdninger,
                nyttKravhode.onsketVirkningsdato?.toNorwegianLocalDate()
            )
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
        val virkningDato = kravhode.onsketVirkningsdato
        val virkningAar = virkningDato?.let(::getYear)

        if (virkningDato?.let(::isFirstDayOfYear) == true) {
            return virkningAar!! - persongrunnlag.sisteGyldigeOpptjeningsAr <= OPPTJENING_ETTERSLEP_ANTALL_AAR
        }

        if (virkningDato?.let(::isFirstDayOfMay) == true) {
            val response: SatsResponse = context.fetchGyldigSats(satsRequest(virkningDato))
            val sisteGyldigeGrunnbeloepAar: Int? = response.satsResultater.iterator().next().fom?.let(::getYear)
            return virkningAar!! - sisteGyldigeGrunnbeloepAar!! <= MAX_DIFFERENCE_IN_YEARS_SISTEGYLDIGEGRUNNBELOP
        }

        return false
    }

    private companion object {

        private const val CALL_TO_PREG_FAILED = "Call to PREG failed"
        private const val MAX_DIFFERENCE_IN_YEARS_SISTEGYLDIGEGRUNNBELOP =
            0 // BeholdningSwitchDecider, specified in PK-25114

        private fun satsRequest(virkningDato: Date?) =
            HentGyldigSatsRequest().apply {
                fomDato = virkningDato
                tomDato = virkningDato
                satsTypeEnum = SatsTypeEnum.GRUNNBELOP
            }
    }
}
