package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.privatAfp
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.LOCAL_ETERNITY
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getMonthBetween
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.intersectsWithPossiblyOpenEndings
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import java.time.LocalDate

// PEN: no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SimuleringEtter2011ResultatMapper
object SimulatorOutputMapper {

    // SimuleringEtter2011ResultatMapper.mapToSimuleringEtter2011Resultat
    fun mapToSimulatorOutput(
        simuleringSpec: SimuleringSpec,
        soekerGrunnlag: Persongrunnlag,
        grunnbeloep: Int
    ) =
        SimulatorOutput().apply {
            this.epsHarInntektOver2G = simuleringSpec.epsHarInntektOver2G
            this.epsHarPensjon = simuleringSpec.epsHarPensjon
            this.grunnbeloep = grunnbeloep
            this.sivilstand =
                soekerGrunnlag.personDetaljListe[0].sivilstandTypeEnum ?: throw RuntimeException("Undefined sivilstand")
        }

    fun simulertPrivatAfpPeriode(
        aarligBeloep: Int,
        resultat: BeregningsResultatAfpPrivat,
        alder: Int?
    ): SimulertPrivatAfpPeriode {
        val privatAfpUnderUtbetaling = resultat.pensjonUnderUtbetaling
        val ytelseKomponentListe = privatAfpUnderUtbetaling?.ytelseskomponenter.orEmpty()
        val privatAfp: AfpPrivatLivsvarig? = resultat.privatAfp()
        val afpKronetillegg = firstYtelseOfType(ytelseKomponentListe, YtelseskomponentTypeEnum.AFP_KRONETILLEGG)
        val afpKompensasjonstillegg = firstYtelseOfType(ytelseKomponentListe, YtelseskomponentTypeEnum.AFP_KOMP_TILLEGG)

        return SimulertPrivatAfpPeriode(
            alderAar = alder,
            aarligBeloep = aarligBeloep,
            maanedligBeloep = privatAfpUnderUtbetaling?.totalbelopNetto,
            livsvarig = privatAfp?.netto,
            kronetillegg = afpKronetillegg?.netto ?: 0,
            kompensasjonstillegg = afpKompensasjonstillegg?.netto ?: 0,
            afpForholdstall = privatAfp?.afpForholdstall,
            justeringBeloep = privatAfp?.justeringsbelop,
            afpOpptjening = resultat.afpPrivatBeregning?.afpOpptjening?.totalbelop?.toInt() ?: 0
        )
    }

