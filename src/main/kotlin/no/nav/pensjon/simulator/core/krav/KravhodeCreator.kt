package no.nav.pensjon.simulator.core.krav

import no.nav.pensjon.simulator.core.*
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdater
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.beregn.InntektType
import no.nav.pensjon.simulator.core.domain.*
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.kode.*
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagsrolleCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.OpptjeningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.RegelverkTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.exception.BrukerFoedtFoer1943Exception
import no.nav.pensjon.simulator.core.krav.KravUtil.kravlinjeType
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.createDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getFirstDateInYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterToday
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isFirstDayOfMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.monthOfYearRange1To12
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.person.PersongrunnlagMapper
import no.nav.pensjon.simulator.core.result.OpptjeningType
import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.krav.KravUtil.utlandMaanederInnenforAaret
import no.nav.pensjon.simulator.core.krav.KravUtil.utlandMaanederInnenforRestenAvAaret
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findValidForYear
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.time.LocalDate
import java.util.*
import java.util.stream.IntStream
import kotlin.streams.toList

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OpprettKravhodeHelper
@Component
class KravhodeCreator(
    private val context: SimulatorContext,
    private val beholdningUpdater: BeholdningUpdater,
    private val persongrunnlagMapper: PersongrunnlagMapper,
    private val generelleDataHolder: GenerelleDataHolder
) {

    private val logger = LoggerFactory.getLogger(KravhodeCreator::class.java)

    // OpprettKravhodeHelper.opprettKravhode
    // Personer will be undefined in forenklet simulering (anonymous)
    fun opprettKravhode(
        spec: KravhodeSpec,
        person: PenPerson?,
        virkningsdatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
    ): Kravhode {
        val simulatorInput = spec.simulatorInput
        val forrigeAlderBeregningsresultat = spec.forrigeAlderspensjonBeregningResult
        val grunnbelop = spec.grunnbeloep
        val gjelderEndring = simulatorInput.gjelderEndring()
        val gjelderAfpOffentligPre2025 = simulatorInput.gjelderPre2025OffentligAfp()

        val kravhode = Kravhode().apply {
            kravFremsattDato = Date()
            onsketVirkningsdato = fromLocalDate(oensketVirkningDato(simulatorInput))
            gjelder = null
            sakId = null
            sakType = SakType.ALDER
            regelverkTypeCti = finnRegelverkType(simulatorInput)
        }

        addSokerGrunnlagToKrav(simulatorInput, kravhode, person, forrigeAlderBeregningsresultat, grunnbelop)
        addEpsGrunnlagToKrav(simulatorInput, kravhode, forrigeAlderBeregningsresultat, grunnbelop)

        if (kravTilsierBoddEllerArbeidetUtenlands(forrigeAlderBeregningsresultat)) {
            kravhode.boddEllerArbeidetIUtlandet = true
        } else {
            kravhode.boddEllerArbeidetIUtlandet = harUtenlandsopphold(simulatorInput.utlandAntallAar, emptyList())
        }

        // NB: Next line requires avdød persongrunnlag to be fetched above
        val avdodGrunnlag = kravhode.hentPersongrunnlagForRolle(grunnlagsrolle = GrunnlagRolle.AVDOD, checkBruk = false)

        kravhode.boddArbeidUtlandAvdod =
            avdodGrunnlag?.let { harUtenlandsopphold(simulatorInput.avdoed?.antallAarUtenlands, it.trygdetidPerioder) }
                ?: false
        addForsteVirkningsdatoGrunnlagToKrav(kravhode, virkningsdatoGrunnlagListe)
        kravhode.uttaksgradListe = alderspensjonUttaksgrader(simulatorInput)

        kravhode.uttaksgradListe =
            when {
                gjelderAfpOffentligPre2025 -> afpOffentligPre2025Uttaksgrader(
                    simulatorInput,
                    forrigeAlderBeregningsresultat
                )

                gjelderEndring -> endringUttaksgrader(simulatorInput, forrigeAlderBeregningsresultat)
                else -> alderspensjonUttaksgrader(simulatorInput)
            }

        addKravlinjerToKrav(kravhode)
        settGenerelleFelter(kravhode)
        updateOnsketVirkAndUtbetalingsgrad(simulatorInput, kravhode)
        return kravhode
    }

    private fun updateOnsketVirkAndUtbetalingsgrad(spec: SimuleringSpec, kravhode: Kravhode) {
        if (spec.erAnonym) return

        val persongrunnlag = kravhode.hentPersongrunnlagForSoker()
        val opptjeningGrunnlag = null //ANON context.hentUforeOpptjeningGrunnlag(persongrunnlag.penPerson!!.penPersonId)
        persongrunnlag.utbetalingsgradUTListe =
            mutableListOf() //ANON opptjeningGrunnlag?.let(Ap2025KjerneToSimuleringUforeMapper::mapUforeOpptjeningGrunnlag) ?: mutableListOf()
    }

    private fun settGenerelleFelter(kravhode: Kravhode) {
        //kravhode.afpTilLegg = false
        //kravhode.kravFremsattDato = Date() // done elsewhere
        //----- vurdereTrygdeavtale is not used in pensjon-regler kravhode: -----
        //kravhode.setVurdereTrygdeavtale(...)
        //kravhode.vurdereTrygdeavtaleAvdod = ...
        //-----------------------------------------------------------------------

        // CR225950 10.02.2011 OJB2812 - Citation from design:
        // Finner året bruker fyller MAX_ALDER år. Ved MAX_OPPTJENING_ALDER år er det siste året som det kan simuleres for (les: siste virkningstidspunkt
        // i kall til PREG). Det vil igjen si at året bruker fyller 73 er det siste året en opptjening kan bli godskrevet. Siden
        // det ikke har noen effekt at sisteGyldigeOpptjeningsår settes høyere enn det som er reelt så setter man verdiene
        // høyere for å unngå feil som følge av at man forsøker å gjøre det korrekt. Dette som en pragmatisk løsning på et
        // feilutsatt område. Merk også at dette feltet er satt fra før under opprettelse av persongrunnlag, men da er det satt
        // til verdien som gjelder for normal beregning. Dette fordi det trengs til utplukk av beholdninger fra POPP via
        // FPEN027.
        val foedselDato: Date = kravhode.hentPersongrunnlagForSoker().fodselsdato!!
        val sisteGyldigeOpptjeningAar = yearUserTurnsGivenAge(foedselDato, MAX_ALDER)

        for (persongrunnlag in kravhode.persongrunnlagListe) {
            persongrunnlag.sisteGyldigeOpptjeningsAr = sisteGyldigeOpptjeningAar
        }
    }

    private fun addKravlinjerToKrav(kravhode: Kravhode) {
        kravhode.kravlinjeListe =
            mutableListOf(norskKravlinje(KravlinjeTypePlus.AP, kravhode.hentPersongrunnlagForSoker().penPerson!!))
        val avdodGrunnlag = kravhode.hentPersongrunnlagForRolle(GrunnlagRolle.AVDOD, false)

        if (avdodGrunnlag != null) {
            kravhode.kravlinjeListe.add(norskKravlinje(KravlinjeTypePlus.GJR, avdodGrunnlag.penPerson!!))
        }
    }

    // OpprettKravHodeHelper.leggTilForstevirkningsdatoGrunnlag
    private fun addForsteVirkningsdatoGrunnlagToKrav(
        kravhode: Kravhode,
        virkningsdatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
    ) {
        //TODO inspect this code:
        kravhode.persongrunnlagListe.forEach {
            it.forsteVirkningsdatoGrunnlagListe.toMutableList()
                .forEach { item -> it.forsteVirkningsdatoGrunnlagListe.remove(item) }
        }

        kravhode.persongrunnlagListe.forEach {
            addForsteVirkningsdatoGrunnlagToPersongrunnlag(
                it,
                virkningsdatoGrunnlagListe
            )
        }
    }

    // OpprettKravHodeHelper.leggTilForstevirkningsdatoGrunnlag
    private fun addForsteVirkningsdatoGrunnlagToPersongrunnlag(
        persongrunnlag: Persongrunnlag,
        virkningsdatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
    ) {
        if ((persongrunnlag.penPerson?.penPersonId ?: 0) <= 0) return

        virkningsdatoGrunnlagListe.forEach(persongrunnlag.forsteVirkningsdatoGrunnlagListe::add)
    }

    // OpprettKravHodeHelper.isBoddArbeidUtlandTrueOnKravHode + findKravHode
    private fun kravTilsierBoddEllerArbeidetUtenlands(beregningsresultat: AbstraktBeregningsResultat?): Boolean =
        false //ANON beregningsresultat?.kravId?.let(context::getKravhode)?.boddEllerArbeidetIUtlandet ?: false

    // OpprettKravHodeHelper.opprettPersongrunnlagForEPS
    fun addAlderspensjonEpsGrunnlagToKrav(spec: SimuleringSpec, kravhode: Kravhode, grunnbelop: Int) {
        if (EnumSet.of(SimuleringType.ALDER_M_GJEN, SimuleringType.ENDR_ALDER_M_GJEN).contains(spec.type)) {
            //createPersongrunnlagInCaseOfGjenlevenderett(simulering, kravhode)
            with("Simulering for gjenlevende is not supported") {
                logger.error(this)
                throw RuntimeException(this)
            }
        } else if (EnumSet.of(SivilstatusType.SAMB, SivilstatusType.GIFT, SivilstatusType.REPA)
                .contains(spec.sivilstatus)
        ) {
            kravhode.persongrunnlagListe.add(persongrunnlagBasedOnSivilstatus(spec, grunnbelop))
        }
    }

    private fun addEndringEpsGrunnlagToKrav(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeResultat: AbstraktBeregningsResultat?,
        grunnbelop: Int
    ) {
        //ANON EndringPersongrunnlag(context).opprettEpsGrunnlag(spec, kravhode, forrigeResultat, grunnbelop)
    }

    private fun addPre2025OffentligAfpEpsGrunnlagToKrav(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeResultat: AbstraktBeregningsResultat?,
        grunnbelop: Int
    ) {
        //ANON AfpPersongrunnlag(context).opprettPersongrunnlagForEps(spec, kravhode, forrigeResultat, grunnbelop)
        // TODO Optimize this call chain when forrigeAlderBeregningsresultat = null:
        //    KravhodeCreator.addPre2025OffentligAfpEpsGrunnlagToKrav (this method)
        // -> AfpPersongrunnlag.opprettPersongrunnlagForEps(4)
        // -> AfpPersongrunnlag.opprettPersongrunnlagForEps(3)
        // -> KravhodeCreator.addAlderspensjonEpsGrunnlagToKrav
    }

    // OpprettKravHodeHelper.createPersongrunnlagBasedOnSivilstatus
    private fun persongrunnlagBasedOnSivilstatus(spec: SimuleringSpec, grunnbeloep: Int): Persongrunnlag {
        val grunnlag = persongrunnlagMapper.mapToEpsPersongrunnlag(
            spec.sivilstatus,
            foedselDato(spec)
        )

        if (spec.epsHarInntektOver2G) {
            val today = LocalDate.now()
            val forsteUttakDato =
                if (isBeforeDay(spec.foersteUttakDato, today)) spec.foersteUttakDato else today
            val inntektFom = getFirstDateInYear(forsteUttakDato)
            grunnlag.inntektsgrunnlagListe.add(
                inntektsgrunnlagForSokerOrEps(
                    GRUNNBELOP_MULTIPLIER * grunnbeloep,
                    inntektFom,
                    null
                )
            )
        }

        return grunnlag
    }

    private fun foedselDato(spec: SimuleringSpec): LocalDate =
        //ANON spec.pid?.let(context::fetchLegacyFodselsdato) ?: foersteDag(spec.fodselsar)
        foersteDag(spec.foedselAar)

    /* Simulering type ALDER_M_GJEN not yet supported
    private fun createPersongrunnlagInCaseOfGjenlevenderett(simulering: SimuleringSpec, kravhode: Kravhode) {
        val lastYear = DateUtil.getYear(LocalDate.now()) - 1

        // Del 1
        val persongrunnlag = PersongrunnlagMapper(context).mapToPersongrunnlagAvdod(context.hentPenPerson(simulering.avdodPid), simulering)
        // Will later be retrieved thus: kravhode.hentPersongrunnlagForRolle(GrunnlagRolle.AVDOD, false)

        kravhode.persongrunnlagListe.add(persongrunnlag)
        persongrunnlag.sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR

        // Del 2
        oppdaterGrunnlagMedBeholdninger(persongrunnlag, kravhode, simulering.avdodPid, true)
        filterAndUpdateInntektsgrunnlaglistOnPersongrunnlag(persongrunnlag)

        // Del 3.1
        val inntekt = Inntekt2().apply {
            inntektAr = lastYear
            belop = simulering.avdodInntektForDod.toLong()
        }
        val inntektListeEPS = mutableListOf(inntekt)

        // Del 3.2
        val oppdaterOpptjeningsgrunnlagFraInntektListeResponse: OppdaterOpptjeningsgrunnlagFraInntektListeResponse = caller.oppdaterOpptjeningsgrunnlagFraInntektListe(
            OppdaterOpptjeningsgrunnlagFraInntektListeRequest(inntektListeEPS, persongrunnlag.opptjeningsgrunnlagListe, persongrunnlag.fodselsdato)
        )
        persongrunnlag.opptjeningsgrunnlagListe = oppdaterOpptjeningsgrunnlagFraInntektListeResponse.opptjeningsgrunnlagListe
    }

    private fun filterAndUpdateInntektsgrunnlaglistOnPersongrunnlag(persongrunnlagToUpdate: Persongrunnlag) {
        val filteredList: MutableList<Inntektsgrunnlag> = mutableListOf()

        for (inntektsgrunnlag in persongrunnlagToUpdate.inntektsgrunnlagListe) {
            if (inntektsgrunnlag.bruk) {
                // PENPORT-161 (21.11.2011) remove all inntektsgrunnlag where inntektType equal FPI
                if (inntektsgrunnlag.inntektType.kode != InntektType.FPI.name) {
                    filteredList.add(inntektsgrunnlag)
                }
            }
        }

        persongrunnlagToUpdate.inntektsgrunnlagListe = filteredList
    }
    */

    private fun finnRegelverkType(spec: SimuleringSpec): RegelverkTypeCti {
        /*ANON? val pid = spec.fnr

     val foedselAar: Int = if (pid != null) {
         Pid.get4DigitYearOfBirth(pid.toString())
     } else {
         spec.fodselsar
     }*/
        val foedselAar = spec.foedselAar

        if (foedselAar < 1943) {
            throw BrukerFoedtFoer1943Exception("FPEN028 - Kan ikke sett regelverktype - Fødselsår < 1943")
        }

        if (foedselAar <= 1953) {
            return RegelverkTypeCti(RegelverkType.N_REG_G_OPPTJ.name)
        }

        if (foedselAar <= 1962) {
            return RegelverkTypeCti(RegelverkType.N_REG_G_N_OPPTJ.name)
        }

        return RegelverkTypeCti(RegelverkType.N_REG_N_OPPTJ.name)
    }

    // SimulerFleksibelAPCommand.opprettPersongrunnlagForBruker
    private fun opprettSokergrunnlag(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        person: PenPerson?
    ): Kravhode {
        if (spec.erAnonym) {
            kravhode.persongrunnlagListe.add(forenkletSokergrunnlag(spec))
            return kravhode
        }

        val response: Kravhode = addSokerPersongrunnlagToKravForNormalSimulering(spec, kravhode, person!!)
        beholdningUpdater.updateBeholdningFromEksisterendePersongrunnlag(kravhode)
        return response
    }

    // OpprettKravHodeHelper.opprettPersongrunnlagForBruker
    fun addSokerPersongrunnlagToKravForNormalSimulering(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        person: PenPerson
    ): Kravhode {
        val grunnlag = persongrunnlagMapper.mapToPersongrunnlag(person, spec)
        kravhode.persongrunnlagListe.add(grunnlag)
        addBeholdningerToPersongrunnlag(grunnlag, kravhode, person.pid!!, person.fodselsdato!!, false)
        return kravhode
    }

    // SimulerFleksibelAPCommand.opprettPersongrunnlagForBrukerForenkletSimulering
    private fun forenkletSokergrunnlag(spec: SimuleringSpec) =
        Persongrunnlag().apply {
            penPerson = PenPerson().apply { penPersonId = FORENKLET_SIMULERING_PERSON_ID }
            fodselsdato = legacyFoersteDag(spec.foedselAar)
            antallArUtland = spec.utlandAntallAar
            statsborgerskap = norge
            flyktning = false
            bosattLand = norge
            personDetaljListe = mutableListOf(forenkletPersonDetalj(spec))
            inngangOgEksportGrunnlag = InngangOgEksportGrunnlag().apply { fortsattMedlemFT = true }
            sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
        }.also { it.finishInit() }

    // SimulerFleksibelAPCommand.createPersonDetaljerForenkletSimulering
    private fun forenkletPersonDetalj(spec: SimuleringSpec) =
        PersonDetalj().apply {
            grunnlagKilde = GrunnlagKildeCti(GrunnlagKilde.BRUKER.name)
            grunnlagsrolle = GrunnlagsrolleCti(GrunnlagRolle.SOKER.name)
            rolleFomDato = legacyFoersteDag(spec.foedselAar)
            sivilstandType = SivilstandTypeCti(forenkletSivilstand(spec.sivilstatus).name)
            bruk = true
        }.also { it.finishInit() }

    // SimulerFleksibelAPCommand.getSivilstandForenkletSimulering
    private fun forenkletSivilstand(sivilstatus: SivilstatusType) =
        when (sivilstatus) {
            SivilstatusType.GIFT -> SivilstandType.GIFT
            SivilstatusType.REPA -> SivilstandType.REPA
            else -> SivilstandType.UGIF
        }

    // OpprettKravHodeHelper.oppdaterGrunnlagMedBeholdninger
    private fun addBeholdningerToPersongrunnlag(
        persongrunnlag: Persongrunnlag,
        kravhode: Kravhode,
        pid: Pid,
        fodselsdato: Date,
        hentBeholdninger: Boolean
    ) {
        /*ANON
        val request = BeholdningerMedGrunnlagSpec(pid, fodselsdato, kravhode, true, true, hentBeholdninger)

        with(context.hentBeholdningerMedGrunnlag(request)) {
            persongrunnlag.opptjeningsgrunnlagListe = opptjeningsgrunnlagListe
            persongrunnlag.omsorgsgrunnlagListe = omsorgsgrunnlagListe
            persongrunnlag.inntektsgrunnlagListe = inntektsgrunnlagListe
            persongrunnlag.dagpengegrunnlagListe = dagpengegrunnlagListe
            persongrunnlag.forstegangstjenestegrunnlag = forstegangstjenestegrunnlag

            if (hentBeholdninger) {
                persongrunnlag.beholdninger.addAll(beholdninger)
            }
        }
        */
    }

    // OpprettKravHodeHelper.opprettPersongrunnlag
    private fun addSokerGrunnlagToKrav(
        simulatorInput: SimuleringSpec,
        kravhode: Kravhode,
        person: PenPerson?,
        forrigeAlderspensjonBeregningsresultat: AbstraktBeregningsResultat?,
        grunnbelop: Int
    ) {
        val updatedKravhode: Kravhode =
            when {
                /*ANON
                simulatorInput.gjelderAfpOffentligPre2025() -> person?.let {
                    AfpPersongrunnlag(context).opprettSokerGrunnlag(
                        it,
                        simulatorInput,
                        kravhode,
                        forrigeAlderspensjonBeregningsresultat
                    )
                } ?: kravhode

                simulatorInput.gjelderEndring() -> person?.let {
                    EndringPersongrunnlag(context).opprettSokerGrunnlag(
                        it,
                        simulatorInput,
                        kravhode,
                        forrigeAlderspensjonBeregningsresultat
                    )
                } ?: kravhode
                */
                else -> opprettSokergrunnlag(simulatorInput, kravhode, person)
            }

        val persongrunnlag = updatedKravhode.hentPersongrunnlagForSoker()
        val brukFremtidigInntekt = simulatorInput.fremtidigInntektListe.isNotEmpty()
        val inntekter: MutableList<Inntekt>

        if (brukFremtidigInntekt) {
            val gjeldendeAr = SISTE_GYLDIGE_OPPTJENING_AAR + 1
            val sisteOpptjeningsAr = MAX_OPPTJENING_ALDER + simulatorInput.foedselAar
            val fom = foersteDag(gjeldendeAr)

            val inntektsgrunnlagList =
                ArrayList(fjernForventetArbeidsinntektFraInntektsgrunnlag(persongrunnlag.inntektsgrunnlagListe))
                    .also {
                        it.addAll(
                            inntektsgrunnlagListeFraFremtidigeInntekter(
                                simulatorInput,
                                gjeldendeAr,
                                sisteOpptjeningsAr,
                                fom
                            )
                        )
                    }

            persongrunnlag.inntektsgrunnlagListe = inntektsgrunnlagList
            inntekter = inntekter(inntektsgrunnlagList)
        } else {
            inntekter = aarligeInntekterFraDagensDato(simulatorInput, grunnbelop, person?.fodselsdato)
            persongrunnlag.inntektsgrunnlagListe =
                opprettInntektsgrunnlagForSoeker(simulatorInput, persongrunnlag.inntektsgrunnlagListe)
        }

        persongrunnlag.opptjeningsgrunnlagListe =
            oppdaterOpptjeningsgrunnlagFraInntekter(
                inntekter,
                persongrunnlag.opptjeningsgrunnlagListe,
                persongrunnlag.fodselsdato.toLocalDate()
            )
    }

    private fun addEpsGrunnlagToKrav(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeAlderspensjonBeregningResult: AbstraktBeregningsResultat?,
        grunnbeloep: Int
    ) {
        when {
            spec.gjelderPre2025OffentligAfp() -> addPre2025OffentligAfpEpsGrunnlagToKrav(
                spec,
                kravhode,
                forrigeAlderspensjonBeregningResult,
                grunnbeloep
            )

            spec.gjelderEndring() -> addEndringEpsGrunnlagToKrav(
                spec,
                kravhode,
                forrigeAlderspensjonBeregningResult,
                grunnbeloep
            )

            else -> addAlderspensjonEpsGrunnlagToKrav(spec, kravhode, grunnbeloep)
        }
    }

    private fun oppdaterOpptjeningsgrunnlagFraInntekter(
        inntektListe: List<Inntekt>,
        opptjeningsgrunnlagListe: List<Opptjeningsgrunnlag>,
        foedselDato: LocalDate?
    ): MutableList<Opptjeningsgrunnlag> {
        var grunnlagListe: MutableList<Opptjeningsgrunnlag> = mutableListOf()

        for (inntekt in inntektListe) {
            if (inntekt.beloep > 0L) {
                grunnlagListe.add(opptjeningsgrunnlag(inntekt))
            }
        }

        grunnlagListe = context.beregnPoengtallBatch(grunnlagListe, foedselDato)
        val opptjeningsgrunnlagListToReturn: MutableList<Opptjeningsgrunnlag> = ArrayList(opptjeningsgrunnlagListe)

        for (opptjeningsgrunnlag in grunnlagListe) {
            opptjeningsgrunnlag.bruk = true
            opptjeningsgrunnlag.grunnlagKilde = GrunnlagKildeCti(GrunnlagKilde.BRUKER.name)
            opptjeningsgrunnlagListToReturn.add(opptjeningsgrunnlag)
        }

        return opptjeningsgrunnlagListToReturn
    }

    // OpprettKravHodeHelper.finnListeOverInntektPerArFraDagensDato
    // NB: fodselsdato is undefined if anonym simulering
    private fun aarligeInntekterFraDagensDato(
        spec: SimuleringSpec,
        grunnbeloep: Int,
        foedselDato: Date?
    ): MutableList<Inntekt> {
        var veietGrunnbeloepListe: List<VeietSatsResultat> = emptyList()
        val innevaerendeAar = LocalDate.now().year
        val gjeldendeAar: Int
        val aarSoekerBlirMaxAlder: Int

        if (spec.erAnonym) {
            gjeldendeAar = (spec.foersteUttakDato?.year ?: 0) - spec.inntektOver1GAntallAar
            aarSoekerBlirMaxAlder = MAX_OPPTJENING_ALDER + spec.foedselAar
            if (gjeldendeAar < innevaerendeAar) {
                veietGrunnbeloepListe =
                    generelleDataHolder.getVeietGrunnbeloepListe(gjeldendeAar, aarSoekerBlirMaxAlder)
            }
        } else {
            gjeldendeAar = SISTE_GYLDIGE_OPPTJENING_AAR + 1
            aarSoekerBlirMaxAlder = yearUserTurnsGivenAge(foedselDato!!, MAX_OPPTJENING_ALDER)
        }

        val forventetInntekt = spec.forventetInntektBeloep
        val inntektUnderGradertUttak =
            if (spec.uttakGrad == UttakGradKode.P_100) 0 else spec.inntektUnderGradertUttakBeloep
        val inntektEtterHeltUttak = spec.inntektEtterHeltUttakBeloep
        val inntekter: MutableList<Inntekt> = mutableListOf()

        for (aar in gjeldendeAar..aarSoekerBlirMaxAlder) {
            val beregnetForventetInntekt =
                (forventetInntekt * forventetInntektAntallMaaneder(aar, spec) / MAANEDER_PER_AAR).toLong()
            val beregnetInntektUnderGradertUttak = (inntektUnderGradertUttak *
                    antallManederMedInntektUnderGradertUttak(aar, spec) / MAANEDER_PER_AAR).toLong()
            val beregnetInntektEtterHeltUttak = (inntektEtterHeltUttak *
                    antallMndMedInntektEtterHeltUttak(aar, spec) / MAANEDER_PER_AAR).toLong()
            val forhold = calculateGrunnbelopForhold(aar, spec, veietGrunnbeloepListe, grunnbeloep)

            inntekter.add(
                Inntekt(
                    inntektAar = aar,
                    beloep = Math.round(forhold * (beregnetForventetInntekt + beregnetInntektUnderGradertUttak + beregnetInntektEtterHeltUttak))
                )
            )
        }

        return inntekter
    }

    private fun antallMndMedInntektEtterHeltUttak(aar: Int, spec: SimuleringSpec): Int {
        val foersteUttakAar: Int = spec.foersteUttakDato?.year ?: 0
        val antallAarInntektEtterHeltUttak: Int = spec.inntektEtterHeltUttakAntallAar ?: 0
        val foersteUttakMaaned = monthOfYearRange1To12(spec.foersteUttakDato)
        val isHeltUttak = spec.uttakGrad == UttakGradKode.P_100
        val heltUttakAar: Int = if (isHeltUttak) foersteUttakAar else spec.heltUttakDato?.year ?: 0
        val heltUttakMaaned =
            if (isHeltUttak) foersteUttakMaaned else monthOfYearRange1To12(spec.heltUttakDato)

        if (aar == heltUttakAar) {
            val mndMedInntektUnderGradertUttak = heltUttakMaaned - 1
            return MAANEDER_PER_AAR - mndMedInntektUnderGradertUttak
        }

        if (aar > heltUttakAar && aar - heltUttakAar < antallAarInntektEtterHeltUttak) {
            return MAANEDER_PER_AAR
        }

        if (aar - heltUttakAar == antallAarInntektEtterHeltUttak) {
            return heltUttakMaaned - 1
        }

        return 0
    }

    private fun antallManederMedInntektUnderGradertUttak(aar: Int, spec: SimuleringSpec): Int {
        val foersteUttakAar: Int = spec.foersteUttakDato?.year ?: 0
        val foersteUttakMaaned = monthOfYearRange1To12(spec.foersteUttakDato)
        val isHeltUttak = spec.uttakGrad == UttakGradKode.P_100
        val heltUttakAar: Int = if (isHeltUttak) foersteUttakAar else spec.heltUttakDato?.year ?: 0
        val heltUttakMaaned =
            if (isHeltUttak) foersteUttakMaaned else monthOfYearRange1To12(spec.heltUttakDato)

        if (aar == foersteUttakAar) {
            return if (foersteUttakAar != heltUttakAar) {
                val mndMedForventetInntekt = foersteUttakMaaned - 1
                MAANEDER_PER_AAR - mndMedForventetInntekt
            } else {
                heltUttakMaaned - foersteUttakMaaned
            }
        }

        if (aar in (foersteUttakAar + 1) until heltUttakAar) {
            return MAANEDER_PER_AAR
        }

        if (aar == heltUttakAar) {
            return heltUttakMaaned - 1
        }

        return 0
    }

    private fun forventetInntektAntallMaaneder(aar: Int, spec: SimuleringSpec): Int {
        val foersteUttakAar: Int = spec.foersteUttakDato?.year ?: 0

        return when {
            aar < foersteUttakAar -> MAANEDER_PER_AAR - utlandMaanederInnenforAaret(spec, aar)
            aar == foersteUttakAar -> monthOfYearRange1To12(spec.foersteUttakDato) - 1 - utlandMaanederInnenforRestenAvAaret(spec)
            else -> 0
        }
    }

    private fun afpOffentligPre2025Uttaksgrader(
        simulatorInput: SimuleringSpec,
        forrigeResultat: AbstraktBeregningsResultat?
    ): MutableList<Uttaksgrad> =
        mutableListOf() //ANON AfpUttaksgrad(context).uttaksgrader(simulatorInput, forrigeResultat, fodselsdato(simulatorInput))

    private fun endringUttaksgrader(
        simulatorInput: SimuleringSpec,
        forrigeResultat: AbstraktBeregningsResultat?
    ): MutableList<Uttaksgrad> =
        mutableListOf() //ANON EndringUttaksgrad(context).uttaksgrader(simulatorInput, forrigeResultat?.kravId)

    private companion object {
        private const val MAX_ALDER = 80
        private const val MAX_OPPTJENING_ALDER = 75
        private const val MAX_UTTAKSGRAD = 100
        private const val GRUNNBELOP_MULTIPLIER = 3
        private const val FORENKLET_SIMULERING_PERSON_ID = -1L
        private val norge = LandCti(Land.NOR.name)

        // OpprettKravHodeHelper.finnUttaksgradListe
        // -> SimulerFleksibelAPCommand.finnUttaksgradListe
        private fun alderspensjonUttaksgrader(spec: SimuleringSpec): MutableList<Uttaksgrad> {
            val uttaksgrader = mutableListOf(angittUttaksgrad(spec))

            if (erGradertUttak(spec)) {
                uttaksgrader.add(helUttaksgrad(spec.heltUttakDato))
            }

            return uttaksgrader
        }

        // SimulerFleksibelAPCommand.createUttaksgradChosenByUser
        private fun angittUttaksgrad(spec: SimuleringSpec) =
            Uttaksgrad().apply {
                fomDato = fromLocalDate(spec.foersteUttakDato)
                uttaksgrad = spec.uttakGrad.value.toInt()

                if (erGradertUttak(spec)) {
                    val dayBeforeHeltUttak = getRelativeDateByDays(spec.heltUttakDato, -1)
                    tomDato = fromLocalDate(dayBeforeHeltUttak)
                }
            }.also { it.finishInit() }

        // OpprettKravHodeHelper.createArliginntekt
        private fun arligInntekt(aarligInntektListe: List<FremtidigInntekt>): BigInteger {
            val iterator = aarligInntektListe.listIterator()
            if (!iterator.hasNext()) return BigInteger.ZERO

            var inntekt = iterator.next()
            var nextInntekt: FremtidigInntekt
            var aarligInntekt = BigInteger.ZERO

            while (iterator.hasNext()) {
                nextInntekt = iterator.next()
                val periodeAntallManeder = nextInntekt.fom.monthValue - inntekt.fom.monthValue
                aarligInntekt = aarligInntekt.add(periodevisInntekt(inntekt, periodeAntallManeder))
                inntekt = nextInntekt
            }

            val sistePeriodeAntallManeder = MAANEDER_PER_AAR - inntekt.fom.monthValue
            return aarligInntekt.add(periodevisInntekt(inntekt, sistePeriodeAntallManeder))
        }

        private fun addFremtidigInntektAtStartOfEachYear(
            sortertInntektListe: MutableList<FremtidigInntekt>,
            sisteOpptjeningAar: Int
        ) {
            val inntektIterator = sortertInntektListe.listIterator()
            var gjeldendeInntekt = inntektIterator.next()
            var currentInntekt: FremtidigInntekt

            while (inntektIterator.hasNext()) {
                currentInntekt = inntektIterator.next()

                if (sammeAr(currentInntekt, gjeldendeInntekt) || starterAretEtter(
                        currentInntekt,
                        gjeldendeInntekt
                    ) && starterJanuar(currentInntekt)
                ) {
                    gjeldendeInntekt = currentInntekt
                } else if (starterAretEtter(currentInntekt, gjeldendeInntekt) && !starterJanuar(currentInntekt)
                    || currentInntekt.fom.year > aretEtter(gjeldendeInntekt)
                ) {
                    val firstOfYear = foersteDag(aretEtter(gjeldendeInntekt))
                    val nyInntekt = FremtidigInntekt(gjeldendeInntekt.aarligInntektBeloep, firstOfYear)
                    inntektIterator.previous()
                    inntektIterator.add(nyInntekt)
                    gjeldendeInntekt = nyInntekt
                }
            }

            val lastYearWithFremtidigInntekt = gjeldendeInntekt.fom.year

            if (lastYearWithFremtidigInntekt < sisteOpptjeningAar) {
                addFremtidigInntektForHvertArInntilSisteOpptjeningsar(
                    lastYearWithFremtidigInntekt,
                    sisteOpptjeningAar,
                    inntektIterator,
                    gjeldendeInntekt
                )
            }
        }

        private fun addFremtidigInntektForHvertArInntilSisteOpptjeningsar(
            sisteAarMedFremtidigInntekt: Int,
            sisteOpptjeningAar: Int, fremtidigInntektIterator: MutableListIterator<FremtidigInntekt>,
            gjeldendeFremtidigInntekt: FremtidigInntekt
        ) {
            for (ar in sisteAarMedFremtidigInntekt + 1..sisteOpptjeningAar) {
                fremtidigInntektIterator.add(
                    FremtidigInntekt(
                        aarligInntektBeloep = gjeldendeFremtidigInntekt.aarligInntektBeloep,
                        fom = foersteDag(ar)
                    )
                )
            }
        }

        private fun validateSortedFremtidigeInntekter(sortertInntektListe: MutableList<FremtidigInntekt>) {
            require(!sortertInntektListe.any { !isFirstDayOfMonth(it.fom) }) {
                "Det er en fremtidig inntekt med f.o.m. som ikke er den 1. i måneden"
            }

            require(!sortertInntektListe.any { it.aarligInntektBeloep < 0 }) {
                "Det er en fremtidig inntekt med negativ verdi"
            }

            IntStream.range(0, sortertInntektListe.size - 1)
                .forEach {
                    require(sortertInntektListe[it].fom != sortertInntektListe[it + 1].fom) { "De er to fremtidige inntekter med samme f.o.m." }
                }
        }

        private fun fjernForventetArbeidsinntektFraInntektsgrunnlag(grunnlagListe: List<Inntektsgrunnlag>) =
            grunnlagListe.filter { it.bruk && it.inntektType!!.kode != InntektType.FPI.name }

        private fun opprettInntektsgrunnlagForSoeker(
            spec: SimuleringSpec,
            existingInntektsgrunnlagList: MutableList<Inntektsgrunnlag>
        ): MutableList<Inntektsgrunnlag> {
            val inntektsgrunnlagListe: MutableList<Inntektsgrunnlag> = mutableListOf()

            // Inntekt frem til første uttak
            if (isAfterToday(spec.foersteUttakDato) && spec.forventetInntektBeloep > 0) {
                val beloep = spec.forventetInntektBeloep
                val fom = LocalDate.now()
                val tom = getRelativeDateByDays(spec.foersteUttakDato, -1)
                inntektsgrunnlagListe.add(inntektsgrunnlagForSokerOrEps(beloep, fom, tom))
            }

            val isHeltUttak = spec.uttakGrad == UttakGradKode.P_100
            val inntektUnderGradertUttak: Int = spec.inntektUnderGradertUttakBeloep

            if (!isHeltUttak && inntektUnderGradertUttak > 0) {
                val beloep = inntektUnderGradertUttak
                val fom = spec.foersteUttakDato
                val tom = getRelativeDateByDays(spec.heltUttakDato, -1)
                inntektsgrunnlagListe.add(inntektsgrunnlagForSokerOrEps(beloep, fom, tom))
            }

            val antallArInntektEtterHeltUttak: Int = spec.inntektEtterHeltUttakAntallAar ?: 0

            if (spec.inntektEtterHeltUttakBeloep > 0 && antallArInntektEtterHeltUttak > 0) {
                val beloep = spec.inntektEtterHeltUttakBeloep
                val fom = if (isHeltUttak) spec.foersteUttakDato else spec.heltUttakDato
                val tom = getRelativeDateByDays(getRelativeDateByYear(fom, antallArInntektEtterHeltUttak), -1)
                inntektsgrunnlagListe.add(inntektsgrunnlagForSokerOrEps(beloep, fom!!, tom))
            }

            inntektsgrunnlagListe.addAll(existingInntektsgrunnlagList.filter {
                it.bruk && !isForventetPensjongivendeInntekt(it)
            })

            return inntektsgrunnlagListe
        }

        private fun isForventetPensjongivendeInntekt(grunnlag: Inntektsgrunnlag): Boolean =
            grunnlag.inntektType?.let { it.kode == InntektType.FPI.name } ?: false

        private fun calculateGrunnbelopForhold(
            aar: Int,
            spec: SimuleringSpec,
            veietGrunnbeloepListe: List<VeietSatsResultat>,
            grunnbeloep: Int
        ): Double {
            val innevarendeAar = LocalDate.now().year

            return if (spec.erAnonym && aar < innevarendeAar)
                findValidForYear(veietGrunnbeloepListe, aar)?.let { it.verdi / grunnbeloep } ?: 1.0
            else
                1.0
        }

        private fun oensketVirkningDato(simulatorInput: SimuleringSpec) =
            if (simulatorInput.erAnonym) null else finnOensketVirkningDato(simulatorInput)

        private fun finnOensketVirkningDato(simulatorInput: SimuleringSpec) =
            simulatorInput.heltUttakDato ?: simulatorInput.foersteUttakDato

        private fun inntekter(inntektsgrunnlagList: MutableList<Inntektsgrunnlag>) =
            inntektsgrunnlagList.map {
                Inntekt(
                    beloep = it.belop.toLong(),
                    inntektAar = getYear(it.fom)
                )
            }.toMutableList()

        private fun inntektsgrunnlagListeFraFremtidigeInntekter(
            spec: SimuleringSpec,
            gjeldendeAar: Int,
            sisteOpptjeningAar: Int,
            fom: LocalDate
        ): List<Inntektsgrunnlag> {
            val fremtidigeInntekter = spec.fremtidigInntektListe

            if (fremtidigeInntekter.isEmpty() || doesNotHaveFremtidigInntektBeforeFom(spec, fom)) {
                fremtidigeInntekter.add(FremtidigInntekt(aarligInntektBeloep = 0, fom = fom))
            }

            val sortedFremtidigeInntekter = fremtidigeInntekter.toMutableList()
            sortedFremtidigeInntekter.sortBy { it.fom }
            validateSortedFremtidigeInntekter(sortedFremtidigeInntekter)
            addFremtidigInntektAtStartOfEachYear(sortedFremtidigeInntekter, sisteOpptjeningAar)

            return IntStream.rangeClosed(gjeldendeAar, sisteOpptjeningAar)
                .toList()
                .map { inntektsgrunnlagListeForAret(sortedFremtidigeInntekter, it) }
        }

        private fun inntektsgrunnlagListeForAret(
            inntektListe: MutableList<FremtidigInntekt>,
            aar: Int
        ): Inntektsgrunnlag {
            val arligeInntekter = inntektListe.filter { it.fom.year == aar }
            return inntektsgrunnlagForAret(aar, arligeInntekter)
        }

        private fun inntektsgrunnlagForAret(aar: Int, aaretsInntekListe: List<FremtidigInntekt>) =
            Inntektsgrunnlag().apply {
                fom = fromLocalDate(foersteDag(aar))
                tom = createDate(aar, Calendar.DECEMBER, 31)
                belop = arligInntekt(aaretsInntekListe).toInt()
                bruk = true
                grunnlagKilde = GrunnlagKildeCti(GrunnlagKilde.BRUKER.name)
                inntektType = InntektTypeCti(InntektType.FPI.name)
            }

        private fun inntektsgrunnlagForSokerOrEps(belop: Int, fom: LocalDate?, tom: LocalDate?) =
            Inntektsgrunnlag().apply {
                this.belop = belop
                this.bruk = true
                this.fom = fromLocalDate(fom)
                this.grunnlagKilde = GrunnlagKildeCti(GrunnlagKilde.BRUKER.name)
                this.inntektType = InntektTypeCti(InntektType.FPI.name)
                this.tom = fromLocalDate(tom)
            }

        private fun norskKravlinje(kravlinjeType: KravlinjeTypePlus, person: PenPerson) =
            Kravlinje(kravlinjeType = kravlinjeType(kravlinjeType), relatertPerson = person).apply {
                kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                land = Land.NOR
            }

        private fun opptjeningsgrunnlag(inntekt: Inntekt) =
            Opptjeningsgrunnlag().apply {
                ar = inntekt.inntektAar
                pi = inntekt.beloep.toInt()
                opptjeningType = OpptjeningTypeCti(OpptjeningType.PPI.name)
                bruk = true
            }

        private fun helUttaksgrad(fom: LocalDate?) =
            Uttaksgrad().apply {
                fomDato = fromLocalDate(fom)
                uttaksgrad = MAX_UTTAKSGRAD
            }

        // Extracted from OpprettKravHodeHelper.createArliginntekt
        private fun periodevisInntekt(inntekt: FremtidigInntekt, periodOfMonths: Int) =
            BigInteger.valueOf(inntekt.aarligInntektBeloep.toLong())
                .multiply(BigInteger.valueOf(periodOfMonths.toLong()))
                .divide(BigInteger.valueOf(MAANEDER_PER_AAR.toLong()))

        private fun doesNotHaveFremtidigInntektBeforeFom(simulatorInput: SimuleringSpec, fom: LocalDate) =
            simulatorInput.fremtidigInntektListe.none { isBeforeByDay(it.fom, fom, true) }

        private fun harUtenlandsopphold(antallAarUtenlands: Int?, trygdetidperioder: List<TTPeriode>) =
            if (antallAarUtenlands == null) containsTrygdetidAbroad(trygdetidperioder) else antallAarUtenlands > 0

        private fun containsTrygdetidAbroad(trygdetidPeriodeListe: List<TTPeriode>) =
            trygdetidPeriodeListe.any { it.land!!.kode != Land.NOR.name }

        private fun erGradertUttak(spec: SimuleringSpec) = spec.uttakGrad != UttakGradKode.P_100

        private fun sammeAr(inntekt1: FremtidigInntekt, inntekt2: FremtidigInntekt) =
            inntekt1.fom.year == inntekt2.fom.year

        private fun starterJanuar(inntekt: FremtidigInntekt) =
            inntekt.fom.monthValue == 1

        private fun starterAretEtter(fremtidigInntekt: FremtidigInntekt, inntekt: FremtidigInntekt) =
            fremtidigInntekt.fom.year == aretEtter(inntekt)

        private fun aretEtter(inntekt: FremtidigInntekt) =
            inntekt.fom.year + 1

        private fun foersteDag(aar: Int) =
            LocalDate.of(aar, 1, 1)

        private fun legacyFoersteDag(aar: Int) =
            fromLocalDate(foersteDag(aar))
    }
}
