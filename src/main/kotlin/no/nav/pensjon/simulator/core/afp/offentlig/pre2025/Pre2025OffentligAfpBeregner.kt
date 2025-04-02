package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpPersongrunnlag.Companion.persongrunnlagHavingRolle
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.domain.regler.to.SimuleringRequest
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.firstDayOfMonthAfterUserTurnsGivenAge
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByMonth
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.NorwegianCalendar
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.normalder.NormAlderService
import no.nav.pensjon.simulator.person.PersonService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// Corresponds to SimulerAFPogAPCommand (beregning part)
@Component
class Pre2025OffentligAfpBeregner(
    private val context: SimulatorContext,
    private val normAlderService: NormAlderService,
    private val personService: PersonService
) {
    private var beregnInstopphold = false // TODO this seems to be always false
    private var beregnForsorgingstillegg = false // TODO pass as parameter instead?
    private var ektefelleMottarPensjon = false // TODO pass as parameter instead?

    // SimulerAFPogAPCommand.beregnAfpOffentlig
    fun beregnAfp(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
        grunnbeloep: Int
    ): Pre2025OffentligAfpResult {
        val persongrunnlagListe: MutableList<Persongrunnlag> = copy(kravhode.persongrunnlagListe)

        val epsGrunnlag: Persongrunnlag? = persongrunnlagHavingRolle(
            persongrunnlagListe,
            GrunnlagsrolleEnum.EKTEF,
            GrunnlagsrolleEnum.PARTNER,
            GrunnlagsrolleEnum.SAMBO
        )

        epsGrunnlag?.let {
            addEpsInntektGrunnlag(
                eps = eps(spec, kravhode.regelverkTypeEnum, forrigeAlderspensjonBeregningResultat),
                inntektGrunnlagListe = it.inntektsgrunnlagListe,
                foersteUttakDato = spec.foersteUttakDato,
                grunnbeloep
            )
        }

        val soekerGrunnlag: Persongrunnlag? = persongrunnlagHavingRolle(persongrunnlagListe, GrunnlagsrolleEnum.SOKER)
        soekerGrunnlag?.inntektsgrunnlagListe?.add(inntektsgrunnlagOneManedBeforeUttak(spec))
        val normAlder: Alder = normAlderService.normAlder(spec.pid)

        val simuleringResultat: Simuleringsresultat =
            simulerPensjonsberegning(spec, persongrunnlagListe, normAlder)

        if (ikkeInnvilget(simuleringResultat.statusEnum)) {
            //log.info(
            //    "Simulering AFP offentlig - Ikke innvilget - Kode {} - Merknader {}",
            //    if (status == null) "(ingen)" else status.code, SimulerAFPogAPCommand.asCsv(simuleringsresultat.merknadListe)
            //)
            throw Pre2025OffentligAfpAvslaattException(simuleringResultat.merknadListe.joinToString(", ") { it.asString() })
        }

        populateKravhodeWithAfpHistorikk(kravhode, simuleringResultat.beregning, spec)
        return Pre2025OffentligAfpResult(simuleringResultat, kravhode)
    }

    private fun simulerPensjonsberegning(
        spec: SimuleringSpec,
        persongrunnlagListe: MutableList<Persongrunnlag>,
        normAlder: Alder
    ): Simuleringsresultat =
        simulerPensjonsberegning(simulering(spec, persongrunnlagListe), normAlder)

    // SimpleSimuleringService.simulerPensjonsberegning
    // -> SimulerPensjonsberegningCommand.execute
    private fun simulerPensjonsberegning(simulering: Simulering, normAlder: Alder): Simuleringsresultat {
        validateInput(simulering, normAlder)
        var simuleringAvslag: Boolean = simulerTrygdetid(simulering)

        if (simuleringAvslag) {
            return Simuleringsresultat().apply {
                statusEnum = VedtakResultatEnum.AVSL
                merknadListe.add(minsteTrygdetidMerknad())
            }
        }

        addUfoereHistorikk(simulering)
        createVedtakListe(simulering)
        var simuleringsresultat = Simuleringsresultat()

        if (SimuleringTypeEnum.AFP == simulering.simuleringTypeEnum) {
            setBeregnForsorgingstilleggAndEktefelleMottarPensjon(simulering)
            simuleringsresultat = simulerVilkarsprovAfp(simulering)
            simuleringsresultat.statusEnum?.let { simuleringAvslag = avslag(it) }
        }

        if (!simuleringAvslag) {
            simuleringsresultat = simulerPre2025OffentligAfp(simulering)
        }

        // Must be set explicitly, because pensjon-regler does not simulate "vilkår"
        if (simuleringsresultat.statusEnum == null) {
            simuleringsresultat.statusEnum = VedtakResultatEnum.INNV
        }

        return simuleringsresultat
    }

    // SimulerPensjonsberegningCommand.simulateVilkarsprvAFP
    // -> DefaultSimuleringConsumerService.simulerVilkarsprovAfp
    // -> SimulerVilkarsprovAfpConsumerCommand.execute
    private fun simulerVilkarsprovAfp(simulering: Simulering): Simuleringsresultat =
        try {
            context.simulerVilkarsprovPre2025OffentligAfp(SimuleringRequest(simulering, simulering.uttaksdato))
        } catch (e: KanIkkeBeregnesException) {
            throw FeilISimuleringsgrunnlagetException(e)
        } catch (e: RegelmotorValideringException) {
            throw KonsistensenIGrunnlagetErFeilException(e)
        }

    // SimulerPensjonsberegningCommand.simulateTrygdetid
    private fun simulerTrygdetid(simulering: Simulering): Boolean {
        var simuleringAvslag = false
        var dummyPersonId = 0L

        for (persongrunnlag in simulering.persongrunnlagListe) {
            persongrunnlag.penPerson?.let {
                if (it.penPersonId == 0L) { // NB: Never null in SIMDOM
                    it.penPersonId = ++dummyPersonId
                }
            }

            val trygdetid = Trygdetid().apply {
                tt = if (persongrunnlag.flyktning == true) MAX_TRYGDETID else trygdetid(persongrunnlag.antallArUtland)
            }

            persongrunnlag.trygdetider.add(trygdetid)
            // Ref. PEN PersonGrunnlagToReglerMapper.mapPersongrunnlagToRegler:
            persongrunnlag.trygdetid = persongrunnlag.latestTrygdetid()

            // Validate trygdetid to see if the simulation should be rejected:
            if (SimuleringTypeEnum.ALDER == simulering.simuleringTypeEnum) {
                if (persongrunnlag.personDetaljListe.any { GrunnlagsrolleEnum.SOKER == it.grunnlagsrolleEnum && trygdetid.tt < MIN_TRYGDETID }) {
                    simuleringAvslag = true
                    break
                }
            }
        }

        return simuleringAvslag
    }

    // SimulerPensjonsberegningCommand.addUforehistorikk
    private fun addUfoereHistorikk(simulering: Simulering) {
        val simuleringType = simulering.simuleringTypeEnum

        if (simuleringType == SimuleringTypeEnum.ALDER
            || simuleringType == SimuleringTypeEnum.ALDER_M_GJEN
            || simuleringType == SimuleringTypeEnum.AFP
        ) {
            val soekerGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(simulering.persongrunnlagListe, GrunnlagsrolleEnum.SOKER)
            val soekerUfoereHistorikk: Uforehistorikk? =
                fetchUforehistorikk(soekerGrunnlag?.penPerson, simulering.uttaksdato)

            if (soekerUfoereHistorikk?.containsActualUforeperiode() == true) {
                soekerGrunnlag?.uforeHistorikk = soekerUfoereHistorikk
            }
        }

        if (simuleringType == SimuleringTypeEnum.ALDER_M_GJEN) {
            val avdoedGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(simulering.persongrunnlagListe, GrunnlagsrolleEnum.AVDOD)

            val avdoedUfoereHistorikk: Uforehistorikk? =
                fetchUforehistorikk(avdoedGrunnlag?.penPerson, simulering.uttaksdato)

            if (avdoedUfoereHistorikk?.containsActualUforeperiode() == true) {
                avdoedGrunnlag?.uforeHistorikk = avdoedUfoereHistorikk
            }
        }

        if (simuleringType == SimuleringTypeEnum.BARN) {
            val morGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(simulering.persongrunnlagListe, GrunnlagsrolleEnum.MOR)

            if (morGrunnlag != null) {
                val morUforehistorikk: Uforehistorikk? =
                    fetchUforehistorikk(morGrunnlag.penPerson, simulering.uttaksdato)

                if (morUforehistorikk?.containsActualUforeperiode() == true) {
                    morGrunnlag.uforeHistorikk = morUforehistorikk
                }
            }

            val farGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(simulering.persongrunnlagListe, GrunnlagsrolleEnum.FAR)

            if (farGrunnlag != null) {
                val farUforehistorikk: Uforehistorikk? =
                    fetchUforehistorikk(farGrunnlag.penPerson, simulering.uttaksdato)

                if (farUforehistorikk?.containsActualUforeperiode() == true) {
                    farGrunnlag.uforeHistorikk = farUforehistorikk
                }
            }
        }
    }

    // SimulerPensjonsberegningCommand.getUforehistorikkForPenPerson
    private fun fetchUforehistorikk(penPerson: PenPerson?, virkningsdato: Date?): Uforehistorikk? {
        val soeker = penPerson?.pid?.let(personService::person)
        val ufoereHistorikk: Uforehistorikk = soeker?.uforehistorikk ?: return null

        // Creating copy to prevent hibernate from updating the actual object.
        // TODO hibernate issues not relevant in this context
        return Uforehistorikk(ufoereHistorikk).apply {
            uforeperiodeListe = uforeperiodeListe.filter { it.virk?.before(virkningsdato) == true }.toMutableList()
        }
    }

    // SimulerPensjonsberegningCommand.createVilkarsvedtakList
    private fun createVedtakListe(simulering: Simulering) {
        val innvilgetResultat = VedtakResultatEnum.INNV

        for (persongrunnlag in simulering.persongrunnlagListe) {
            val virkningFom: Date? = simulering.uttaksdato?.let { virkningFom(it).noon() }// TODO LocalDate

            val vedtak = VilkarsVedtak().apply {
                vilkarsvedtakResultatEnum = innvilgetResultat
                virkFom = virkningFom
                virkTom = null
                gjelderPerson = persongrunnlag.penPerson
                penPerson = persongrunnlag.penPerson // ref. PEN: VilkarsVedtakToReglerMapper.mapVilkarsVedtak
            }

            val grunnbeloep = virkningFom?.let {
                fetchGrunnbeloep(it.toNorwegianLocalDate()) // TODO cache?
            } ?: throw RuntimeException("Failed to obtain grunnbeløp - virkningFom null")

            persongrunnlag.personDetaljListe.forEach {
                updateVedtak(simulering, vedtak, persongrunnlag, it, grunnbeloep)
            }
        }
    }

    private fun fetchGrunnbeloep(date: LocalDate): Double =
        context.fetchGrunnbeloepListe(date).satsResultater.firstOrNull()?.verdi ?: 0.0

    // SimulerPensjonsberegningCommand.simulate
    // -> SimulerPensjonsberegningCommand.simulateCorrectTypeOfPensjon
    private fun simulerPre2025OffentligAfp(simulering: Simulering): Simuleringsresultat {
        setBeregnForsorgingstilleggAndEktefelleMottarPensjon(simulering) // TODO use function arguments instead?

        try {
            // SIMDOM: Only AFP (pre-2025 offentlig AFP) supported in this context
            if (SimuleringTypeEnum.AFP == simulering.simuleringTypeEnum) {
                return simulerPre2025OffentligAfp(
                    simulering,
                    beregnInstopphold,
                    beregnForsorgingstillegg,
                    ektefelleMottarPensjon
                )
            } else {
                throw RuntimeException("Unsupported simuleringtype in pre-2025 offentlig AFP context: ${simulering.simuleringTypeEnum}")
            }
        } catch (e: KanIkkeBeregnesException) {
            throw FeilISimuleringsgrunnlagetException(e)
        } catch (e: RegelmotorValideringException) {
            throw KonsistensenIGrunnlagetErFeilException(e)
        }
    }

    // DefaultSimuleringConsumerService.simulerAFP
    // -> SimulerPensjonsberegningConsumerCommand.execute with ytelse = SimulerPensjonsberegningConsumerCommand.AFP
    private fun simulerPre2025OffentligAfp(
        simulering: Simulering,
        beregnInstitusjonsopphold: Boolean,
        beregnForsoergingstillegg: Boolean,
        epsMottarPensjon: Boolean
    ): Simuleringsresultat =
        context.simulerPre2025OffentligAfp(
            SimuleringRequest(
                simulering = simulering,
                fom = simulering.uttaksdato,
                ektefelleMottarPensjon = epsMottarPensjon,
                beregnForsorgingstillegg = beregnForsoergingstillegg,
                beregnInstitusjonsopphold = beregnInstitusjonsopphold
            )
        )

    // SimulerPensjonsberegningCommand.setBeregnForsorgingstilleggAndEktefelleMottarPensjon
    private fun setBeregnForsorgingstilleggAndEktefelleMottarPensjon(simulering: Simulering) {
        simulering.vilkarsvedtakliste.forEach {
            val kravlinjeType = it.kravlinjeTypeEnum

            if (kravlinjeType == KravlinjeTypeEnum.ET || kravlinjeType == KravlinjeTypeEnum.BT) {
                beregnForsorgingstillegg = true
            }
        }

        ektefelleMottarPensjon = epsMottarPensjon(simulering.persongrunnlagListe)
    }

    companion object {
        const val AFP_VIRKNING_TOM_ALDER_AAR: Int = 67 // TODO use normalder?
        private const val AFP_MIN_AGE: Int = 62
        private const val MAX_TRYGDETID: Int = 40
        private const val MIN_TRYGDETID: Int = 3
        private const val GRUNNLAG_FOR_BEREGNING_AV_TRYGDETID: Int = 51
        private const val MINSTE_TRYGDETID: String = "MinsteTrygdetid"

        private fun populateKravhodeWithAfpHistorikk(
            kravhode: Kravhode,
            beregning: Beregning?,
            spec: SimuleringSpec
        ) {
            val persongrunnlag: Persongrunnlag = kravhode.hentPersongrunnlagForSoker()

            persongrunnlag.afpHistorikkListe = mutableListOf(AfpHistorikk().apply {
                // tp = tilleggspensjon, spt = sluttpoengtall, fpp = framtidige pensjonspoeng, pt = poengtall
                afpFpp = beregning?.tp?.spt?.poengrekke?.fpp?.pt
                    ?: 0.0 // SimulerAFPogAPCommand.getFppValueFromTilleggspensjonList + SimulerAFPogAPCommandHelper.checkValuesForNullAndReturnFpp
                afpOrdningEnum = spec.pre2025OffentligAfp?.afpOrdning?.name?.let(AFPtypeEnum::valueOf)
                afpPensjonsgrad = beregning?.afpPensjonsgrad ?: 0
                virkFom = spec.foersteUttakDato?.toNorwegianDateAtNoon()
                virkTom = persongrunnlag.fodselsdato?.let {
                    firstDayOfMonthAfterUserTurnsGivenAge(foedselsdato = it, alderAar = AFP_VIRKNING_TOM_ALDER_AAR)
                }
            })
        }

        // Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig
        private fun addEpsInntektGrunnlag(
            eps: Eps,
            inntektGrunnlagListe: MutableList<Inntektsgrunnlag>,
            foersteUttakDato: LocalDate?,
            grunnbeloep: Int
        ) {
            removeInntektsgrunnlagForventetArbeidsinntekt(inntektGrunnlagListe)

            // SimulerAFPogAPCommand.isEpsMottarPensjon
            if (eps.harPensjon) {
                inntektGrunnlagListe.add(
                    inntektsgrunnlag(
                        fom = foersteUttakDato?.toNorwegianDateAtNoon(),
                        type = InntekttypeEnum.PENF, // Pensjonsinntekt fra folketrygden
                        beloep = 1
                    )
                )
            }

            // SimulerAFPogAPCommand.isEpsInntektOver2g
            if (eps.harInntektOver2G) {
                inntektGrunnlagListe.add(
                    inntektsgrunnlag(
                        fom = foersteUttakDato?.toNorwegianDateAtNoon(),
                        type = InntekttypeEnum.FPI, // Forventet pensjongivende inntekt
                        beloep = 3 * grunnbeloep
                    )
                )
            }
        }

        // EktefelleMottarPensjonDecider.isEktefelleMottarPensjon
        private fun epsMottarPensjon(persongrunnlagListe: List<Persongrunnlag>): Boolean {
            val epsGrunnlag = persongrunnlagListe.firstOrNull { p -> p.personDetaljListe.any { it.isEps() } }

            return epsGrunnlag?.inntektsgrunnlagListe.orEmpty()
                .any { it.inntektTypeEnum == InntekttypeEnum.PENF && it.belop > 0 }
        }

        // SimulerAFPogAPCommand.createInntektsgrunnlag
        private fun inntektsgrunnlag(fom: Date?, type: InntekttypeEnum, beloep: Int) =
            Inntektsgrunnlag().apply {
                inntektTypeEnum = type
                this.fom = fom
                this.belop = beloep
                grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
                bruk = true
                //kopiertFraGammeltKrav = Boolean.FALSE
            }

        // Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig + .getSimuleringGrunnlagKilde + .getInntektMaanedenFoerUttakInntektstype
        private fun inntektsgrunnlagOneManedBeforeUttak(spec: SimuleringSpec) =
            Inntektsgrunnlag().apply {
                bruk = true
                inntektTypeEnum = InntekttypeEnum.IMFU // IMFU = Inntekt måneden før uttak
                fom = spec.foersteUttakDato?.toNorwegianDateAtNoon()?.let { getRelativeDateByMonth(it, -1) }
                belop = spec.pre2025OffentligAfp?.inntektMaanedenFoerAfpUttakBeloep ?: 0
                grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
            }

        // SimulerPensjonsberegningCommand.opprettEktefelleTillegg
        private fun opprettEktefelleTillegg(
            simulering: Simulering,
            epsGrunnlag: Persongrunnlag,
            grunnbeloep: Double
        ): Boolean {
            // Check if "søker" is applicable for "ektefelletillegg"
            val alderspensjonSimulering = SimuleringTypeEnum.ALDER == simulering.simuleringTypeEnum
            if (!alderspensjonSimulering) return false

            var opprettEktefelleTillegg = false
            var pensjonsinntektFraFolketrygden = 0
            var forventetPensjongivendeInntekt = 0

            for (inntektsgrunnlag in epsGrunnlag.inntektsgrunnlagListe) {
                val inntektType = inntektsgrunnlag.inntektTypeEnum

                if (InntekttypeEnum.PENF == inntektType) {
                    pensjonsinntektFraFolketrygden = inntektsgrunnlag.belop
                } else if (InntekttypeEnum.FPI == inntektType) {
                    forventetPensjongivendeInntekt = inntektsgrunnlag.belop
                }
            }

            if (pensjonsinntektFraFolketrygden <= 0 && forventetPensjongivendeInntekt <= grunnbeloep) {
                opprettEktefelleTillegg = true

                /*
                // If "tilknyttet person" is over 60, or cannot support him/herself, the søker should get ektefelletillegg
                if (!alderspensjonSimulering && persongrunnlag.over60ArKanIkkeForsorgesSelv == false) {
                    opprettEktefelleTillegg = false // <----- unreachable code, since !alderspensjonSimulering = false
                }
                */
            }

            return opprettEktefelleTillegg
        }

        // Extracted from SimulerPensjonsberegningCommand.simulateTrygdetid
        private fun trygdetid(utlandAntallAar: Int): Int {
            val trygdetid = GRUNNLAG_FOR_BEREGNING_AV_TRYGDETID - utlandAntallAar

            return when {
                trygdetid < 0 -> 0
                trygdetid > MAX_TRYGDETID -> MAX_TRYGDETID
                else -> trygdetid
            }
        }

        // SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun updateVedtak(
            simulering: Simulering,
            vedtak: VilkarsVedtak,
            persongrunnlag: Persongrunnlag,
            personDetalj: PersonDetalj,
            grunnbeloep: Double
        ) {
            // Based on the type of simulation and "grunnlagsrolle", set the type of "vilkårsvedtak" and find the "første virkningsdato"
            var virkningFom = vedtak.virkFom
            var kravlinjeType: KravlinjeTypeEnum? = null
            val rolle = personDetalj.grunnlagsrolleEnum
            val simuleringType: SimuleringTypeEnum? = simulering.simuleringTypeEnum

            if (GrunnlagsrolleEnum.SOKER == rolle) {
                kravlinjeType = simuleringType?.let(::soekerKravlinjeType)
            } else if (GrunnlagsrolleEnum.AVDOD == rolle) {
                kravlinjeType = simuleringType?.let(::avdoedKravlinjeType)
            } else if (GrunnlagsrolleEnum.BARN == rolle) {
                if (SimuleringTypeEnum.ALDER == simuleringType || SimuleringTypeEnum.ALDER_M_GJEN == simuleringType) {
                    if (personDetalj.barnDetalj?.inntektOver1G != true) {
                        kravlinjeType = KravlinjeTypeEnum.BT // Barnetillegg
                        virkningFom = vedtak.virkFom
                    }
                }
            } else if (gjelderEps(rolle?.name, personDetalj)) {
                if (opprettEktefelleTillegg(simulering, persongrunnlag, grunnbeloep)) {
                    kravlinjeType = KravlinjeTypeEnum.ET // Ektefelletillegg
                    virkningFom = vedtak.virkFom
                }
            }

            // Add the vilkårsvedtak to the simulation:
            kravlinjeType?.let {
                vedtak.kravlinje = Kravlinje().apply {
                    this.kravlinjeTypeEnum = it
                    this.hovedKravlinje = it.erHovedkravlinje
                    this.relatertPerson = persongrunnlag.penPerson
                }
                vedtak.kravlinjeTypeEnum = it
                vedtak.forsteVirk = virkningFom
                simulering.vilkarsvedtakliste.add(vedtak)
            }
        }

        // Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig + .getAfpSimuleringsType
        private fun simulering(spec: SimuleringSpec, persongrunnlagListe: MutableList<Persongrunnlag>) =
            Simulering().apply {
                simuleringTypeEnum = SimuleringTypeEnum.AFP
                uttaksdato = spec.foersteUttakDato?.toNorwegianDateAtNoon()
                afpOrdningEnum = spec.pre2025OffentligAfp?.afpOrdning?.name?.let(AFPtypeEnum::valueOf)
                this.persongrunnlagListe = persongrunnlagListe
            }

        // SimulerPensjonsberegningCommand.validateInput
        private fun validateInput(simulering: Simulering, normAlder: Alder) {
            val simuleringType =
                simulering.simuleringTypeEnum ?: throw ImplementationUnrecoverableException("simulering.simuleringType")

            if (simulering.uttaksdato == null) {
                throw ImplementationUnrecoverableException("simulering.uttaksdato")
            }

            if (SimuleringTypeEnum.AFP == simuleringType && simulering.afpOrdningEnum == null) {
                throw ImplementationUnrecoverableException("simulering.afpordning")
            }

            if (simulering.persongrunnlagListe.isEmpty()) {
                throw ImplementationUnrecoverableException("simulering.persongrunnlagListe")
            }

            if (simulering.persongrunnlagListe.any { it.personDetaljListe.isEmpty() }) {
                throw ImplementationUnrecoverableException("simulering.persongrunnlagListe.persondetaljliste")
            }

            val soekerGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(simulering.persongrunnlagListe, GrunnlagsrolleEnum.SOKER)

            val soekerFoedselsdato: Calendar = NorwegianCalendar.forNoon(soekerGrunnlag!!.fodselsdato!!)
            val uttakDato: Calendar = NorwegianCalendar.forNoon(simulering.uttaksdato!!)
            val foedselMaaned = soekerFoedselsdato[Calendar.MONTH]
            val uttakMaaned = uttakDato[Calendar.MONTH]
            val alder = uttakDato[Calendar.YEAR] - soekerFoedselsdato[Calendar.YEAR]

            if (SimuleringTypeEnum.ALDER == simuleringType) {
                if (alder < normAlder.aar || alder == normAlder.aar && uttakMaaned <= foedselMaaned) {
                    throw PersonForUngException("Alderspensjon;${normAlder.aar};0")
                }
            }

            if (SimuleringTypeEnum.AFP == simuleringType) {
                if (alder < AFP_MIN_AGE || alder == AFP_MIN_AGE && uttakMaaned <= foedselMaaned) {
                    throw PersonForUngException("AFP;$AFP_MIN_AGE;0")
                }
            }
        }

        // SimulerPensjonsberegningCommand.findPersongrunnlagWithGivenRole
        private fun findPersongrunnlagHavingRolle(
            persongrunnlagListe: List<Persongrunnlag>,
            rolle: GrunnlagsrolleEnum
        ): Persongrunnlag? =
            persongrunnlagListe.firstOrNull { hasRolle(it, rolle) }

        // Extracted from SimulerPensjonsberegningCommand.findPersongrunnlagWithGivenRole
        private fun hasRolle(persongrunnlag: Persongrunnlag, rolle: GrunnlagsrolleEnum) =
            persongrunnlag.personDetaljListe.any { rolle == it.grunnlagsrolleEnum }

        // Extracted from SimulerPensjonsberegningCommand.execute
        private fun avslag(status: VedtakResultatEnum) =
            VedtakResultatEnum.AVSL == status || VedtakResultatEnum.VETIKKE == status

        // Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig
        private fun ikkeInnvilget(status: VedtakResultatEnum?) =
            status == null || VedtakResultatEnum.INNV != status

        // Extracted from SimulerPensjonsberegningCommand.execute
        private fun minsteTrygdetidMerknad() = Merknad().apply { kode = MINSTE_TRYGDETID }

        // SimulerAFPogAPCommandHelper.copyPersongrunnlagList
        private fun copy(persongrunnlagListe: List<Persongrunnlag>): MutableList<Persongrunnlag> =
            persongrunnlagListe.map {
                Persongrunnlag(
                    source = it,
                    excludeForsteVirkningsdatoGrunnlag = true
                )
            }.toMutableList()

        // SimulerAFPogAPCommand.findBeregningsInformasjonFromForrigeBerresAp
        private fun beregningInfoFraForrigeAlderspensjonBeregningResultat(
            regelverkType: RegelverkTypeEnum?,
            beregningResultat: AbstraktBeregningsResultat?
        ): BeregningsInformasjon? {
            if (beregningResultat == null) return null

            return when (regelverkType) {
                RegelverkTypeEnum.N_REG_G_OPPTJ ->
                    (beregningResultat as BeregningsResultatAlderspensjon2011).beregningsInformasjonKapittel19

                RegelverkTypeEnum.N_REG_G_N_OPPTJ ->
                    ((beregningResultat as BeregningsResultatAlderspensjon2016).beregningsResultat2011)?.beregningsInformasjonKapittel19

                else ->
                    (beregningResultat as BeregningsResultatAlderspensjon2025).beregningsInformasjonKapittel20
            }
        }

        // Extra
        private fun eps(regelverkType: RegelverkTypeEnum?, beregningResultat: AbstraktBeregningsResultat?): Eps? =
            beregningInfoFraForrigeAlderspensjonBeregningResultat(regelverkType, beregningResultat)?.let {
                Eps(harInntektOver2G = it.epsOver2G, harPensjon = it.epsMottarPensjon)
            }

        // Extra
        private fun eps(
            spec: SimuleringSpec,
            regelverkType: RegelverkTypeEnum?,
            beregningResultat: AbstraktBeregningsResultat?
        ): Eps {
            if (spec.epsHarInntektOver2G && spec.epsHarPensjon)
                return Eps(harInntektOver2G = true, harPensjon = true)

            val eps: Eps? = eps(regelverkType, beregningResultat)

            return Eps(
                harInntektOver2G = spec.epsHarInntektOver2G || eps?.harInntektOver2G == true,
                harPensjon = spec.epsHarPensjon || eps?.harPensjon == true
            )
        }

        // SimulerAFPogAPCommand.removeInntektsgrunnlagForventetArbeidsinntektFromList
        private fun removeInntektsgrunnlagForventetArbeidsinntekt(inntektGrunnlagListe: MutableList<Inntektsgrunnlag>) {
            inntektGrunnlagListe.removeIf {
                it.inntektTypeEnum == InntekttypeEnum.FPI
            }
        }

        // Extracted from SimulerPensjonsberegningCommand.createVilkarsvedtakList
        private fun virkningFom(uttakDato: Date): Date =
            NorwegianCalendar.forDate(uttakDato).apply {
                this[Calendar.DAY_OF_MONTH] = 1
            }.time

        // Extracted from SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun gjelderEps(rolle: String?, personDetalj: PersonDetalj) =
            //TODO use rolle enum
            (GrunnlagsrolleEnum.EKTEF.name == rolle
                    || GrunnlagsrolleEnum.PARTNER.name == rolle
                    || GrunnlagsrolleEnum.SAMBO.name == rolle && (BorMedTypeEnum.SAMBOER1_5 == personDetalj.borMedEnum))

        // Extracted from SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun soekerKravlinjeType(simuleringType: SimuleringTypeEnum): KravlinjeTypeEnum? =
            when (simuleringType) {
                SimuleringTypeEnum.ALDER -> KravlinjeTypeEnum.AP
                SimuleringTypeEnum.ALDER_M_GJEN -> KravlinjeTypeEnum.AP
                SimuleringTypeEnum.AFP -> KravlinjeTypeEnum.AFP
                SimuleringTypeEnum.GJENLEVENDE -> KravlinjeTypeEnum.GJP
                SimuleringTypeEnum.BARN -> KravlinjeTypeEnum.BP
                else -> null
            }

        // Extracted from SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun avdoedKravlinjeType(simuleringType: SimuleringTypeEnum): KravlinjeTypeEnum? =
            when (simuleringType) {
                SimuleringTypeEnum.ALDER_M_GJEN -> KravlinjeTypeEnum.GJR
                else -> null
            }

        private data class Eps(
            val harInntektOver2G: Boolean,
            val harPensjon: Boolean
        )
    }
}
