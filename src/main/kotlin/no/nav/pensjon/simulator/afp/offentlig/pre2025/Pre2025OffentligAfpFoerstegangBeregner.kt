package no.nav.pensjon.simulator.afp.offentlig.pre2025

import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpPersongrunnlag.Companion.persongrunnlagHavingRolle
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpSpecValidator.validateInput
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.Merknad
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
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.ufoere.UfoereService
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.NorwegianCalendar
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

/**
 * Beregner førstegangsuttak av offentlig AFP i henhold til reglene som gjaldt før 2025 ("gammel offentlig AFP").
 */
// Corresponds to SimulerAFPogAPCommand (beregning part) in PEN
@Component
class Pre2025OffentligAfpFoerstegangBeregner(
    private val context: SimulatorContext,
    private val normalderService: NormertPensjonsalderService,
    private val ufoereService: UfoereService,
    private val grunnbeloepService: GrunnbeloepService
) {
    private var beregnInstopphold = false // TODO this seems to be always false
    private var beregnForsorgingstillegg = false // TODO pass as parameter instead?
    private var ektefelleMottarPensjon = false // TODO pass as parameter instead?

    // PEN: SimulerAFPogAPCommand.beregnAfpOffentlig
    fun beregnAfp(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?
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
                foersteUttakDato = spec.foersteUttakDato
            )
        }

        persongrunnlagHavingRolle(persongrunnlagListe, GrunnlagsrolleEnum.SOKER)?.apply {
            inntektsgrunnlagListe.add(inntektsgrunnlagMaanedenFoerUttak(spec))
        }

        val normalder: Alder = normalderService.normalder(spec.pid!!)
        val simuleringResultat: Simuleringsresultat = simulerPensjonsberegning(spec, persongrunnlagListe, normalder)

        if (ikkeInnvilget(simuleringResultat.statusEnum)) {
            throw Pre2025OffentligAfpAvslaattException(simuleringResultat.merknadListe.joinToString(", ") { it.asString() })
        }

        populateKravhodeWithAfpHistorikk(kravhode, simuleringResultat.beregning, spec)
        return Pre2025OffentligAfpResult(simuleringResultat, kravhode)
    }

    private fun simulerPensjonsberegning(
        spec: SimuleringSpec,
        persongrunnlagListe: MutableList<Persongrunnlag>,
        normalder: Alder
    ): Simuleringsresultat =
        simulerPensjonsberegning(afpSpec(spec, persongrunnlagListe), normalder)

    // PEN: SimpleSimuleringService.simulerPensjonsberegning -> SimulerPensjonsberegningCommand.execute
    private fun simulerPensjonsberegning(spec: Simulering, normalder: Alder): Simuleringsresultat {
        validateInput(spec, normalder)
        var simuleringAvslag: Boolean = simulerTrygdetid(spec)

        if (simuleringAvslag) {
            return Simuleringsresultat().apply {
                statusEnum = VedtakResultatEnum.AVSL
                merknadListe.add(minsteTrygdetidMerknad())
            }
        }

        addUfoereHistorikk(spec)
        createVedtakListe(spec)
        var simuleringsresultat = Simuleringsresultat()

        if (SimuleringTypeEnum.AFP == spec.simuleringTypeEnum) {
            setBeregnForsoergingstilleggAndEktefelleMottarPensjon(spec)
            simuleringsresultat = simulerVilkarsprovAfp(spec)
            simuleringsresultat.statusEnum?.let { simuleringAvslag = avslag(it) }
        }

        if (!simuleringAvslag) {
            simuleringsresultat = simulerPre2025OffentligAfp(spec)
        }

        // Must be set explicitly, because pensjon-regler does not simulate "vilkår"
        if (simuleringsresultat.statusEnum == null) {
            simuleringsresultat.statusEnum = VedtakResultatEnum.INNV
        }

        return simuleringsresultat
    }

    // PEN: SimulerPensjonsberegningCommand.simulateVilkarsprvAFP
    //   -> DefaultSimuleringConsumerService.simulerVilkarsprovAfp
    //   -> SimulerVilkarsprovAfpConsumerCommand.execute
    private fun simulerVilkarsprovAfp(spec: Simulering): Simuleringsresultat =
        try {
            context.simulerVilkarsprovPre2025OffentligAfp(SimuleringRequest(spec, spec.uttaksdato))
        } catch (e: KanIkkeBeregnesException) {
            throw FeilISimuleringsgrunnlagetException(e)
        } catch (e: RegelmotorValideringException) {
            throw KonsistensenIGrunnlagetErFeilException(e)
        }

    // PEN: SimulerPensjonsberegningCommand.simulateTrygdetid
    private fun simulerTrygdetid(spec: Simulering): Boolean {
        var simuleringAvslag = false
        var dummyPersonId = 0L

        for (persongrunnlag in spec.persongrunnlagListe) {
            persongrunnlag.penPerson?.let {
                if (it.penPersonId == 0L) {
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
            if (SimuleringTypeEnum.ALDER == spec.simuleringTypeEnum) {
                if (persongrunnlag.personDetaljListe.any { GrunnlagsrolleEnum.SOKER == it.grunnlagsrolleEnum && trygdetid.tt < MIN_TRYGDETID }) {
                    simuleringAvslag = true
                    break
                }
            }
        }

        return simuleringAvslag
    }

    private fun ufoerehistorikk(persongrunnlag: Persongrunnlag, spec: Simulering): Uforehistorikk? =
        persongrunnlag.penPerson?.pid?.let {
            ufoereService.ufoerehistorikk(
                pid = it,
                uttakDato = spec.uttaksdato!!.toNorwegianLocalDate()
            )
        }

    // PEN: SimulerPensjonsberegningCommand.addUforehistorikk
    private fun addUfoereHistorikk(spec: Simulering) {
        val simuleringType = spec.simuleringTypeEnum

        if (simuleringType == SimuleringTypeEnum.ALDER
            || simuleringType == SimuleringTypeEnum.ALDER_M_GJEN
            || simuleringType == SimuleringTypeEnum.AFP
        ) {
            val soekerGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(spec.persongrunnlagListe, GrunnlagsrolleEnum.SOKER)

            soekerGrunnlag?.let {
                val historikk: Uforehistorikk? = ufoerehistorikk(persongrunnlag = it, spec)

                if (historikk?.containsActualUforeperiode() == true) {
                    it.uforeHistorikk = historikk
                }
            }
        }

        if (simuleringType == SimuleringTypeEnum.ALDER_M_GJEN) {
            val avdoedGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(spec.persongrunnlagListe, GrunnlagsrolleEnum.AVDOD)

            avdoedGrunnlag?.let {
                val historikk: Uforehistorikk? = ufoerehistorikk(persongrunnlag = it, spec)

                if (historikk?.containsActualUforeperiode() == true) {
                    it.uforeHistorikk = historikk
                }
            }
        }

        if (simuleringType == SimuleringTypeEnum.BARN) {
            val morGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(spec.persongrunnlagListe, GrunnlagsrolleEnum.MOR)

            morGrunnlag?.let {
                val historikk: Uforehistorikk? = ufoerehistorikk(persongrunnlag = it, spec)

                if (historikk?.containsActualUforeperiode() == true) {
                    it.uforeHistorikk = historikk
                }
            }

            val farGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(spec.persongrunnlagListe, GrunnlagsrolleEnum.FAR)

            farGrunnlag?.let {
                val historikk: Uforehistorikk? = ufoerehistorikk(persongrunnlag = it, spec)

                if (historikk?.containsActualUforeperiode() == true) {
                    it.uforeHistorikk = historikk
                }
            }
        }
    }

    // PEN: SimulerPensjonsberegningCommand.createVilkarsvedtakList
    private fun createVedtakListe(spec: Simulering) {
        val innvilgetResultat = VedtakResultatEnum.INNV
        val virkningFom: Date? = spec.uttaksdato?.let { virkningFom(uttakDato = it).noon() }// TODO LocalDate

        val grunnbeloep = virkningFom?.let { grunnbeloepService.grunnbeloep(dato = it.toNorwegianLocalDate()) }
            ?: throw RuntimeException("Failed to obtain grunnbeløp - virkningFom null")

        for (persongrunnlag in spec.persongrunnlagListe) {
            val vedtak = VilkarsVedtak().apply {
                vilkarsvedtakResultatEnum = innvilgetResultat
                virkFom = virkningFom
                virkTom = null
                gjelderPerson = persongrunnlag.penPerson
                penPerson = persongrunnlag.penPerson // ref. PEN: VilkarsVedtakToReglerMapper.mapVilkarsVedtak
            }

            persongrunnlag.personDetaljListe.forEach {
                updateVedtak(spec, vedtak, persongrunnlag, personDetalj = it, grunnbeloep)
            }
        }
    }

    // PEN: SimulerPensjonsberegningCommand.simulate -> SimulerPensjonsberegningCommand.simulateCorrectTypeOfPensjon
    private fun simulerPre2025OffentligAfp(spec: Simulering): Simuleringsresultat {
        setBeregnForsoergingstilleggAndEktefelleMottarPensjon(spec) // TODO use function arguments instead?

        try {
            // SIMDOM: Only AFP (pre-2025 offentlig AFP) supported in this context
            if (SimuleringTypeEnum.AFP == spec.simuleringTypeEnum) {
                return simulerPre2025OffentligAfp(
                    spec,
                    beregnInstopphold,
                    beregnForsorgingstillegg,
                    ektefelleMottarPensjon
                )
            } else {
                throw RuntimeException("Unsupported simuleringtype in pre-2025 offentlig AFP context: ${spec.simuleringTypeEnum}")
            }
        } catch (e: KanIkkeBeregnesException) {
            throw FeilISimuleringsgrunnlagetException(e)
        } catch (e: RegelmotorValideringException) {
            throw KonsistensenIGrunnlagetErFeilException(e)
        }
    }

    // PEN: DefaultSimuleringConsumerService.simulerAFP
    //   -> SimulerPensjonsberegningConsumerCommand.execute with ytelse = SimulerPensjonsberegningConsumerCommand.AFP
    private fun simulerPre2025OffentligAfp(
        spec: Simulering,
        beregnInstitusjonsopphold: Boolean,
        beregnForsoergingstillegg: Boolean,
        epsMottarPensjon: Boolean
    ): Simuleringsresultat =
        context.simulerPre2025OffentligAfp(
            SimuleringRequest(
                simulering = spec,
                fom = spec.uttaksdato,
                ektefelleMottarPensjon = epsMottarPensjon,
                beregnForsorgingstillegg = beregnForsoergingstillegg,
                beregnInstitusjonsopphold = beregnInstitusjonsopphold
            )
        )

    // PEN: Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig
    private fun addEpsInntektGrunnlag(
        eps: Eps,
        inntektGrunnlagListe: MutableList<Inntektsgrunnlag>,
        foersteUttakDato: LocalDate?
    ) {
        removeInntektsgrunnlagForventetArbeidsinntekt(inntektGrunnlagListe)

        // PEN: SimulerAFPogAPCommand.isEpsMottarPensjon
        if (eps.harPensjon) {
            inntektGrunnlagListe.add(
                inntektsgrunnlag(
                    fom = foersteUttakDato?.toNorwegianDateAtNoon(),
                    type = InntekttypeEnum.PENF, // Pensjonsinntekt fra folketrygden
                    beloep = 1
                )
            )
        }

        // PEN: SimulerAFPogAPCommand.isEpsInntektOver2g
        if (eps.harInntektOver2G) {
            inntektGrunnlagListe.add(
                inntektsgrunnlag(
                    fom = foersteUttakDato?.toNorwegianDateAtNoon(),
                    type = InntekttypeEnum.FPI, // Forventet pensjongivende inntekt
                    beloep = 3 * grunnbeloepService.naavaerendeGrunnbeloep()
                )
            )
        }
    }

    // PEN: SimulerPensjonsberegningCommand.setBeregnForsorgingstilleggAndEktefelleMottarPensjon
    private fun setBeregnForsoergingstilleggAndEktefelleMottarPensjon(spec: Simulering) {
        spec.vilkarsvedtakliste.forEach {
            val kravlinjeType = it.kravlinjeTypeEnum

            if (kravlinjeType == KravlinjeTypeEnum.ET || kravlinjeType == KravlinjeTypeEnum.BT) {
                beregnForsorgingstillegg = true
            }
        }

        ektefelleMottarPensjon = epsMottarPensjon(spec.persongrunnlagListe)
    }

    companion object {
        const val AFP_VIRKNING_TOM_ALDER_AAR: Int = 67 // TODO use normalder?
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

        // PEN: EktefelleMottarPensjonDecider.isEktefelleMottarPensjon
        private fun epsMottarPensjon(persongrunnlagListe: List<Persongrunnlag>): Boolean {
            val epsGrunnlag = persongrunnlagListe.firstOrNull { p -> p.personDetaljListe.any { it.isEps() } }

            return epsGrunnlag?.inntektsgrunnlagListe.orEmpty()
                .any { it.inntektTypeEnum == InntekttypeEnum.PENF && it.belop > 0 }
        }

        // PEN: SimulerAFPogAPCommand.createInntektsgrunnlag
        private fun inntektsgrunnlag(fom: Date?, type: InntekttypeEnum, beloep: Int) =
            Inntektsgrunnlag().apply {
                inntektTypeEnum = type
                this.fom = fom
                this.belop = beloep
                grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
                bruk = true
                //kopiertFraGammeltKrav = Boolean.FALSE
            }

        // PEN: Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig + .getSimuleringGrunnlagKilde + .getInntektMaanedenFoerUttakInntektstype
        private fun inntektsgrunnlagMaanedenFoerUttak(spec: SimuleringSpec) =
            Inntektsgrunnlag().apply {
                bruk = true
                inntektTypeEnum = InntekttypeEnum.IMFU // IMFU = Inntekt måneden før uttak
                //fom = spec.foersteUttakDato?.toNorwegianDateAtNoon()?.let { getRelativeDateByMonth(it, -1) }
                fom = spec.foersteUttakDato?.minusMonths(1)?.toNorwegianDateAtNoon()
                belop = spec.pre2025OffentligAfp?.inntektMaanedenFoerAfpUttakBeloep ?: 0
                grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
            }

        // PEN: SimulerPensjonsberegningCommand.opprettEktefelleTillegg
        private fun opprettEktefelleTillegg(
            spec: Simulering,
            epsGrunnlag: Persongrunnlag,
            grunnbeloep: Int
        ): Boolean {
            // Check if "søker" is applicable for "ektefelletillegg"
            if (SimuleringTypeEnum.ALDER != spec.simuleringTypeEnum) return false

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

        // PEN: Extracted from SimulerPensjonsberegningCommand.simulateTrygdetid
        private fun trygdetid(utlandAntallAar: Int): Int {
            val trygdetid = GRUNNLAG_FOR_BEREGNING_AV_TRYGDETID - utlandAntallAar

            return when {
                trygdetid < 0 -> 0
                trygdetid > MAX_TRYGDETID -> MAX_TRYGDETID
                else -> trygdetid
            }
        }

        // PEN: SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun updateVedtak(
            spec: Simulering,
            vedtak: VilkarsVedtak,
            persongrunnlag: Persongrunnlag,
            personDetalj: PersonDetalj,
            grunnbeloep: Int
        ) {
            // Based on the type of simulation and "grunnlagsrolle", set the type of "vilkårsvedtak" and find the "første virkningsdato"
            var virkningFom = vedtak.virkFom
            var kravlinjeType: KravlinjeTypeEnum? = null
            val rolle = personDetalj.grunnlagsrolleEnum
            val simuleringType: SimuleringTypeEnum? = spec.simuleringTypeEnum

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
                if (opprettEktefelleTillegg(spec, persongrunnlag, grunnbeloep)) {
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
                spec.vilkarsvedtakliste.add(vedtak)
            }
        }

        // PEN: Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig + .getAfpSimuleringsType
        private fun afpSpec(spec: SimuleringSpec, persongrunnlagListe: MutableList<Persongrunnlag>) =
            Simulering().apply {
                simuleringTypeEnum = SimuleringTypeEnum.AFP
                uttaksdato = spec.foersteUttakDato?.toNorwegianDateAtNoon()
                afpOrdningEnum = spec.pre2025OffentligAfp?.afpOrdning?.name?.let(AFPtypeEnum::valueOf)
                this.persongrunnlagListe = persongrunnlagListe
            }

        // PEN: SimulerPensjonsberegningCommand.findPersongrunnlagWithGivenRole
        private fun findPersongrunnlagHavingRolle(
            persongrunnlagListe: List<Persongrunnlag>,
            rolle: GrunnlagsrolleEnum
        ): Persongrunnlag? =
            persongrunnlagListe.firstOrNull { hasRolle(it, rolle) }

        // PEN: Extracted from SimulerPensjonsberegningCommand.findPersongrunnlagWithGivenRole
        private fun hasRolle(persongrunnlag: Persongrunnlag, rolle: GrunnlagsrolleEnum) =
            persongrunnlag.personDetaljListe.any { rolle == it.grunnlagsrolleEnum }

        // PEN: Extracted from SimulerPensjonsberegningCommand.execute
        private fun avslag(status: VedtakResultatEnum) =
            VedtakResultatEnum.AVSL == status || VedtakResultatEnum.VETIKKE == status

        // PEN: Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig
        private fun ikkeInnvilget(status: VedtakResultatEnum?) =
            status == null || VedtakResultatEnum.INNV != status

        // PEN: Extracted from SimulerPensjonsberegningCommand.execute
        private fun minsteTrygdetidMerknad() =
            Merknad().apply { kode = MINSTE_TRYGDETID }

        // PEN: SimulerAFPogAPCommandHelper.copyPersongrunnlagList
        private fun copy(persongrunnlagListe: List<Persongrunnlag>): MutableList<Persongrunnlag> =
            persongrunnlagListe.map {
                Persongrunnlag(
                    source = it,
                    excludeForsteVirkningsdatoGrunnlag = true
                )
            }.toMutableList()

        // PEN: SimulerAFPogAPCommand.findBeregningsInformasjonFromForrigeBerresAp
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

        // PEN: SimulerAFPogAPCommand.removeInntektsgrunnlagForventetArbeidsinntektFromList
        private fun removeInntektsgrunnlagForventetArbeidsinntekt(inntektGrunnlagListe: MutableList<Inntektsgrunnlag>) {
            inntektGrunnlagListe.removeIf {
                it.inntektTypeEnum == InntekttypeEnum.FPI
            }
        }

        // PEN: Extracted from SimulerPensjonsberegningCommand.createVilkarsvedtakList
        private fun virkningFom(uttakDato: Date): Date =
            NorwegianCalendar.forDate(uttakDato).apply {
                this[Calendar.DAY_OF_MONTH] = 1
            }.time

        // PEN: Extracted from SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun gjelderEps(rolle: String?, personDetalj: PersonDetalj) =
            (GrunnlagsrolleEnum.EKTEF.name == rolle
                    || GrunnlagsrolleEnum.PARTNER.name == rolle
                    || GrunnlagsrolleEnum.SAMBO.name == rolle && (BorMedTypeEnum.SAMBOER1_5 == personDetalj.borMedEnum))

        // PEN: Extracted from SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun soekerKravlinjeType(simuleringType: SimuleringTypeEnum): KravlinjeTypeEnum? =
            when (simuleringType) {
                SimuleringTypeEnum.ALDER -> KravlinjeTypeEnum.AP
                SimuleringTypeEnum.ALDER_M_GJEN -> KravlinjeTypeEnum.AP
                SimuleringTypeEnum.AFP -> KravlinjeTypeEnum.AFP
                SimuleringTypeEnum.GJENLEVENDE -> KravlinjeTypeEnum.GJP
                SimuleringTypeEnum.BARN -> KravlinjeTypeEnum.BP
                else -> null
            }

        // PEN: Extracted from SimulerPensjonsberegningCommand.updateVilkarsvedtak
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
