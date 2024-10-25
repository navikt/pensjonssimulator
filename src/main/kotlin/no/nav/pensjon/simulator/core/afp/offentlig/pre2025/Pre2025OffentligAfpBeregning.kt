package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpPersongrunnlag.Companion.persongrunnlagHavingRolle
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.kode.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.domain.regler.to.SimuleringRequest
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.BeregningsmotorValidereException
import no.nav.pensjon.simulator.core.exception.BeregningstjenesteFeiletException
import no.nav.pensjon.simulator.core.exception.KanIkkeBeregnesException
import no.nav.pensjon.simulator.core.legacy.util.DateUtil
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toDate
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.normalder.NormAlderService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.sak.SakService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// Corresponds to SimulerAFPogAPCommand (beregning part)
@Component
class Pre2025OffentligAfpBeregning(
    private val context: SimulatorContext,
    private val normAlderService: NormAlderService,
    private val sakService: SakService
) {

    private var beregnInstopphold = false // TODO this seems to be always false
    private var beregnForsorgingstillegg = false // TODO pass as parameter instead?
    private var ektefelleMottarPensjon = false // TODO pass as parameter instead?

    // SimulerAFPogAPCommand.beregnAfpOffentlig
    //@Throws(PEN222BeregningstjenesteFeiletException::class)
    fun beregnAfpOffentlig(
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
            addEpsInnteksgrunnlag(
                inntektGrunnlagListe = it.inntektsgrunnlagListe,
                spec,
                beregningInfo = beregningInfoFraForrigeAlderspensjonBeregningResultat(
                    kravhode,
                    forrigeAlderspensjonBeregningResultat
                ),
                grunnbeloep
            )
        }

        val soekerGrunnlag: Persongrunnlag? = persongrunnlagHavingRolle(persongrunnlagListe, GrunnlagsrolleEnum.SOKER)
        soekerGrunnlag?.inntektsgrunnlagListe?.add(inntektsgrunnlagOneManedBeforeUttak(spec))
        val normAlder: Alder = normAlderService.normAlder(spec.pid)

        val simuleringResultat: Simuleringsresultat =
            simulerPensjonsberegning(spec, persongrunnlagListe, normAlder)

        if (avslag(simuleringResultat.status)) {
            //log.info(
            //    "Simulering AFP offentlig - Ikke innvilget - Kode {} - Merknader {}",
            //    if (status == null) "(ingen)" else status.code, SimulerAFPogAPCommand.asCsv(simuleringsresultat.merknadListe)
            //)

            // Throwing PEN240VilkarsprovingAvAFPOffentligErAvslattException without packaging it in a RuntimeException, would
            // force the execute method of the abstract class which this class relies on to throw
            // PEN240VilkarsprovingAvAFPOffentligErAvslattException. This would then cascade into forcing the other classes
            // inheriting from the abstract class to also throw the exception, impacting a lot more code than intended by the
            // design.
            //
            // For this reason the Exception is packaged in a RuntimeException, which will be explicitly catch by the service
            // using this command. This service will unpack the original exception and throw it, accordingly to the intended
            // design.
            //val cause = PEN240VilkarsprovingAvAFPOffentligErAvslattException(simuleringsresultat.merknadListe.map(SimulatorContext.Companion::mapMerknadToPen))
            //throw InternalSimuleringVilkarsprovingAvAFPOffentligErAvslattException(cause)
            throw Pre2025OffentligAfpAvslaattException(simuleringResultat.merknadListe.joinToString(", "))
        }

        populateKravhodeWithAfpHistorikk(kravhode, simuleringResultat.beregning, spec)
        return Pre2025OffentligAfpResult(simuleringResultat, kravhode)
    }

    private fun simulerPensjonsberegning(
        spec: SimuleringSpec,
        persongrunnlagListe: MutableList<Persongrunnlag>,
        normAlder: Alder
    ): Simuleringsresultat =
        try {
            simulerPensjonsberegning(simulering(spec, persongrunnlagListe), normAlder)
        } catch (e: ForUngForSimuleringException) {
            throw BeregningstjenesteFeiletException(e.message)
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            throw BeregningstjenesteFeiletException(e.message)
        } catch (e: FeilISimuleringsgrunnlagetException) {
            throw BeregningstjenesteFeiletException(e.message)
        }

    // SimpleSimuleringService.simulerPensjonsberegning
    // -> SimulerPensjonsberegningCommand.execute
    //@Throws(PEN070KonsistensenIGrunnlagetErFeilException::class, PEN071FeilISimuleringsgrunnlagetException::class, PEN019ForUngForSimuleringException::class)
    private fun simulerPensjonsberegning(simulering: Simulering, normAlder: Alder): Simuleringsresultat {
        validateInput(simulering, normAlder)
        var simuleringAvslag: Boolean = simulerTrygdetid(simulering)

        if (simuleringAvslag) {
            return Simuleringsresultat().apply {
                status = VilkarsvedtakResultatCti(VedtakResultatEnum.AVSL.name)
                merknadListe.add(minsteTrygdetidMerknad())
            }
        }

        addUfoereHistorikk(simulering)
        createVedtakListe(simulering)
        var simuleringsresultat = Simuleringsresultat()

        if (SimuleringTypeEnum.AFP.name == simulering.simuleringType?.kode) {
            setBeregnForsorgingstilleggAndEktefelleMottarPensjon(simulering)
            simuleringsresultat = simulerVilkarsprovAfp(simulering)
            simuleringsresultat.status?.let { simuleringAvslag = avslag(it.kode) }
        }

        if (!simuleringAvslag) {
            simuleringsresultat = simulerPre2025OffentligAfp(simulering)
        }

        // Must be set explicitly, because pensjon-regler does not simulate "vilkår"
        if (simuleringsresultat.status == null) {
            simuleringsresultat.status = VilkarsvedtakResultatCti(VedtakResultatEnum.INNV.name)
        }

        return simuleringsresultat
    }

    // SimulerPensjonsberegningCommand.simulateVilkarsprvAFP
    // -> DefaultSimuleringConsumerService.simulerVilkarsprovAfp
    // -> SimulerVilkarsprovAfpConsumerCommand.execute
    //@Throws(PEN071FeilISimuleringsgrunnlagetException::class, PEN070KonsistensenIGrunnlagetErFeilException::class)
    private fun simulerVilkarsprovAfp(simulering: Simulering): Simuleringsresultat =
        try {
            context.simulerVilkarsprovPre2025OffentligAfp(SimuleringRequest(simulering, simulering.uttaksdato))
        } catch (e: KanIkkeBeregnesException) {
            throw FeilISimuleringsgrunnlagetException(e)
        } catch (e: BeregningsmotorValidereException) {
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

            // Validate trygdetid to see if the simulation should be rejected:
            if (SimuleringTypeEnum.ALDER.name == simulering.simuleringType?.kode) {
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
        val simuleringType: String? = simulering.simuleringType?.kode

        if (simuleringType == SimuleringTypeEnum.ALDER.name
            || simuleringType == SimuleringTypeEnum.ALDER_M_GJEN.name
            || simuleringType == SimuleringTypeEnum.AFP.name
        ) {
            val soekerGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(simulering.persongrunnlagListe, GrunnlagsrolleEnum.SOKER)
            val soekerUfoereHistorikk: Uforehistorikk? =
                fetchUforehistorikk(soekerGrunnlag?.penPerson, simulering.uttaksdato)

            if (soekerUfoereHistorikk?.containsActualUforeperiode() == true) {
                soekerGrunnlag?.uforeHistorikk = soekerUfoereHistorikk
            }
        }

        if (simuleringType == SimuleringTypeEnum.ALDER_M_GJEN.name) {
            val avdoedGrunnlag: Persongrunnlag? =
                findPersongrunnlagHavingRolle(simulering.persongrunnlagListe, GrunnlagsrolleEnum.AVDOD)

            val avdoedUfoereHistorikk: Uforehistorikk? =
                fetchUforehistorikk(avdoedGrunnlag?.penPerson, simulering.uttaksdato)

            if (avdoedUfoereHistorikk?.containsActualUforeperiode() == true) {
                avdoedGrunnlag?.uforeHistorikk = avdoedUfoereHistorikk
            }
        }

        if (simuleringType == SimuleringTypeEnum.BARN.name) {
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
        val soeker = penPerson?.pid?.let(::fetchPerson)
        val ufoereHistorikk: Uforehistorikk = soeker?.uforehistorikk ?: return null

        // Creating copy to prevent hibernate from updating the actual object.
        // TODO hibernate issues not relevant in this context
        return Uforehistorikk(ufoereHistorikk).apply {
            uforeperiodeListe = uforeperiodeListe.filter { it.virk?.before(virkningsdato) == true }.toMutableList()
        }
    }

    // SimulerPensjonsberegningCommand.createVilkarsvedtakList
    private fun createVedtakListe(simulering: Simulering) {
        val innvilgetResultat = VilkarsvedtakResultatCti(VedtakResultatEnum.INNV.name)

        for (persongrunnlag in simulering.persongrunnlagListe) {
            val vedtak = VilkarsVedtak().apply {
                vilkarsvedtakResultat = innvilgetResultat
                virkFom = virkningFom(simulering.uttaksdato)
                virkTom = null
                gjelderPerson = persongrunnlag.penPerson
            }

            val grunnbeloep = fetchGrunnbeloep(vedtak.virkFom.toLocalDate()!!) // TODO reuse for each persongrunnlag?

            persongrunnlag.personDetaljListe.forEach {
                updateVilkarsvedtak(simulering, vedtak, persongrunnlag, it, grunnbeloep)
            }
        }
    }

    private fun fetchGrunnbeloep(date: LocalDate): Double =
        context.fetchGrunnbeloepListe(date).satsResultater.firstOrNull()?.verdi ?: 0.0

    //TODO use a dedicated person service?
    private fun fetchPerson(pid: Pid): PenPerson =
        sakService.personVirkningDato(pid).person

    // SimulerPensjonsberegningCommand.simulate
    // -> SimulerPensjonsberegningCommand.simulateCorrectTypeOfPensjon
    //@Throws(PEN071FeilISimuleringsgrunnlagetException::class, PEN070KonsistensenIGrunnlagetErFeilException::class, PEN019ForUngForSimuleringException::class)
    private fun simulerPre2025OffentligAfp(simulering: Simulering): Simuleringsresultat {
        setBeregnForsorgingstilleggAndEktefelleMottarPensjon(simulering) // TODO use function arguments instead?

        try {
            // SIMDOM: Only AFP (pre-2025 offentlig AFP) supported in this context
            if (SimuleringTypeEnum.AFP.name == simulering.simuleringType?.kode) {
                return simulerPre2025OffentligAfp(
                    simulering,
                    beregnInstopphold,
                    beregnForsorgingstillegg,
                    ektefelleMottarPensjon
                )
            } else {
                throw RuntimeException("Unsupported simuleringtype in pre-2025 offentlig AFP context: ${simulering.simuleringType?.kode}")
            }
        } catch (e: KanIkkeBeregnesException) {
            throw FeilISimuleringsgrunnlagetException(e)
        } catch (e: BeregningsmotorValidereException) {
            throw KonsistensenIGrunnlagetErFeilException(e)
        }
    }

    // DefaultSimuleringConsumerService.simulerAFP
    // -> SimulerPensjonsberegningConsumerCommand.execute with ytelse = SimulerPensjonsberegningConsumerCommand.AFP
    //@Throws(PEN165KanIkkeBeregnesException::class, PEN166BeregningsmotorValidereException::class)
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
            val kravlinjeType = it.kravlinjeType?.kode

            if (kravlinjeType == KravlinjeTypeEnum.ET.name || kravlinjeType == KravlinjeTypeEnum.BT.name) {
                beregnForsorgingstillegg = true
            }
        }

        ektefelleMottarPensjon = epsMottarPensjon(simulering.persongrunnlagListe)
    }

    private companion object {
        private const val AFP_MIN_AGE: Int = 62
        private const val AFP_VIRKNING_TOM_ALDER_AAR: Int = 67 // TODO use normalder?
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
                afpOrdning = spec.afpOrdning?.let { AfpOrdningTypeCti(it.name) }
                afpPensjonsgrad = beregning?.afpPensjonsgrad ?: 0
                virkFom = spec.foersteUttakDato?.toDate()
                virkTom =
                    persongrunnlag.penPerson?.fodselsdato?.let{ DateUtil.firstDayOfMonthAfterUserTurnsGivenAge(it, AFP_VIRKNING_TOM_ALDER_AAR)}
            })
        }

        // Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig
        private fun addEpsInnteksgrunnlag(
            inntektGrunnlagListe: MutableList<Inntektsgrunnlag>,
            spec: SimuleringSpec,
            beregningInfo: BeregningsInformasjon?,
            grunnbeloep: Int
        ) {
            removeInntektsgrunnlagForventetArbeidsinntekt(inntektGrunnlagListe)

            if (epsMottarPensjon(spec, beregningInfo)) {
                inntektGrunnlagListe.add(
                    inntektsgrunnlag(
                        fom = spec.foersteUttakDato?.toDate(),
                        type = InntekttypeEnum.PENF, // Pensjonsinntekt fra folketrygden
                        beloep = 1
                    )
                )
            }

            if (epsHarInntektOver2G(spec, beregningInfo)) {
                inntektGrunnlagListe.add(
                    inntektsgrunnlag(
                        fom = spec.foersteUttakDato?.toDate(),
                        type = InntekttypeEnum.FPI, // Forventet pensjongivende inntekt
                        beloep = 3 * grunnbeloep
                    )
                )
            }
        }

        // SimulerAFPogAPCommand.isEpsInntektOver2g
        private fun epsHarInntektOver2G(
            spec: SimuleringSpec,
            forrigeBeregning: BeregningsInformasjon?
        ): Boolean =
            spec.epsHarInntektOver2G || (forrigeBeregning?.epsOver2G ?: false)

        // SimulerAFPogAPCommand.isEpsMottarPensjon
        private fun epsMottarPensjon(
            spec: SimuleringSpec,
            forrigeBeregning: BeregningsInformasjon?
        ): Boolean =
            spec.epsHarPensjon || (forrigeBeregning?.epsMottarPensjon ?: false)

        // EktefelleMottarPensjonDecider.isEktefelleMottarPensjon
        private fun epsMottarPensjon(persongrunnlagListe: List<Persongrunnlag>): Boolean {
            val epsGrunnlag = persongrunnlagListe.firstOrNull { p -> p.personDetaljListe.any { it.isEps() } }

            return epsGrunnlag?.inntektsgrunnlagListe.orEmpty()
                .any { it.inntektType?.kode == InntekttypeEnum.PENF.name && it.belop > 0 }
        }

        // SimulerAFPogAPCommand.createInntektsgrunnlag
        private fun inntektsgrunnlag(fom: Date?, type: InntekttypeEnum, beloep: Int) =
            Inntektsgrunnlag().apply {
                inntektType = InntektTypeCti(type.name)
                this.fom = fom
                this.belop = beloep
                grunnlagKilde = GrunnlagKildeCti(GrunnlagkildeEnum.SIMULERING.name)
                bruk = true
                //kopiertFraGammeltKrav = Boolean.FALSE
            }

        // Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig + .getSimuleringGrunnlagKilde + .getInntektMaanedenFoerUttakInntektstype
        private fun inntektsgrunnlagOneManedBeforeUttak(spec: SimuleringSpec) =
            Inntektsgrunnlag().apply {
                bruk = true
                inntektType = InntektTypeCti(InntekttypeEnum.IMFU.name) // IMFU = Inntekt måneden før uttak
                fom = DateUtil.getRelativeDateByMonth(spec.foersteUttakDato?.toDate(), -1)
                belop = spec.afpInntektMaanedFoerUttak ?: 0
                grunnlagKilde = GrunnlagKildeCti(GrunnlagkildeEnum.SIMULERING.name)
            }

        // SimulerPensjonsberegningCommand.opprettEktefelleTillegg
        private fun opprettEktefelleTillegg(
            simulering: Simulering,
            epsGrunnlag: Persongrunnlag,
            grunnbeloep: Double
        ): Boolean {
            // Check if "søker" is applicable for "ektefelletillegg"
            val alderspensjonSimulering = SimuleringTypeEnum.ALDER.name == simulering.simuleringType?.kode
            if (!alderspensjonSimulering) return false

            var opprettEktefelleTillegg = false
            var pensjonsinntektFraFolketrygden = 0
            var forventetPensjongivendeInntekt = 0

            for (inntektsgrunnlag in epsGrunnlag.inntektsgrunnlagListe) {
                val inntektType = inntektsgrunnlag.inntektType?.kode

                if (InntekttypeEnum.PENF.name == inntektType) {
                    pensjonsinntektFraFolketrygden = inntektsgrunnlag.belop
                } else if (InntekttypeEnum.FPI.name == inntektType) {
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
        private fun updateVilkarsvedtak(
            simulering: Simulering,
            vedtak: VilkarsVedtak,
            persongrunnlag: Persongrunnlag,
            personDetalj: PersonDetalj,
            grunnbeloep: Double
        ) {
            // Based on the type of simulation and "grunnlagsrolle", set the type of "vilkårsvedtak" and find the "første virkningsdato"
            var virkningFom = vedtak.virkFom
            var kravlinjeType: KravlinjeTypeCti? = null
            val rolle = personDetalj.grunnlagsrolleEnum

            val simuleringType: SimuleringTypeEnum? =
                simulering.simuleringType?.let { SimuleringTypeEnum.valueOf(it.kode) }

            if (GrunnlagsrolleEnum.SOKER == rolle) {
                kravlinjeType = simuleringType?.let(::soekerKravlinjeType)
            } else if (GrunnlagsrolleEnum.AVDOD == rolle) {
                kravlinjeType = simuleringType?.let(::avdoedKravlinjeType)
            } else if (GrunnlagsrolleEnum.BARN == rolle) {
                if (SimuleringTypeEnum.ALDER == simuleringType || SimuleringTypeEnum.ALDER_M_GJEN == simuleringType) {
                    if (personDetalj.barnDetalj?.inntektOver1G != true) {
                        kravlinjeType = kravlinjeTypeCti(KravlinjeTypeEnum.BT) // Barnetillegg
                        virkningFom = vedtak.virkFom
                    }
                }
            } else if (gjelderEps(rolle?.name, personDetalj)) {
                if (opprettEktefelleTillegg(simulering, persongrunnlag, grunnbeloep)) {
                    kravlinjeType = kravlinjeTypeCti(KravlinjeTypeEnum.ET) // Ektefelletillegg
                    virkningFom = vedtak.virkFom
                }
            }

            // Add the vilkårsvedtak to the simulation:
            kravlinjeType?.let {
                vedtak.kravlinje = Kravlinje().apply {
                    kravlinjeType = it
                    relatertPerson = persongrunnlag.penPerson
                }
                vedtak.kravlinjeType = it
                vedtak.forsteVirk = virkningFom
                simulering.vilkarsvedtakliste.add(vedtak)
            }
        }

        // Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig + .getAfpSimuleringsType
        private fun simulering(spec: SimuleringSpec, persongrunnlagListe: MutableList<Persongrunnlag>) =
            Simulering().apply {
                simuleringType = SimuleringTypeCti(SimuleringTypeEnum.AFP.name) // getAfpSimuleringsType
                uttaksdato = spec.foersteUttakDato?.toDate()
                afpOrdning = spec.afpOrdning?.let { AfpOrdningTypeCti(it.name) }
                this.persongrunnlagListe = persongrunnlagListe
            }

        // SimulerPensjonsberegningCommand.validateInput
        //@Throws(PEN019ForUngForSimuleringException::class)
        private fun validateInput(simulering: Simulering, normAlder: Alder) {
            val simuleringType =
                simulering.simuleringType ?: throw ImplementationUnrecoverableException("simulering.simuleringType")

            if (simulering.uttaksdato == null) {
                throw ImplementationUnrecoverableException("simulering.uttaksdato")
            }

            val simuleringTypeKode = simuleringType.kode

            if (SimuleringTypeEnum.AFP.name == simuleringTypeKode && simulering.afpOrdning == null) {
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

            val soekerFoedselDato = Calendar.getInstance().apply { time = soekerGrunnlag!!.fodselsdato }
            val uttakDato = Calendar.getInstance().apply { time = simulering.uttaksdato }
            val foedselMaaned = soekerFoedselDato[Calendar.MONTH]
            val uttakMaaned = uttakDato[Calendar.MONTH]
            val alder = uttakDato[Calendar.YEAR] - soekerFoedselDato[Calendar.YEAR]

            if (SimuleringTypeEnum.ALDER.name == simuleringTypeKode) {
                if (alder < normAlder.aar || alder == normAlder.aar && uttakMaaned <= foedselMaaned) {
                    throw ForUngForSimuleringException("Alderspensjon;${normAlder.aar};0")
                }
            }

            if (SimuleringTypeEnum.AFP.name == simuleringTypeKode) {
                if (alder < AFP_MIN_AGE || alder == AFP_MIN_AGE && uttakMaaned <= foedselMaaned) {
                    throw ForUngForSimuleringException("AFP;$AFP_MIN_AGE;0")
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
        private fun avslag(status: String) =
            VedtakResultatEnum.AVSL.name == status || VedtakResultatEnum.VETIKKE.name == status

        // Extracted from SimulerAFPogAPCommand.beregnAfpOffentlig
        private fun avslag(status: VilkarsvedtakResultatCti?) =
            status == null || VedtakResultatEnum.INNV.name != status.kode

        // Extracted from SimulerPensjonsberegningCommand.execute
        private fun minsteTrygdetidMerknad() = Merknad().apply { kode = MINSTE_TRYGDETID }

        // SimulerAFPogAPCommandHelper.copyPersongrunnlagList
        private fun copy(persongrunnlagListe: List<Persongrunnlag>): MutableList<Persongrunnlag> {
            val copy: MutableList<Persongrunnlag> = mutableListOf()

            persongrunnlagListe.forEach {
                copy.add(Persongrunnlag(source = it, excludeForsteVirkningsdatoGrunnlag = true))
            }

            return copy
        }

        // SimulerAFPogAPCommand.findBeregningsInformasjonFromForrigeBerresAp
        private fun beregningInfoFraForrigeAlderspensjonBeregningResultat(
            krav: Kravhode,
            beregningResultat: AbstraktBeregningsResultat?
        ): BeregningsInformasjon? {
            if (beregningResultat == null) return null

            return when (krav.regelverkTypeEnum) {
                RegelverkTypeEnum.N_REG_G_OPPTJ ->
                    (beregningResultat as BeregningsResultatAlderspensjon2011).beregningsInformasjonKapittel19

                RegelverkTypeEnum.N_REG_G_N_OPPTJ ->
                    ((beregningResultat as BeregningsResultatAlderspensjon2016).beregningsResultat2011)?.beregningsInformasjonKapittel19

                else ->
                    (beregningResultat as BeregningsResultatAlderspensjon2025).beregningsInformasjonKapittel20
            }
        }

        // SimulerAFPogAPCommand.removeInntektsgrunnlagForventetArbeidsinntektFromList
        private fun removeInntektsgrunnlagForventetArbeidsinntekt(inntektGrunnlagListe: MutableList<Inntektsgrunnlag>) {
            inntektGrunnlagListe.removeIf {
                it.inntektType?.kode == InntekttypeEnum.FPI.name
            }
        }

        // Extracted from SimulerPensjonsberegningCommand.createVilkarsvedtakList
        private fun virkningFom(uttakDato: Date?): Date =
            Calendar.getInstance().apply {
                time = uttakDato
                this[Calendar.DATE] = 1
            }.time

        // Extracted from SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun gjelderEps(rolle: String?, personDetalj: PersonDetalj) =
            //TODO use rolle enum
            (GrunnlagsrolleEnum.EKTEF.name == rolle
                    || GrunnlagsrolleEnum.PARTNER.name == rolle
                    || GrunnlagsrolleEnum.SAMBO.name == rolle && (BorMedTypeEnum.SAMBOER1_5 == personDetalj.borMedEnum))

        // Extracted from SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun soekerKravlinjeType(simuleringType: SimuleringTypeEnum): KravlinjeTypeCti? =
            when (simuleringType) {
                SimuleringTypeEnum.ALDER -> kravlinjeTypeCti(KravlinjeTypeEnum.AP)
                SimuleringTypeEnum.ALDER_M_GJEN -> kravlinjeTypeCti(KravlinjeTypeEnum.AP)
                SimuleringTypeEnum.AFP -> kravlinjeTypeCti(KravlinjeTypeEnum.AFP)
                SimuleringTypeEnum.GJENLEVENDE -> kravlinjeTypeCti(KravlinjeTypeEnum.GJP)
                SimuleringTypeEnum.BARN -> kravlinjeTypeCti(KravlinjeTypeEnum.BP)
                else -> null
            }

        // Extracted from SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun avdoedKravlinjeType(simuleringType: SimuleringTypeEnum): KravlinjeTypeCti? =
            when (simuleringType) {
                SimuleringTypeEnum.ALDER_M_GJEN -> kravlinjeTypeCti(KravlinjeTypeEnum.GJR)
                else -> null
            }

        // Extracted from SimulerPensjonsberegningCommand.updateVilkarsvedtak
        private fun kravlinjeTypeCti(type: KravlinjeTypeEnum) =
            KravlinjeTypeCti(
                kode = type.name,
                hovedKravlinje = type.erHovedkravlinje
            )
    }
}