    // SimuleringEtter2011ResultatMapper.mapToSimulertBeregningsinformasjon
    fun mapToSimulertBeregningsinformasjon(
        kravhode: Kravhode,
        beregningResultat: AbstraktBeregningsResultat,
        simulertAlderspensjon: SimulertAlderspensjon,
        foedselsdato: LocalDate,
        knekkpunkt: LocalDate
    ) =
        SimulertBeregningInformasjon().apply {
            val beregningsresultatKapittel19: BeregningsResultatAlderspensjon2011?
            val beregningsresultatKapittel20: BeregningsResultatAlderspensjon2025?
            val beregningsinfo: BeregningsInformasjon?

            if (erAp2011Beregning(kravhode)) {
                beregningsresultatKapittel19 = beregningResultat as? BeregningsResultatAlderspensjon2011
                beregningsresultatKapittel20 = null
                beregningsinfo = beregningsresultatKapittel19?.beregningsInformasjonKapittel19
            } else if (erAp2016Beregning(kravhode)) {
                val resultat2016 = beregningResultat as? BeregningsResultatAlderspensjon2016
                beregningsresultatKapittel19 = resultat2016?.beregningsResultat2011
                beregningsresultatKapittel20 = resultat2016?.beregningsResultat2025
                beregningsinfo = beregningsresultatKapittel19?.beregningsInformasjonKapittel19
            } else {
                beregningsresultatKapittel19 = null
                beregningsresultatKapittel20 = beregningResultat as? BeregningsResultatAlderspensjon2025
                beregningsinfo = beregningsresultatKapittel20?.beregningsInformasjonKapittel20
            }

            val beregningsinfoKapittel19: BeregningsInformasjon? =
                beregningsresultatKapittel19?.beregningsInformasjonKapittel19
            val pensjon: PensjonUnderUtbetaling? = beregningResultat.pensjonUnderUtbetaling
            val beregningKapittel20: AldersberegningKapittel20? = beregningsresultatKapittel20?.beregningKapittel20
            val beregningKapittel19: AldersberegningKapittel19? = beregningsresultatKapittel19?.beregningKapittel19

            if (beregningsresultatKapittel20 != null) {
                pensjon?.gjenlevendetilleggAP?.let {
                    this.apKap19medGJR = it.apKap19MedGJR
                    this.apKap19utenGJR = it.apKap19UtenGJR
                    this.gjtAP = it.bruttoPerAr.toInt()
                }

                val pensjonKapittel20: Int =
                    beregningsresultatKapittel20.pensjonUnderUtbetaling?.totalbelopNettoAr?.toInt() ?: 0
                this.kapittel20Pensjon = pensjonKapittel20
                this.vektetKapittel20Pensjon = (pensjonKapittel20 * simulertAlderspensjon.kapittel20Andel).toInt()
                this.pensjonBeholdningEtterUttak =
                    firstPensjonsbeholdning(beregningKapittel20?.beholdninger?.beholdninger.orEmpty())?.totalbelop?.toInt()
            }

            if (beregningsresultatKapittel19 != null) { // ref. jira.adeo.no/browse/PEB-442
                pensjon?.gjenlevendetilleggAPKap19?.let {
                    this.gjtAPKap19 = it.bruttoPerAr.toInt()
                }
            }

            beregningsinfoKapittel19?.let {
                this.vinnendeBeregning =
                    if (it.gjenlevenderettAnvendt) GrunnlagsrolleEnum.AVDOD
                    else GrunnlagsrolleEnum.SOKER
            }

            firstYtelseOfType(pensjon?.ytelseskomponenter.orEmpty(), YtelseskomponentTypeEnum.SKJERMT)?.let {
                this.skjermingstillegg = it.bruttoPerAr.toInt()

                if (it is Skjermingstillegg) {
                    this.ufoereGrad = it.ufg
                }
            }

            beregningsresultatKapittel19?.let {
                val totalbelopNettoAr = it.pensjonUnderUtbetaling?.totalbelopNettoAr?.toInt() ?: 0
                this.kapittel19Pensjon = totalbelopNettoAr
                this.vektetKapittel19Pensjon = (totalbelopNettoAr * simulertAlderspensjon.kapittel19Andel).toInt()
            }

            beregningKapittel19?.basispensjon?.let {
                this.basispensjon = it.totalbelop.toInt()
                this.basisGrunnpensjon = it.gp?.bruttoPerAr ?: 0.0
                this.basisTilleggspensjon = it.tp?.bruttoPerAr ?: 0.0
                this.basisPensjonstillegg = it.pt?.bruttoPerAr ?: 0.0
                this.minstePensjonsnivaSats = it.pt?.minstepensjonsnivaSats ?: 0.0
            }

            beregningKapittel19?.restpensjon?.let {
                val basisgrunnpensjon = it.gp?.bruttoPerAr?.toInt() ?: 0
                val basistilleggspensjon = it.tp?.bruttoPerAr?.toInt() ?: 0
                this.restBasisPensjon = basisgrunnpensjon + basistilleggspensjon
            }

            this.tt_anv_kap19 = beregningKapittel19?.tt_anv

            beregningKapittel20?.let {
                this.delingstall = it.delingstall
                this.tt_anv_kap20 = it.tt_anv
                this.garantipensjonssats = (it.beholdningerForForsteuttak ?: it.beholdninger)
                    ?.beholdninger?.filterIsInstance<Garantipensjonsbeholdning>()
                    ?.map(Garantipensjonsbeholdning::sats)?.firstOrNull()
            }

            beregningsinfo?.spt?.let {
                this.pa_f92 = it.poengrekke?.pa_f92
                this.pa_e91 = it.poengrekke?.pa_e91
                this.spt = it.pt
            }

            this.datoFom = knekkpunkt
            this.startMaaned = getMonthsBetweenInRange1To12(foedselsdato, knekkpunkt)
            this.aarligBeloep = pensjon?.totalbelopNettoAr?.toInt() ?: 0
            this.maanedligBeloep = pensjon?.totalbelopNetto
            this.inntektspensjon = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.IP)
            this.inntektspensjonPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.IP)
            this.garantipensjon = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.GAP)
            this.garantipensjonPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.GAP)
            this.garantitillegg = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.GAT)
            this.grunnpensjon = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.GP)
            this.grunnpensjonPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.GP)
            this.grunnpensjonsats = pensjon?.grunnpensjon?.pSats_gp
            this.tilleggspensjon = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.TP)
            this.tilleggspensjonPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.TP)
            this.pensjonstillegg = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.PT)
            this.pensjonstilleggPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.PT)
            this.minstePensjonsnivaSats = pensjon?.pensjonstillegg?.minstepensjonsnivaSats
            this.individueltMinstenivaaTillegg = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.MIN_NIVA_TILL_INDV)
            this.pensjonistParMinstenivaaTillegg = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.MIN_NIVA_TILL_PPAR)
            this.forholdstall = beregningsinfo?.forholdstallUttak
            this.uttakGrad = beregningResultat.uttaksgrad.toDouble()
        }

    /**
     * The logic behind useNullAsDefaultPensjonspoeng is found in PEN:
     * SimuleringEtter2011ResultatMapper.mapToSimulertOpptjening
     * In PEN poengtallListe is nullable; if null then pensjonspoengPi becomes null
     * Here poengtallListe is not nullable, so need useNullAsDefaultPensjonspoeng to carry this info
     */
    // SimuleringEtter2011ResultatMapper.mapToSimulertOpptjening
    fun mapToSimulertOpptjening(
        kalenderAar: Int,
        resultatListe: List<AbstraktBeregningsResultat>,
        soekerGrunnlag: Persongrunnlag,
        poengtallListe: List<Poengtall>,
        useNullAsDefaultPensjonspoeng: Boolean
    ): SimulertOpptjening {
        val opptjeningGrunnlagListe = soekerGrunnlag.opptjeningsgrunnlagListe

        return SimulertOpptjening(
            pensjonsgivendeInntekt = pensjonsgivendeInntektForAar(opptjeningGrunnlagListe, kalenderAar)?.pi ?: 0,
            kalenderAar = kalenderAar,
            pensjonsgivendeInntektPensjonspoeng = findValidForAar(poengtallListe, kalenderAar)?.pp
                ?: if (useNullAsDefaultPensjonspoeng) null else 0.0,
            omsorgPensjonspoeng = omsorgspoengForAar(opptjeningGrunnlagListe, kalenderAar),
            pensjonBeholdning = pensjonBeholdning(soekerGrunnlag, kalenderAar, resultatListe)?.totalbelop?.toInt(),
            omsorg = containsValidOmsorgsgrunnlagForAr(soekerGrunnlag.omsorgsgrunnlagListe, kalenderAar),
            dagpenger = dagpengegrunnlagAvTypeEksistererForAar(
                dagpengegrunnlagListe = soekerGrunnlag.dagpengegrunnlagListe,
                type = DagpengetypeEnum.DP,
                kalenderAar = kalenderAar
            ),
            dagpengerFiskere = dagpengegrunnlagAvTypeEksistererForAar(
                dagpengegrunnlagListe = soekerGrunnlag.dagpengegrunnlagListe,
                type = DagpengetypeEnum.DP_FF,
                kalenderAar = kalenderAar
            ),
            foerstegangstjeneste = soekerGrunnlag.forstegangstjenestegrunnlag?.let {
                containsValidForstegangstjenestePeriodeForAr(it.periodeListe, kalenderAar)
            },
            harUfoere = soekerGrunnlag.uforeHistorikk?.let {
                findEarliestIntersectingWithYear(filterUforeperioder(it.uforeperiodeListe), kalenderAar) != null
            },
            harOffentligAfp = harOffentligAfp(soekerGrunnlag.afpHistorikkListe, kalenderAar),
        )
    }

    //TODO: Sjekk hvilken AFP-type denne funksjonen dekker
    private fun harOffentligAfp(afpHistorikkListe: List<AfpHistorikk>?, kalenderAar: Int) =
        if (afpHistorikkListe.isNullOrEmpty())
            false
        else  // ref. no.nav.domain.pensjon.kjerne.grunnlag.Persongrunnlag.setAfpHistorikkListe
            isIntersectingWithYear(element = afpHistorikkListe[0], year = kalenderAar)

    // Part of SimuleringEtter2011ResultatMapper.mapToSimulertOpptjening
    private fun pensjonBeholdning(
        soekerGrunnlag: Persongrunnlag,
        kalenderAar: Int,
        resultatListe: List<AbstraktBeregningsResultat>
    ): Pensjonsbeholdning? {
        val beholdning = beholdningForAar(soekerGrunnlag.beholdninger, kalenderAar)
        if (beholdning != null) return beholdning

        val validBeregningsresultat =
            findValidForDate(resultatListe, firstDayOf(kalenderAar)) as? BeregningsResultatAlderspensjon2025

        return validBeregningsresultat?.let {
            findLatestPensjonsbeholdning(it.beregningKapittel20?.beholdninger?.beholdninger.orEmpty())
        }
    }

    private fun erAp2011Beregning(kravhode: Kravhode): Boolean =
        kravhode.regelverkTypeEnum == RegelverkTypeEnum.N_REG_G_OPPTJ

    private fun erAp2016Beregning(kravhode: Kravhode): Boolean =
        kravhode.regelverkTypeEnum == RegelverkTypeEnum.N_REG_G_N_OPPTJ

    private fun getMonthsBetweenInRange1To12(firstDate: LocalDate, secondDate: LocalDate): Int {
        val monthsBetween = getMonthBetween(firstDate, secondDate) % MAANEDER_PER_AAR
        return if (monthsBetween == 0) MAANEDER_PER_AAR else monthsBetween
    }

    // SimuleringEtter2011ResultatMapper.findBeholdningOfTypeForYear
    private fun beholdningForAar(beholdninger: List<Pensjonsbeholdning>, aar: Int): Pensjonsbeholdning? {
        val pensjonsbeholdninger = extractPensjonsbeholdninger(beholdninger)
        val filteredByTypeAndYear = sortedBeholdningSubset(pensjonsbeholdninger, aar)
        // NB: Det er typisk to beholdninger per år (før og etter regulering);
        // her plukkes den første i listen, men man vet ikke om den er før eller etter regulering:
        return if (filteredByTypeAndYear.isEmpty()) null else filteredByTypeAndYear[0]
    }

    // Specific version of SimuleringEtter2011ResultatMapper.findLatestBeholdningOfType
    private fun findLatestPensjonsbeholdning(beholdninger: List<Beholdning>): Pensjonsbeholdning {
        val pensjonsbeholdninger =
            extractPensjonsbeholdninger(beholdninger).sortedBy { it.ar } // PeriodisertInformasjonListeUtils + PeriodisertInformasjonAscendingDateComparator
        // NB: using ar instead of fom, since fom is always null in response from regler
        //TODO Sjekk om påstanden over er riktig
        return pensjonsbeholdninger.last()
    }

    private fun pensjonsgivendeInntektForAar(
        opptjeningGrunnlagListe: List<Opptjeningsgrunnlag>,
        kalenderAar: Int
    ): Opptjeningsgrunnlag? {
        val filteredByYear = sortedOpptjeningsgrunnlagSubset(opptjeningGrunnlagListe, kalenderAar)
        return extractPensjonsgivendeInntekter(filteredByYear).firstOrNull()
    }

    private fun omsorgspoengForAar(opptjeningGrunnlagListe: List<Opptjeningsgrunnlag>, kalenderAar: Int): Double {
        var omsorgspoeng = 0.0
        var priority = Int.MAX_VALUE

        val omsorgTypes = arrayOf(
            OpptjeningtypeEnum.OSFE,
            OpptjeningtypeEnum.OBO7H,
            OpptjeningtypeEnum.OBU7,
            OpptjeningtypeEnum.OBO6H,
            OpptjeningtypeEnum.OBU6
        )

        val prioritisedOmsorgTypeList = ArrayList(listOf(*omsorgTypes))
        var tempPriority: Int

        for (grunnlag in opptjeningGrunnlagListe) {
            if (grunnlag.ar == kalenderAar) {
                tempPriority = prioritisedOmsorgTypeList.indexOf(grunnlag.opptjeningTypeEnum)

                if (tempPriority != -1 && tempPriority < priority) {
                    priority = tempPriority
                    omsorgspoeng = grunnlag.pp
                }
            }
        }

        return omsorgspoeng
    }

    private fun dagpengegrunnlagAvTypeEksistererForAar(
        dagpengegrunnlagListe: List<Dagpengegrunnlag>,
        type: DagpengetypeEnum,
        kalenderAar: Int
    ) =
        sortedDagpengegrunnlagSubset(subsetOfTypes(dagpengegrunnlagListe, type), kalenderAar).isNotEmpty()

    private fun filterUforeperioder(perioder: MutableList<Uforeperiode>): MutableList<Uforeperiode> =
        perioder.filter { it.uforeTypeEnum != UforetypeEnum.VIRK_IKKE_UFOR }.toMutableList()

    private fun isIntersectingWithYear(element: AfpHistorikk?, year: Int): Boolean {
        if (element == null) return false
        val jan1st = firstDayOf(year)
        val dec31st = LocalDate.of(year, 12, 31)
        return intersectsWithPossiblyOpenEndings(jan1st, dec31st, element.virkFom, element.virkTom, true)
    }

    // Specific variant of TypedInformationListeUtils.subsetOfTypes
    private fun subsetOfTypes(list: List<Dagpengegrunnlag>, type: DagpengetypeEnum): List<Dagpengegrunnlag> =
        list.filter { it.dagpengetypeEnum == type }

    // Specific variant of TypedInformationListeUtils.subsetOfTypes
    private fun extractPensjonsbeholdninger(list: List<Beholdning>): List<Pensjonsbeholdning> =
        list.filter { it.beholdningsTypeEnum == BeholdningtypeEnum.PEN_B }.map { it as Pensjonsbeholdning }

    // Specific variant of TypedInformationListeUtils.subsetOfTypes
    private fun extractPensjonsgivendeInntekter(list: List<Opptjeningsgrunnlag>): List<Opptjeningsgrunnlag> =
        list.filter { it.opptjeningTypeEnum == OpptjeningtypeEnum.PPI }

    // ArligInformasjonListeUtils.sortedSubset + ArligInformasjonAscendingComparator
    // Duplicate in RegdomOpprettOutputHelper
    private fun sortedDagpengegrunnlagSubset(list: List<Dagpengegrunnlag>, year: Int): List<Dagpengegrunnlag> {
        val result = list.filter { it.ar == year }.toMutableList()
        result.sortBy { it.ar } //TODO: Seems unnecessary to sort this since year is same for all elements
        return result
    }

    // ArligInformasjonListeUtils.sortedSubset + ArligInformasjonAscendingComparator
    private fun sortedBeholdningSubset(list: List<Pensjonsbeholdning>, year: Int): List<Pensjonsbeholdning> {
        val result = list.filter { it.ar == year }.toMutableList()
        result.sortBy { it.ar } //TODO: Seems unnecessary to sort this since year is same for all elements
        return result
    }

    // ArligInformasjonListeUtils.sortedSubset + ArligInformasjonAscendingComparator
    private fun sortedOpptjeningsgrunnlagSubset(list: List<Opptjeningsgrunnlag>, year: Int): List<Opptjeningsgrunnlag> {
        val result = list.filter { it.ar == year }.toMutableList()
        result.sortBy { it.ar } //TODO: Seems unnecessary to sort this since year is same for all elements
        return result
    }

    // PeriodisertInformasjonListeUtils.findValidForDate
    private fun findValidForDate(list: List<AbstraktBeregningsResultat>, date: LocalDate): AbstraktBeregningsResultat? =
        list.firstOrNull { isValidForDate(it, date) }

    // From PeriodisertInformasjonUtils
    private fun isValidForDate(element: AbstraktBeregningsResultat, date: LocalDate) =
        isDateInPeriod(date, element.virkFom, element.virkTom)

    // From ArligInformasjonListeUtils
    private fun containsValidForstegangstjenestePeriodeForAr(list: List<ForstegangstjenestePeriode>, year: Int) =
        findValidForAar(list, year) != null

    // From ArligInformasjonListeUtils
    private fun containsValidOmsorgsgrunnlagForAr(list: List<Omsorgsgrunnlag>, year: Int) =
        findValidForAar(list, year) != null

    // Specific variant of ArligInformasjonListeUtils.findValidForYear
    private fun findValidForAar(list: List<ForstegangstjenestePeriode>, aar: Int): ForstegangstjenestePeriode? =
        list.firstOrNull { it.fomDato?.let(::getYear) == aar }

    // Specific variant of ArligInformasjonListeUtils.findValidForYear
    private fun findValidForAar(list: List<Omsorgsgrunnlag>, aar: Int): Omsorgsgrunnlag? =
        list.firstOrNull { it.ar == aar }

    // Specific variant of ArligInformasjonListeUtils.findValidForYear
    private fun findValidForAar(list: List<Poengtall>, aar: Int): Poengtall? =
        list.firstOrNull { it.ar == aar }

    // From PeriodisertInformasjonListeUtils
    private fun findEarliestIntersectingWithYear(list: List<Uforeperiode>, aar: Int): Uforeperiode? {
        val jan1st = firstDayOf(aar)
        val dec31st = LocalDate.of(aar, 12, 31)
        return findEarliestIntersecting(list, jan1st, dec31st)
    }

    // From PeriodisertInformasjonListeUtils
    private fun findEarliestIntersecting(
        list: List<Uforeperiode>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Uforeperiode? {
        var result: Uforeperiode? = null
        var earliestDate: LocalDate? = LOCAL_ETERNITY

        for (element in list) {
            if (intersectsWithPossiblyOpenEndings(startDate, endDate, element.ufgFom, element.ufgTom, true)) {
                if (isBeforeByDay(element.ufgFom, earliestDate, false)) {
                    earliestDate = element.ufgFom?.toNorwegianLocalDate()
                    result = element
                }
            }
        }

        return result
    }

    // Specific variant of ArligInformasjonListeUtils.findElementOfType
    // Duplicate in RegdomOpprettOutputHelper
    private fun firstPensjonsbeholdning(list: List<Beholdning>): Pensjonsbeholdning? =
        list.firstOrNull { BeholdningtypeEnum.PEN_B == it.beholdningsTypeEnum } as? Pensjonsbeholdning

    // Specific variant of ArligInformasjonListeUtils.findElementOfType
    private fun firstYtelseOfType(list: List<Ytelseskomponent>, type: YtelseskomponentTypeEnum): Ytelseskomponent? =
        list.firstOrNull { it.ytelsekomponentTypeEnum == type }

    private fun bruttoPerAar(pensjon: PensjonUnderUtbetaling?, ytelseType: YtelseskomponentTypeEnum) =
        firstYtelseOfType(pensjon?.ytelseskomponenter.orEmpty(), ytelseType)?.bruttoPerAr?.toInt()

    private fun bruttoPerMaaned(pensjon: PensjonUnderUtbetaling?, ytelseType: YtelseskomponentTypeEnum) =
        firstYtelseOfType(pensjon?.ytelseskomponenter.orEmpty(), ytelseType)?.brutto

    private fun firstDayOf(year: Int) = LocalDate.of(year, 1, 1)
}
