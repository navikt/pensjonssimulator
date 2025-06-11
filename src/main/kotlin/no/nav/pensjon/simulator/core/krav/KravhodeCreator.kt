package no.nav.pensjon.simulator.core.krav

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpPersongrunnlag
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpUttaksgrad
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdater
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.endring.EndringPersongrunnlag
import no.nav.pensjon.simulator.core.endring.EndringUttaksgrad
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.exception.PersonForGammelException
import no.nav.pensjon.simulator.core.inntekt.InntektUtil.faktiskAarligInntekt
import no.nav.pensjon.simulator.core.inntekt.OpptjeningUpdater
import no.nav.pensjon.simulator.core.krav.KravUtil.utlandMaanederFraAarStartTilFoersteUttakDato
import no.nav.pensjon.simulator.core.krav.KravUtil.utlandMaanederInnenforAaret
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isFirstDayOfMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.monthOfYearRange1To12
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.person.PersongrunnlagService
import no.nav.pensjon.simulator.core.person.eps.EpsService
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.spec.UttakValidator.validateGradertUttak
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findValidForYear
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDag
import no.nav.pensjon.simulator.tech.time.DateUtil.sisteDag
import no.nav.pensjon.simulator.tech.time.Time
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
    private val beholdningUpdater: BeholdningUpdater,
    private val epsService: EpsService,
    private val persongrunnlagService: PersongrunnlagService,
    private val opptjeningUpdater: OpptjeningUpdater,
    private val generelleDataHolder: GenerelleDataHolder,
    private val kravService: KravService,
    private val ufoereService: UfoeretrygdUtbetalingService,
    private val endringPersongrunnlag: EndringPersongrunnlag,
    private val endringUttaksgrad: EndringUttaksgrad,
    private val pre2025OffentligAfpPersongrunnlag: Pre2025OffentligAfpPersongrunnlag,
    private val pre2025OffentligAfpUttaksgrad: Pre2025OffentligAfpUttaksgrad,
    private val time: Time
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
            kravFremsattDato = time.today().toNorwegianDateAtNoon()
            onsketVirkningsdato = oensketVirkningDato(spec)
            gjelder = null
            sakId = null
            sakType = SakTypeEnum.ALDER
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

                gjelderEndring -> endringUttaksgrad.uttaksgradListe(
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
        person?.foedselsdato?.year ?: spec.foedselAar

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
            fodselsdato = spec.foedselDato?.toNorwegianDateAtNoon()
            antallArUtland = spec.utlandAntallAar
            statsborgerskapEnum = norge
            flyktning = false
            bosattLandEnum = norge
            personDetaljListe = mutableListOf(anonymPersondetalj(spec))
            inngangOgEksportGrunnlag = InngangOgEksportGrunnlag().apply { fortsattMedlemFT = true }
            sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
        }.also {
            it.finishInit()
        }

    // SimulerFleksibelAPCommand.createPersonDetaljerForenkletSimulering
    private fun anonymPersondetalj(spec: SimuleringSpec) =
        PersonDetalj().apply {
            grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
            grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
            penRolleFom = spec.foedselDato?.toNorwegianDateAtNoon()
            sivilstandTypeEnum = anonymSivilstand(spec.sivilstatus)
            bruk = true
        }.also {
            it.finishInit()
        }

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
        val inntektListe: MutableList<Inntekt>

        if (spec.brukFremtidigInntekt) {
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
            inntektListe = aarligeInntekterFraDagensDato(spec, grunnbeloep, person?.foedselsdato)
            persongrunnlag.inntektsgrunnlagListe =
                opprettInntektGrunnlagForSoeker(spec, persongrunnlag.inntektsgrunnlagListe)
        }

        persongrunnlag.opptjeningsgrunnlagListe =
            opptjeningUpdater.oppdaterOpptjeningsgrunnlagFraInntekter(
                originalGrunnlagListe = persongrunnlag.opptjeningsgrunnlagListe,
                inntektListe,
                foedselsdato = persongrunnlag.fodselsdato?.toNorwegianLocalDate()
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

    // PEN: OpprettKravHodeHelper.finnListeOverInntektPerArFraDagensDato
    private fun aarligeInntekterFraDagensDato(
        spec: SimuleringSpec,
        grunnbeloep: Int,
        foedselsdato: LocalDate? // null if anonym
    ): MutableList<Inntekt> {
        var veietGrunnbeloepListe: List<VeietSatsResultat> = emptyList()
        val innevaerendeAar = time.today().year
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

        // NB: In PEN uttakGrad (utg) is null, and null is not interpreted as 100 % (used for AFP_ETTERF_ALDER)
        // Ref. PEN SimuleringEtter2011.isUttaksgrad100
        val inntektUnderAfpEllerGradertUttak =
            spec.pre2025OffentligAfp?.inntektUnderAfpUttakBeloep
                ?: if (spec.uttakErGradertEllerNull()) spec.inntektUnderGradertUttakBeloep else 0

        val inntektEtterHeltUttak = spec.inntektEtterHeltUttakBeloep
        val inntekter: MutableList<Inntekt> = mutableListOf()

        for (aar in gjeldendeAar..aarSoekerBlirMaxAlder) {
            val beregnetForventetInntekt =
                (forventetInntekt * forventetInntektAntallMaaneder(aar, spec) / MAANEDER_PER_AAR).toLong()
            val beregnetInntektUnderAfpEllerGradertUttak = (inntektUnderAfpEllerGradertUttak *
                    antallMaanederMedInntektUnderAfpEllerGradertUttak(aar, spec) / MAANEDER_PER_AAR).toLong()
            val beregnetInntektEtterHeltUttak = (inntektEtterHeltUttak *
                    antallMaanederMedInntektEtterHeltUttak(aar, spec) / MAANEDER_PER_AAR).toLong()
            val forhold = calculateGrunnbeloepForhold(aar, spec, veietGrunnbeloepListe, grunnbeloep)

            inntekter.add(
                Inntekt(
                    inntektAar = aar,
                    beloep = Math.round(forhold * (beregnetForventetInntekt + beregnetInntektUnderAfpEllerGradertUttak + beregnetInntektEtterHeltUttak))
                )
            )
        }

        return inntekter
    }

    // PEN: antallMndMedInntektUnderGradertUttak
    private fun antallMaanederMedInntektUnderAfpEllerGradertUttak(aar: Int, spec: SimuleringSpec): Int {
        val foersteUttak =
            KalenderMaaned(
                aarstall = spec.foersteUttakDato!!.year,
                maaned = monthOfYearRange1To12(spec.foersteUttakDato)
            )

        val heltUttak = heltUttakTidspunkt(spec) ?: foersteUttak

        if (aar == foersteUttak.aarstall) {
            return if (foersteUttak.aarstall != heltUttak.aarstall) {
                val antallMaanederMedForventetInntekt = foersteUttak.maaned - 1
                MAANEDER_PER_AAR - antallMaanederMedForventetInntekt
            } else {
                heltUttak.maaned - foersteUttak.maaned
            }
        }

        if (aar in (foersteUttak.aarstall + 1) until heltUttak.aarstall) {
            return MAANEDER_PER_AAR
        }

        if (aar == heltUttak.aarstall) {
            return heltUttak.maaned - 1
        }

        return 0
    }

    // PEN: antallMndMedInntektEtterHeltUttak
    private fun antallMaanederMedInntektEtterHeltUttak(aar: Int, spec: SimuleringSpec): Int {
        val foersteUttak =
            KalenderMaaned(
                aarstall = spec.foersteUttakDato!!.year,
                maaned = monthOfYearRange1To12(spec.foersteUttakDato)
            )

        val heltUttak = heltUttakTidspunkt(spec) ?: foersteUttak

        if (aar == heltUttak.aarstall) {
            val antallMaanederMedInntektUnderGradertUttak = heltUttak.maaned - 1
            return MAANEDER_PER_AAR - antallMaanederMedInntektUnderGradertUttak
        }

        val antallAarInntektEtterHeltUttak: Int = spec.inntektEtterHeltUttakAntallAar ?: 0

        if (heltUttak.aarstall < aar && aar < heltUttak.aarstall + antallAarInntektEtterHeltUttak) {
            return MAANEDER_PER_AAR
        }

        if (aar - heltUttak.aarstall == antallAarInntektEtterHeltUttak) {
            return heltUttak.maaned - 1
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

    private fun calculateGrunnbeloepForhold(
        aar: Int,
        spec: SimuleringSpec,
        veietGrunnbeloepListe: List<VeietSatsResultat>,
        grunnbeloep: Int
    ): Double {
        val innevaerendeAar = time.today().year

        return if (spec.erAnonym && aar < innevaerendeAar)
            findValidForYear(veietGrunnbeloepListe, aar)?.let { it.verdi / grunnbeloep } ?: 1.0
        else
            1.0
    }

    // PEN: OpprettKravHodeHelper.opprettInntektsgrunnlagForBruker
    private fun opprettInntektGrunnlagForSoeker(
        spec: SimuleringSpec,
        existingInntektsgrunnlagList: MutableList<Inntektsgrunnlag>
    ): MutableList<Inntektsgrunnlag> {
        val inntektsgrunnlagListe: MutableList<Inntektsgrunnlag> = mutableListOf()

        // Inntekt fram til første uttak:

        if (spec.foersteUttakDato!!.isAfter(time.today()) && spec.forventetInntektBeloep > 0) {
            inntektsgrunnlagListe.add(
                inntektsgrunnlagForSoekerOrEps(
                    beloep = spec.forventetInntektBeloep,
                    fom = time.today(),
                    tom = getRelativeDateByDays(spec.foersteUttakDato, -1)
                )
            )
        }

        // Inntekt mellom første og andre uttak:

        val inntektUnderAfpEllerGradertUttak: Int =
            spec.pre2025OffentligAfp?.inntektUnderAfpUttakBeloep ?: spec.inntektUnderGradertUttakBeloep

        val gjelder2FaseSimulering: Boolean = spec.gjelder2FaseSimulering()

        if (gjelder2FaseSimulering && inntektUnderAfpEllerGradertUttak > 0) {
            inntektsgrunnlagListe.add(
                inntektsgrunnlagForSoekerOrEps(
                    beloep = inntektUnderAfpEllerGradertUttak,
                    fom = spec.foersteUttakDato,
                    tom = getRelativeDateByDays(spec.heltUttakDato!!, -1)
                )
            )
        }

        // Inntekt etter start av helt uttak:

        val inntektEtterHeltUttakAntallAar: Int = spec.inntektEtterHeltUttakAntallAar ?: 0

        if (spec.inntektEtterHeltUttakBeloep > 0 && inntektEtterHeltUttakAntallAar > 0) {
            val fom = if (gjelder2FaseSimulering) spec.heltUttakDato else spec.foersteUttakDato
            val tom = getRelativeDateByDays(getRelativeDateByYear(fom!!, inntektEtterHeltUttakAntallAar), -1)
            inntektsgrunnlagListe.add(inntektsgrunnlagForSoekerOrEps(spec.inntektEtterHeltUttakBeloep, fom, tom))
        }

        inntektsgrunnlagListe.addAll(existingInntektsgrunnlagList.filter {
            it.bruk == true && !isForventetPensjongivendeInntekt(it)
        })

        return inntektsgrunnlagListe
    }

    private companion object {
        private const val MAX_ALDER = 80 // normert?
        private const val MAX_OPPTJENING_ALDER = 75 // normert?
        private const val MAX_UTTAKSGRAD = 100
        private const val ANONYM_PERSON_ID = -1L
        private val norge = LandkodeEnum.NOR
        private val log = KotlinLogging.logger {}

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
            val uttaksgradListe = mutableListOf(angittUttaksgrad(spec))

            if (spec.uttakErGradertEllerNull()) {
                uttaksgradListe.add(uttaksgradForHeltUttak(spec.heltUttakDato))
            }

            return uttaksgradListe
        }

        // SimulerFleksibelAPCommand.createUttaksgradChosenByUser
        private fun angittUttaksgrad(spec: SimuleringSpec) =
            Uttaksgrad().apply {
                fomDato = spec.foersteUttakDato?.toNorwegianDateAtNoon()
                uttaksgrad = spec.uttakGrad.value.toInt()

                if (spec.uttakErGradertEllerNull()) {
                    validateGradertUttak(spec)
                    tomDato = spec.heltUttakDato!!.minusDays(1).toNorwegianDateAtNoon()
                }
            }

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
            grunnlagListe.filter { it.bruk == true && InntekttypeEnum.FPI != it.inntektTypeEnum }

        private fun isForventetPensjongivendeInntekt(grunnlag: Inntektsgrunnlag): Boolean =
            grunnlag.inntektTypeEnum == InntekttypeEnum.FPI

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

        // PEN: OpprettKravHodeHelper.createInntektsgrunnlagFromFremtidigInntektList
        private fun inntektsgrunnlagListeFraFremtidigeInntekter(
            spec: SimuleringSpec,
            gjeldendeAar: Int,
            sisteOpptjeningAar: Int,
            fom: LocalDate
        ): List<Inntektsgrunnlag> {
            val fremtidigInntektListe = spec.fremtidigInntektListe

            // NB: The original fremtidigInntektListe is here modified (as is done in PEN):
            if (fremtidigInntektListe.isEmpty() || doesNotHaveFremtidigInntektBeforeFom(spec, fom)) {
                fremtidigInntektListe.add(FremtidigInntekt(aarligInntektBeloep = 0, fom))
            }

            // NB: In PEN OpprettKravHodeHelper.sortFremtidigInntektList a new list is created
            // We achieve the same here by using toMutableList() on the original list
            // TODO check if toMutableList creates a new list
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
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
                inntektTypeEnum = InntekttypeEnum.FPI
            }

        // OpprettKravHodeHelper.createInntektsgrunnlagForBrukerOrEps
        private fun inntektsgrunnlagForSoekerOrEps(beloep: Int, fom: LocalDate?, tom: LocalDate?) =
            Inntektsgrunnlag().apply {
                this.belop = beloep
                this.bruk = true
                this.fom = fom?.toNorwegianDateAtNoon()
                this.grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
                this.inntektTypeEnum = InntekttypeEnum.FPI
                this.tom = tom?.toNorwegianDateAtNoon()
            }

        private fun norskKravlinje(kravlinjeType: KravlinjeTypeEnum, person: PenPerson) =
            Kravlinje().apply {
                kravlinjeTypeEnum = kravlinjeType
                hovedKravlinje = kravlinjeType.erHovedkravlinje
                relatertPerson = person
                hovedKravlinje = kravlinjeType.erHovedkravlinje
                kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                land = LandkodeEnum.NOR
            }

        private fun uttaksgradForHeltUttak(fom: LocalDate?) =
            Uttaksgrad().apply {
                fomDato = fom?.toNorwegianDateAtNoon()
                uttaksgrad = MAX_UTTAKSGRAD
            }

        private fun doesNotHaveFremtidigInntektBeforeFom(spec: SimuleringSpec, fom: LocalDate) =
            spec.fremtidigInntektListe.none { isBeforeByDay(it.fom, fom, true) }

        private fun foedselsdato(person: PenPerson?, spec: SimuleringSpec): LocalDate =
            person?.foedselsdato ?: spec.foedselDato ?: foersteDag(spec.foedselAar)

        private fun harUtenlandsopphold(antallAarUtenlands: Int?, trygdetidPeriodeListe: List<TTPeriode>) =
            if (antallAarUtenlands == null)
                containsTrygdetidUtenlands(trygdetidPeriodeListe)
            else
                antallAarUtenlands > 0

        private fun containsTrygdetidUtenlands(trygdetidPeriodeListe: List<TTPeriode>) =
            trygdetidPeriodeListe.any { it.landEnum != LandkodeEnum.NOR }

        private fun heltUttakTidspunkt(spec: SimuleringSpec): KalenderMaaned? =
            if (spec.gjelder2FaseSimulering())
                spec.heltUttakDato?.let {
                    KalenderMaaned(
                        aarstall = it.year,
                        maaned = monthOfYearRange1To12(it)
                    )
                } ?: handleMissingHeltUttakDato(spec)
            else
                null

        private fun sammeAar(a: FremtidigInntekt, b: FremtidigInntekt) =
            a.fom.year == b.fom.year

        private fun starterJanuar(inntekt: FremtidigInntekt) =
            inntekt.fom.monthValue == 1

        private fun starterAaretEtter(fremtidigInntekt: FremtidigInntekt, inntekt: FremtidigInntekt) =
            fremtidigInntekt.fom.year == aaretEtter(inntekt)

        private fun aaretEtter(inntekt: FremtidigInntekt) =
            inntekt.fom.year + 1

        private fun handleMissingHeltUttakDato(spec: SimuleringSpec): Nothing =
            "Manglende heltUttakDato for 2-fase-simulering".let {
                log.warn { "$it - $spec" }
                throw BadSpecException(it)
            }
    }

    private data class KalenderMaaned(
        val aarstall: Int,
        val maaned: Int // 1 t.o.m. 12
    )
}
