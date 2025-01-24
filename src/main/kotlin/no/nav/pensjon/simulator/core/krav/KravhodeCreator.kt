package no.nav.pensjon.simulator.core.krav

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpPersongrunnlag
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpUttaksgrad
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdater
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.beregn.InntektType
import no.nav.pensjon.simulator.core.domain.GrunnlagKilde
import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.OpptjeningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.endring.EndringPersongrunnlag
import no.nav.pensjon.simulator.core.endring.EndringUttakGrad
import no.nav.pensjon.simulator.core.exception.FeilISimuleringsgrunnlagetException
import no.nav.pensjon.simulator.core.exception.PersonForGammelException
import no.nav.pensjon.simulator.core.inntekt.InntektUtil.faktiskAarligInntekt
import no.nav.pensjon.simulator.core.krav.KravUtil.utlandMaanederFraAarStartTilFoersteUttakDato
import no.nav.pensjon.simulator.core.krav.KravUtil.utlandMaanederInnenforAaret
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterToday
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isFirstDayOfMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.monthOfYearRange1To12
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.person.PersongrunnlagService
import no.nav.pensjon.simulator.core.person.eps.EpsService
import no.nav.pensjon.simulator.core.result.OpptjeningType
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findValidForYear
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDag
import no.nav.pensjon.simulator.tech.time.DateUtil.sisteDag
import no.nav.pensjon.simulator.ufoere.UfoeretrygdUtbetalingService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*
import java.util.stream.IntStream
import kotlin.streams.toList

/**
 * Creates kravhode.
 * Corresponds to PEN class
 * no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OpprettKravhodeHelper
 */
@Component
class KravhodeCreator(
    private val context: SimulatorContext,
    private val beholdningUpdater: BeholdningUpdater,
    private val epsService: EpsService,
    private val persongrunnlagService: PersongrunnlagService,
    private val generelleDataHolder: GenerelleDataHolder,
    private val kravService: KravService,
    private val ufoereService: UfoeretrygdUtbetalingService,
    private val endringPersongrunnlag: EndringPersongrunnlag,
    private val endringUttakGrad: EndringUttakGrad,
    private val pre2025OffentligAfpPersongrunnlag: Pre2025OffentligAfpPersongrunnlag,
    private val pre2025OffentligAfpUttaksgrad: Pre2025OffentligAfpUttaksgrad
) {
    // OpprettKravhodeHelper.opprettKravhode
    // Personer will be undefined in forenklet simulering (anonymous)
    fun opprettKravhode(
        kravhodeSpec: KravhodeSpec,
        person: PenPerson?,
        virkningDatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
    ): Kravhode {
        val spec = kravhodeSpec.simulatorInput
        val forrigeAlderspensjonBeregningResultat = kravhodeSpec.forrigeAlderspensjonBeregningResult
        val grunnbeloep = kravhodeSpec.grunnbeloep
        val gjelderEndring = spec.gjelderEndring()
        val gjelderPre2025OffentligAfp = spec.gjelderPre2025OffentligAfp()

        val kravhode = Kravhode().apply {
            kravFremsattDato = Date()
            onsketVirkningsdato = oensketVirkningDato(spec)?.toNorwegianDateAtNoon()
            gjelder = null
            sakId = null
            sakType = SakType.ALDER
            regelverkTypeEnum = regelverkType(foedselAar(person, spec))
        }

        addPersongrunnlagForSoekerToKravhode(spec, kravhode, person, forrigeAlderspensjonBeregningResultat, grunnbeloep)
        addPersongrunnlagForEpsToKravhode(spec, kravhode, forrigeAlderspensjonBeregningResultat, grunnbeloep)

        if (kravTilsierBoddEllerArbeidetUtenlands(forrigeAlderspensjonBeregningResultat)) {
            kravhode.boddEllerArbeidetIUtlandet = true
        } else {
            kravhode.boddEllerArbeidetIUtlandet = harUtenlandsopphold(spec.utlandAntallAar, emptyList())
        }

        // NB: Next line requires avdød persongrunnlag to be fetched above
        val avdoedGrunnlag: Persongrunnlag? =
            kravhode.hentPersongrunnlagForRolle(rolle = GrunnlagsrolleEnum.AVDOD, checkBruk = false)

        kravhode.boddArbeidUtlandAvdod =
            avdoedGrunnlag?.let {
                harUtenlandsopphold(
                    antallAarUtenlands = spec.avdoed?.antallAarUtenlands,
                    trygdetidPeriodeListe = it.trygdetidPerioder
                )
            } == true

        addFoersteVirkningDatoGrunnlagToKravhode(kravhode, virkningDatoGrunnlagListe)

        kravhode.uttaksgradListe =
            when {
                gjelderPre2025OffentligAfp -> pre2025OffentligAfpUttaksgrad.uttaksgradListe(
                    spec,
                    forrigeAlderspensjonBeregningResultat,
                    foedselsdato = foedselsdato(person, spec) // NB: More robust than in PEN (which only uses spec.pid)
                )

                gjelderEndring -> endringUttakGrad.uttakGradListe(
                    spec,
                    forrigeAlderspensjonKravhodeId = forrigeAlderspensjonBeregningResultat?.kravId
                )

                else -> alderspensjonUttakGradListe(spec)
            }

        addKravlinjerToKravhode(kravhode)
        settGenerelleFelter(kravhode)
        updateOensketVirkningAndUtbetalingsgrad(spec, kravhode)
        return kravhode
    }

    private fun foedselAar(person: PenPerson?, spec: SimuleringSpec): Int =
        person?.fodselsdato?.toNorwegianLocalDate()?.year ?: spec.foedselAar

    private fun updateOensketVirkningAndUtbetalingsgrad(spec: SimuleringSpec, kravhode: Kravhode) {
        if (spec.erAnonym) return

        val persongrunnlag = kravhode.hentPersongrunnlagForSoker()

        //TODO reuse the utbetalingsgradUTListe obtained in BeholdningUpdaterUtil?
        persongrunnlag.utbetalingsgradUTListe =
            persongrunnlag.penPerson?.let { ufoereService.getUtbetalingGradListe(it.penPersonId) }
                .orEmpty().toMutableList()
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
        val foedselsdato: Date = kravhode.hentPersongrunnlagForSoker().fodselsdato!!
        val sisteGyldigeOpptjeningAar = yearUserTurnsGivenAge(foedselsdato, MAX_ALDER)
        kravhode.persongrunnlagListe.forEach { it.sisteGyldigeOpptjeningsAr = sisteGyldigeOpptjeningAar }
    }

    private fun addKravlinjerToKravhode(kravhode: Kravhode) {
        kravhode.kravlinjeListe =
            mutableListOf(
                norskKravlinje(
                    kravlinjeType = KravlinjeTypeEnum.AP,
                    person = kravhode.hentPersongrunnlagForSoker().penPerson!!
                )
            )

        val avdoedGrunnlag: Persongrunnlag? = kravhode.hentPersongrunnlagForRolle(
            rolle = GrunnlagsrolleEnum.AVDOD,
            checkBruk = false
        )

        avdoedGrunnlag?.penPerson?.let {
            kravhode.kravlinjeListe.add(
                norskKravlinje(
                    kravlinjeType = KravlinjeTypeEnum.GJR,
                    person = it
                )
            )
        }
    }

    // OpprettKravHodeHelper.leggTilForstevirkningsdatoGrunnlag
    private fun addFoersteVirkningDatoGrunnlagToKravhode(
        kravhode: Kravhode,
        virkningDatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
    ) {
        //TODO inspect this code:
        kravhode.persongrunnlagListe.forEach {
            it.forsteVirkningsdatoGrunnlagListe.toMutableList()
                .forEach { item -> it.forsteVirkningsdatoGrunnlagListe.remove(item) }
        }

        kravhode.persongrunnlagListe.forEach {
            addFoersteVirkningDatoGrunnlagToPersongrunnlag(
                persongrunnlag = it,
                virkningDatoGrunnlagListe
            )
        }
    }

    // OpprettKravHodeHelper.leggTilForstevirkningsdatoGrunnlag
    private fun addFoersteVirkningDatoGrunnlagToPersongrunnlag(
        persongrunnlag: Persongrunnlag,
        virkningDatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
    ) {
        if ((persongrunnlag.penPerson?.penPersonId ?: 0) <= 0) return

        virkningDatoGrunnlagListe.forEach(persongrunnlag.forsteVirkningsdatoGrunnlagListe::add)
    }

    // OpprettKravHodeHelper.isBoddArbeidUtlandTrueOnKravHode + findKravHode
    private fun kravTilsierBoddEllerArbeidetUtenlands(beregningResultat: AbstraktBeregningsResultat?): Boolean =
        beregningResultat?.kravId?.let(kravService::fetchKravhode)?.boddEllerArbeidetIUtlandet == true

    /* Simulering type ALDER_M_GJEN not yet supported
    private fun createPersongrunnlagInCaseOfGjenlevenderett(simulering: SimuleringSpec, kravhode: Kravhode) {
        val lastYear = LocalDate.now().year - 1

        // Del 1
        val persongrunnlag = persongrunnlagMapper.mapToPersongrunnlagAvdod(context.hentPenPerson(simulering.avdodPid), simulering)
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

    // SimulerFleksibelAPCommand.opprettPersongrunnlagForBruker
    private fun addPersongrunnlagForSoekerToKravhode(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        person: PenPerson? // null if anonym
    ) {
        if (spec.erAnonym) {
            kravhode.persongrunnlagListe.add(anonymPersongrunnlag(spec))
        }

        person?.let {
            with(persongrunnlagService.getPersongrunnlagForSoeker(spec, kravhode, it)) {
                kravhode.persongrunnlagListe.add(this)
            }
        }

        beholdningUpdater.updateBeholdningFromEksisterendePersongrunnlag(kravhode)
    }

    // SimulerFleksibelAPCommand.opprettPersongrunnlagForBrukerForenkletSimulering
    private fun anonymPersongrunnlag(spec: SimuleringSpec) =
        Persongrunnlag().apply {
            penPerson = PenPerson().apply { penPersonId = ANONYM_PERSON_ID }
            fodselsdato = legacyFoersteDag(spec.foedselAar)
            antallArUtland = spec.utlandAntallAar
            statsborgerskapEnum = norge
            flyktning = false
            bosattLandEnum = norge
            personDetaljListe = mutableListOf(anonymPersondetalj(spec))
            inngangOgEksportGrunnlag = InngangOgEksportGrunnlag().apply { fortsattMedlemFT = true }
            sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
        }.also { it.finishInit() }

    // SimulerFleksibelAPCommand.createPersonDetaljerForenkletSimulering
    private fun anonymPersondetalj(spec: SimuleringSpec) =
        PersonDetalj().apply {
            grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
            grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
            rolleFomDato = legacyFoersteDag(spec.foedselAar)
            sivilstandTypeEnum = anonymSivilstand(spec.sivilstatus)
            bruk = true
        }.also { it.finishInit() }

    // SimulerFleksibelAPCommand.getSivilstandForenkletSimulering
    private fun anonymSivilstand(sivilstatus: SivilstatusType): SivilstandEnum =
        when (sivilstatus) {
            SivilstatusType.GIFT -> SivilstandEnum.GIFT
            SivilstatusType.REPA -> SivilstandEnum.REPA
            else -> SivilstandEnum.UGIF
        }

    // OpprettKravHodeHelper.opprettPersongrunnlag
    private fun addPersongrunnlagForSoekerToKravhode(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        person: PenPerson?,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
        grunnbeloep: Int
    ) {
        when {
            spec.gjelderPre2025OffentligAfp() ->
                person?.let {
                    addPre2025OffentligAfpPersongrunnlagForSoekerToKravhode(
                        it,
                        spec,
                        kravhode,
                        forrigeAlderspensjonBeregningResultat
                    )
                }

            spec.gjelderEndring() ->
                person?.let {
                    addEndringPersongrunnlagForSoekerToKravhode(
                        it,
                        spec,
                        kravhode,
                        forrigeAlderspensjonBeregningResultat
                    )
                }

            else -> addPersongrunnlagForSoekerToKravhode(spec, kravhode, person)
        }

        val persongrunnlag = kravhode.hentPersongrunnlagForSoker()
        val brukFremtidigInntekt = spec.fremtidigInntektListe.isNotEmpty()
        val inntektListe: MutableList<Inntekt>

        if (brukFremtidigInntekt) {
            val gjeldendeAar = SISTE_GYLDIGE_OPPTJENING_AAR + 1
            val sisteOpptjeningAar = MAX_OPPTJENING_ALDER + foedselAar(person, spec)
            val fom: LocalDate = foersteDag(gjeldendeAar)

            val inntektsgrunnlagList =
                ArrayList(fjernForventetArbeidsinntektFraInntektGrunnlag(persongrunnlag.inntektsgrunnlagListe))
                    .also {
                        it.addAll(
                            inntektsgrunnlagListeFraFremtidigeInntekter(
                                spec,
                                gjeldendeAar,
                                sisteOpptjeningAar,
                                fom
                            )
                        )
                    }

            persongrunnlag.inntektsgrunnlagListe = inntektsgrunnlagList
            inntektListe = inntektListe(inntektsgrunnlagList)
        } else {
            inntektListe = aarligeInntekterFraDagensDato(spec, grunnbeloep, person?.fodselsdato)
            persongrunnlag.inntektsgrunnlagListe =
                opprettInntektGrunnlagForSoeker(spec, persongrunnlag.inntektsgrunnlagListe)
        }

        persongrunnlag.opptjeningsgrunnlagListe =
            oppdaterOpptjeningsgrunnlagFraInntekter(
                inntektListe,
                persongrunnlag.opptjeningsgrunnlagListe,
                persongrunnlag.fodselsdato!!.toNorwegianLocalDate()
            )
    }

    private fun addEndringPersongrunnlagForSoekerToKravhode(
        person: PenPerson,
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?
    ) {
        endringPersongrunnlag.getPersongrunnlagForSoeker(
            person,
            spec,
            kravhode,
            forrigeAlderspensjonBeregningResultat
        )?.let { kravhode.persongrunnlagListe.add(it) }
    }

    private fun addPre2025OffentligAfpPersongrunnlagForSoekerToKravhode(
        person: PenPerson,
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?
    ) {
        pre2025OffentligAfpPersongrunnlag.getPersongrunnlagForSoeker(
            person,
            spec,
            kravhode,
            forrigeAlderspensjonBeregningResultat
        )?.let { kravhode.persongrunnlagListe.add(it) }
    }

    private fun addPersongrunnlagForEpsToKravhode(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
        grunnbeloep: Int
    ) {
        when {
            spec.gjelderPre2025OffentligAfp() -> pre2025OffentligAfpPersongrunnlag.addPersongrunnlagForEpsToKravhode(
                spec,
                kravhode,
                forrigeAlderspensjonBeregningResultat,
                grunnbeloep
            )

            spec.gjelderEndring() -> endringPersongrunnlag.addPersongrunnlagForEpsToKravhode(
                spec,
                kravhode,
                forrigeAlderspensjonBeregningResultat,
                grunnbeloep
            )

            else -> epsService.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep)
        }
    }

    // PEN: AbstraktSimulerAPFra2011Command.oppdaterOpptjeningsgrunnlagFraInntektListe
    // -> OpprettKravHodeHelper.oppdaterOpptjeningsgrunnlagFraInntektListe
    private fun oppdaterOpptjeningsgrunnlagFraInntekter(
        inntektListe: List<Inntekt>,
        opptjeningsgrunnlagListe: List<Opptjeningsgrunnlag>,
        foedselsdato: LocalDate?
    ): MutableList<Opptjeningsgrunnlag> {
        var grunnlagListe: MutableList<Opptjeningsgrunnlag> = mutableListOf()

        for (inntekt in inntektListe) {
            if (inntekt.beloep > 0L) {
                grunnlagListe.add(opptjeningsgrunnlag(inntekt))
            }
        }

        grunnlagListe = context.beregnPoengtallBatch(grunnlagListe, foedselsdato)
        val opptjeningsgrunnlagListToReturn: MutableList<Opptjeningsgrunnlag> = ArrayList(opptjeningsgrunnlagListe)

        for (opptjeningsgrunnlag in grunnlagListe) {
            opptjeningsgrunnlag.bruk = true
            opptjeningsgrunnlag.grunnlagKilde = GrunnlagKildeCti(GrunnlagKilde.BRUKER.name)
            opptjeningsgrunnlagListToReturn.add(opptjeningsgrunnlag)
        }

        return opptjeningsgrunnlagListToReturn
    }

    // PEN: OpprettKravHodeHelper.finnListeOverInntektPerArFraDagensDato
    private fun aarligeInntekterFraDagensDato(
        spec: SimuleringSpec,
        grunnbeloep: Int,
        foedselsdato: Date? // null if anonym
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
            aarSoekerBlirMaxAlder = yearUserTurnsGivenAge(foedselsdato!!, MAX_OPPTJENING_ALDER)
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
                    antallMaanederMedInntektUnderGradertUttak(aar, spec) / MAANEDER_PER_AAR).toLong()
            val beregnetInntektEtterHeltUttak = (inntektEtterHeltUttak *
                    antallMaanederMedInntektEtterHeltUttak(aar, spec) / MAANEDER_PER_AAR).toLong()
            val forhold = calculateGrunnbeloepForhold(aar, spec, veietGrunnbeloepListe, grunnbeloep)

            inntekter.add(
                Inntekt(
                    inntektAar = aar,
                    beloep = Math.round(forhold * (beregnetForventetInntekt + beregnetInntektUnderGradertUttak + beregnetInntektEtterHeltUttak))
                )
            )
        }

        return inntekter
    }

    private fun antallMaanederMedInntektEtterHeltUttak(aar: Int, spec: SimuleringSpec): Int {
        val foersteUttakAar: Int = spec.foersteUttakDato?.year ?: 0
        val antallAarInntektEtterHeltUttak: Int = spec.inntektEtterHeltUttakAntallAar ?: 0
        val foersteUttakMaaned = monthOfYearRange1To12(spec.foersteUttakDato!!)
        val isHeltUttak = spec.uttakGrad == UttakGradKode.P_100
        val heltUttakAar: Int = if (isHeltUttak) foersteUttakAar else spec.heltUttakDato?.year ?: 0
        val heltUttakMaaned =
            if (isHeltUttak) foersteUttakMaaned else monthOfYearRange1To12(spec.heltUttakDato!!)

        if (aar == heltUttakAar) {
            val antallMaanederMedInntektUnderGradertUttak = heltUttakMaaned - 1
            return MAANEDER_PER_AAR - antallMaanederMedInntektUnderGradertUttak
        }

        if (heltUttakAar < aar && aar < heltUttakAar + antallAarInntektEtterHeltUttak) {
            return MAANEDER_PER_AAR
        }

        if (aar - heltUttakAar == antallAarInntektEtterHeltUttak) {
            return heltUttakMaaned - 1
        }

        return 0
    }

    private fun antallMaanederMedInntektUnderGradertUttak(aar: Int, spec: SimuleringSpec): Int {
        val foersteUttakAar: Int = spec.foersteUttakDato?.year ?: 0
        val foersteUttakMaaned = monthOfYearRange1To12(spec.foersteUttakDato!!)
        val isHeltUttak = spec.uttakGrad == UttakGradKode.P_100
        val heltUttakAar: Int = if (isHeltUttak) foersteUttakAar else spec.heltUttakDato?.year ?: 0
        val heltUttakMaaned =
            if (isHeltUttak) foersteUttakMaaned else monthOfYearRange1To12(spec.heltUttakDato!!)

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
            aar == foersteUttakAar -> monthOfYearRange1To12(spec.foersteUttakDato!!) - 1 - utlandMaanederFraAarStartTilFoersteUttakDato(spec)
            else -> 0
        }
    }

    private companion object {
        private const val MAX_ALDER = 80
        private const val MAX_OPPTJENING_ALDER = 75
        private const val MAX_UTTAKSGRAD = 100
        private const val ANONYM_PERSON_ID = -1L
        private val norge = LandkodeEnum.NOR

        private fun regelverkType(foedselAar: Int): RegelverkTypeEnum =
            when {
                foedselAar < 1943 -> throw PersonForGammelException("Kan ikke sette regelverktype - fødselsår < 1943")
                foedselAar <= 1953 -> RegelverkTypeEnum.N_REG_G_OPPTJ
                foedselAar <= 1962 -> RegelverkTypeEnum.N_REG_G_N_OPPTJ
                else -> RegelverkTypeEnum.N_REG_N_OPPTJ
            }

        // OpprettKravHodeHelper.finnUttaksgradListe
        // -> SimulerFleksibelAPCommand.finnUttaksgradListe
        private fun alderspensjonUttakGradListe(spec: SimuleringSpec): MutableList<Uttaksgrad> {
            val uttakGradListe = mutableListOf(angittUttaksgrad(spec))

            if (erGradertUttak(spec)) {
                uttakGradListe.add(uttaksgradForHeltUttak(spec.heltUttakDato))
            }

            return uttakGradListe
        }

        // SimulerFleksibelAPCommand.createUttaksgradChosenByUser
        private fun angittUttaksgrad(spec: SimuleringSpec) =
            Uttaksgrad().apply {
                fomDato = spec.foersteUttakDato?.toNorwegianDateAtNoon()
                uttaksgrad = spec.uttakGrad.value.toInt()

                if (erGradertUttak(spec)) {
                    if (spec.foersteUttakDato!!.isBefore(spec.heltUttakDato!!).not()) {
                        throw FeilISimuleringsgrunnlagetException(
                            "dato for første uttak (${spec.foersteUttakDato}) er ikke før" +
                                    " dato for helt uttak (${spec.heltUttakDato})"
                        )
                    }

                    val dayBeforeHeltUttak: LocalDate = getRelativeDateByDays(spec.heltUttakDato, -1)
                    tomDato = dayBeforeHeltUttak.toNorwegianDateAtNoon()
                }
            }.also { it.finishInit() }

        private fun addFremtidigInntektVedStartAvHvertAar(
            sortertInntektListe: MutableList<FremtidigInntekt>,
            sisteOpptjeningAar: Int
        ) {
            val inntektIterator = sortertInntektListe.listIterator()
            var gjeldendeInntekt = inntektIterator.next()
            var currentInntekt: FremtidigInntekt

            while (inntektIterator.hasNext()) {
                currentInntekt = inntektIterator.next()

                if (sammeAar(currentInntekt, gjeldendeInntekt)
                    || starterAaretEtter(currentInntekt, gjeldendeInntekt) && starterJanuar(currentInntekt)
                ) {
                    gjeldendeInntekt = currentInntekt
                } else if (starterAaretEtter(currentInntekt, gjeldendeInntekt) && !starterJanuar(currentInntekt)
                    || currentInntekt.fom.year > aaretEtter(gjeldendeInntekt)
                ) {
                    val firstOfYear = foersteDag(aaretEtter(gjeldendeInntekt))
                    val nyInntekt = FremtidigInntekt(gjeldendeInntekt.aarligInntektBeloep, firstOfYear)
                    inntektIterator.previous()
                    inntektIterator.add(nyInntekt)
                    gjeldendeInntekt = nyInntekt
                }
            }

            val sisteFremtidigInntektAar = gjeldendeInntekt.fom.year

            if (sisteFremtidigInntektAar < sisteOpptjeningAar) {
                addFremtidigInntektForHvertAarInntilSisteOpptjeningAar(
                    sisteFremtidigInntektAar,
                    sisteOpptjeningAar,
                    inntektIterator,
                    gjeldendeInntekt
                )
            }
        }

        private fun addFremtidigInntektForHvertAarInntilSisteOpptjeningAar(
            sisteFremtidigInntektAar: Int,
            sisteOpptjeningAar: Int,
            fremtidigInntektIterator: MutableListIterator<FremtidigInntekt>,
            gjeldendeFremtidigInntekt: FremtidigInntekt
        ) {
            for (aar in sisteFremtidigInntektAar + 1..sisteOpptjeningAar) {
                fremtidigInntektIterator.add(
                    FremtidigInntekt(
                        aarligInntektBeloep = gjeldendeFremtidigInntekt.aarligInntektBeloep,
                        fom = foersteDag(aar)
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

        private fun fjernForventetArbeidsinntektFraInntektGrunnlag(grunnlagListe: List<Inntektsgrunnlag>) =
            grunnlagListe.filter { it.bruk == true && InntektType.FPI.name != it.inntektType?.kode }

        private fun opprettInntektGrunnlagForSoeker(
            spec: SimuleringSpec,
            existingInntektsgrunnlagList: MutableList<Inntektsgrunnlag>
        ): MutableList<Inntektsgrunnlag> {
            val inntektsgrunnlagListe: MutableList<Inntektsgrunnlag> = mutableListOf()

            // Inntekt frem til første uttak
            if (isAfterToday(spec.foersteUttakDato) && spec.forventetInntektBeloep > 0) {
                inntektsgrunnlagListe.add(
                    inntektsgrunnlagForSoekerOrEps(
                        beloep = spec.forventetInntektBeloep,
                        fom = LocalDate.now(),
                        tom = getRelativeDateByDays(spec.foersteUttakDato!!, -1)
                    )
                )
            }

            val isHeltUttak = spec.uttakGrad == UttakGradKode.P_100
            val inntektUnderGradertUttak: Int = spec.inntektUnderGradertUttakBeloep

            if (!isHeltUttak && inntektUnderGradertUttak > 0) {
                inntektsgrunnlagListe.add(
                    inntektsgrunnlagForSoekerOrEps(
                        beloep = inntektUnderGradertUttak,
                        fom = spec.foersteUttakDato,
                        tom = getRelativeDateByDays(spec.heltUttakDato!!, -1)
                    )
                )
            }

            val antallArInntektEtterHeltUttak: Int = spec.inntektEtterHeltUttakAntallAar ?: 0

            if (spec.inntektEtterHeltUttakBeloep > 0 && antallArInntektEtterHeltUttak > 0) {
                val fom = if (isHeltUttak) spec.foersteUttakDato else spec.heltUttakDato
                val tom = getRelativeDateByDays(getRelativeDateByYear(fom!!, antallArInntektEtterHeltUttak), -1)
                inntektsgrunnlagListe.add(inntektsgrunnlagForSoekerOrEps(spec.inntektEtterHeltUttakBeloep, fom, tom))
            }

            inntektsgrunnlagListe.addAll(existingInntektsgrunnlagList.filter {
                it.bruk == true && !isForventetPensjongivendeInntekt(it)
            })

            return inntektsgrunnlagListe
        }

        private fun isForventetPensjongivendeInntekt(grunnlag: Inntektsgrunnlag): Boolean =
            grunnlag.inntektType?.let { it.kode == InntektType.FPI.name } == true

        private fun calculateGrunnbeloepForhold(
            aar: Int,
            spec: SimuleringSpec,
            veietGrunnbeloepListe: List<VeietSatsResultat>,
            grunnbeloep: Int
        ): Double {
            val innevaerendeAar = LocalDate.now().year

            return if (spec.erAnonym && aar < innevaerendeAar)
                findValidForYear(veietGrunnbeloepListe, aar)?.let { it.verdi / grunnbeloep } ?: 1.0
            else
                1.0
        }

        private fun oensketVirkningDato(spec: SimuleringSpec) =
            if (spec.erAnonym)
                null
            else
                spec.heltUttakDato ?: spec.foersteUttakDato

        private fun inntektListe(grunnlagListe: MutableList<Inntektsgrunnlag>) =
            grunnlagListe.map {
                Inntekt(
                    beloep = it.belop.toLong(),
                    inntektAar = getYear(it.fom!!)
                )
            }.toMutableList()

        private fun inntektsgrunnlagListeFraFremtidigeInntekter(
            spec: SimuleringSpec,
            gjeldendeAar: Int,
            sisteOpptjeningAar: Int,
            fom: LocalDate
        ): List<Inntektsgrunnlag> {
            val fremtidigInntektListe = spec.fremtidigInntektListe

            if (fremtidigInntektListe.isEmpty() || doesNotHaveFremtidigInntektBeforeFom(spec, fom)) {
                fremtidigInntektListe.add(FremtidigInntekt(aarligInntektBeloep = 0, fom))
            }

            val sortertInntektListe = fremtidigInntektListe.toMutableList().apply { this.sortBy { it.fom } }
            validateSortedFremtidigeInntekter(sortertInntektListe)
            addFremtidigInntektVedStartAvHvertAar(sortertInntektListe, sisteOpptjeningAar)

            return IntStream.rangeClosed(gjeldendeAar, sisteOpptjeningAar)
                .toList()
                .map { inntektsgrunnlagListeForAaret(inntektListe = sortertInntektListe, aar = it) }
        }

        private fun inntektsgrunnlagListeForAaret(
            inntektListe: MutableList<FremtidigInntekt>,
            aar: Int
        ): Inntektsgrunnlag =
            inntektsgrunnlagForAaret(aar, aaretsInntektListe = inntektListe.filter { it.fom.year == aar })

        private fun inntektsgrunnlagForAaret(aar: Int, aaretsInntektListe: List<FremtidigInntekt>) =
            Inntektsgrunnlag().apply {
                fom = foersteDag(aar).toNorwegianDateAtNoon()
                tom = sisteDag(aar).toNorwegianDateAtNoon()
                belop = faktiskAarligInntekt(aaretsInntektListe).toInt()
                bruk = true
                grunnlagKilde = GrunnlagKildeCti(GrunnlagKilde.BRUKER.name)
                inntektType = InntektTypeCti(InntektType.FPI.name)
            }

        // OpprettKravHodeHelper.createInntektsgrunnlagForBrukerOrEps
        private fun inntektsgrunnlagForSoekerOrEps(beloep: Int, fom: LocalDate?, tom: LocalDate?) =
            Inntektsgrunnlag().apply {
                this.belop = beloep
                this.bruk = true
                this.fom = fom?.toNorwegianDateAtNoon()
                this.grunnlagKilde = GrunnlagKildeCti(GrunnlagKilde.BRUKER.name)
                this.inntektType = InntektTypeCti(InntektType.FPI.name)
                this.tom = tom?.toNorwegianDateAtNoon()
            }

        private fun norskKravlinje(kravlinjeType: KravlinjeTypeEnum, person: PenPerson) =
            Kravlinje(kravlinjeTypeEnum = kravlinjeType, relatertPerson = person).apply {
                kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                land = LandkodeEnum.NOR
            }

        private fun opptjeningsgrunnlag(inntekt: Inntekt) =
            Opptjeningsgrunnlag().apply {
                ar = inntekt.inntektAar
                pi = inntekt.beloep.toInt()
                opptjeningType = OpptjeningTypeCti(OpptjeningType.PPI.name)
                bruk = true
            }

        private fun uttaksgradForHeltUttak(fom: LocalDate?) =
            Uttaksgrad().apply {
                fomDato = fom?.toNorwegianDateAtNoon()
                uttaksgrad = MAX_UTTAKSGRAD
            }

        private fun doesNotHaveFremtidigInntektBeforeFom(spec: SimuleringSpec, fom: LocalDate) =
            spec.fremtidigInntektListe.none { isBeforeByDay(it.fom, fom, true) }

        private fun foedselsdato(person: PenPerson?, spec: SimuleringSpec): LocalDate =
            person?.fodselsdato?.toNorwegianLocalDate() ?: spec.foedselDato ?: foersteDag(spec.foedselAar)

        private fun harUtenlandsopphold(antallAarUtenlands: Int?, trygdetidPeriodeListe: List<TTPeriode>) =
            if (antallAarUtenlands == null)
                containsTrygdetidUtenlands(trygdetidPeriodeListe)
            else
                antallAarUtenlands > 0

        private fun containsTrygdetidUtenlands(trygdetidPeriodeListe: List<TTPeriode>) =
            trygdetidPeriodeListe.any { it.land!!.kode != LandkodeEnum.NOR.name }

        private fun erGradertUttak(spec: SimuleringSpec) = spec.uttakGrad != UttakGradKode.P_100

        private fun sammeAar(a: FremtidigInntekt, b: FremtidigInntekt) =
            a.fom.year == b.fom.year

        private fun starterJanuar(inntekt: FremtidigInntekt) =
            inntekt.fom.monthValue == 1

        private fun starterAaretEtter(fremtidigInntekt: FremtidigInntekt, inntekt: FremtidigInntekt) =
            fremtidigInntekt.fom.year == aaretEtter(inntekt)

        private fun aaretEtter(inntekt: FremtidigInntekt) =
            inntekt.fom.year + 1

        private fun legacyFoersteDag(aar: Int) =
            foersteDag(aar).toNorwegianDateAtNoon()
    }
}
