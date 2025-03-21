package no.nav.pensjon.simulator.core.beregn

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpYtelseMedDelingstall
import no.nav.pensjon.simulator.core.beregn.PeriodiseringUtil.periodiserGrunnlagAndModifyKravhode
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidRequest
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.privatAfp
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.beholdning
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktAarsak
import no.nav.pensjon.simulator.core.knekkpunkt.TrygdetidFastsetter
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.person.eps.EpsUtil.epsMottarPensjon
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.ubetingetPensjoneringDato
import no.nav.pensjon.simulator.core.util.isBeforeOrOn
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.core.vilkaar.Vilkaarsproever
import no.nav.pensjon.simulator.core.vilkaar.VilkaarsproevingSpec
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

/**
 * Vilkårsprøving og beregning av alderspensjon.
 */
// PEN: no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.VilkarsprovOgBeregnAlderHelper
@Component
class AlderspensjonVilkaarsproeverOgBeregner(
    private val context: SimulatorContext,
    private val beregner: AlderspensjonBeregner,
    private val vilkaarsproever: Vilkaarsproever,
    private val trygdetidFastsetter: TrygdetidFastsetter,
    private val sisteBeregningCreator: SisteBeregningCreator
) {
    private val log = KotlinLogging.logger { }

    fun vilkaarsproevOgBeregnAlder(spec: AlderspensjonVilkaarsproeverBeregnerSpec): AlderspensjonBeregnerResult {
        val simuleringSpec = spec.simulering

        if (simuleringSpec.type == SimuleringType.AFP_FPP) { // ref. PEN: SimulerAFPogAPCommand.vilkarsprovOgBeregnAlder
            return AlderspensjonBeregnerResult(
                beregningsresultater = mutableListOf(),
                pensjonsbeholdningPerioder = mutableListOf()
            )
        }

        val beregningResultatListe: MutableList<AbstraktBeregningsResultat> = mutableListOf()
        val knekkpunkter = spec.knekkpunkter
        val soekerFoersteVirkning = spec.sokerForsteVirk
        val avdoedFoersteVirkning = spec.avdodForsteVirk
        var forrigeVedtakListe = spec.forrigeVilkarsvedtakListe
        var forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat? = spec.forrigeAlderBeregningsresultat
        var sisteBeregning = spec.sisteBeregning
        var kravhode = periodiserGrunnlag(spec.kravhode)
        val soekerGrunnlag = kravhode.hentPersongrunnlagForRolle(rolle = GrunnlagsrolleEnum.SOKER, checkBruk = false)!!
        val avdoedGrunnlag = kravhode.hentPersongrunnlagForRolle(rolle = GrunnlagsrolleEnum.AVDOD, checkBruk = false)
        var vedtakListe: List<VilkarsVedtak>
        var sisteBeregning2011Tp: SisteBeregning? = null
        val loependeAlderspensjonBeregningResultat = forrigeAlderspensjonBeregningResultat
        val privatAfpBeregningResultatListe =
            mutableListOf<BeregningsResultatAfpPrivat>().also { addAfpBeregningResultat(spec, it) }
        val pensjonBeholdningPeriodeListe: MutableList<BeholdningPeriode> = mutableListOf()
        val vedtakListeAllePerioder: MutableList<VilkarsVedtak> = mutableListOf()

        vedtakListeAllePerioder.addAll(forrigeVedtakListe)
        log.debug { "knekkpunkter $knekkpunkter" }

        for (knekkpunktEntry: Map.Entry<LocalDate, List<KnekkpunktAarsak>> in knekkpunkter) {
            val knekkpunktDato = knekkpunktEntry.key
            val aarsaker = knekkpunktEntry.value

            // Corresponds to part 1
            if (forrigeAlderspensjonBeregningResultat != null && forrigeAlderspensjonBeregningResultat != loependeAlderspensjonBeregningResultat) {
                setForrigeBeregningResultVirkningTom(forrigeAlderspensjonBeregningResultat, knekkpunktDato)
            }

            // Corresponds to part 2
            oppdaterTrygdetidForSoekerOgAvdoed(
                kravhode,
                soekerGrunnlag,
                avdoedGrunnlag,
                knekkpunktDato,
                soekerFoersteVirkning,
                avdoedFoersteVirkning,
                aarsaker,
                spec.sakId
            )

            kravhode.persongrunnlagListe.forEach {
                it.sisteGyldigeOpptjeningsAr = knekkpunktDato.year - OPPTJENING_ETTERSLEP_ANTALL_AAR
            }

            // Corresponds to part 3
            val foedselsdato: LocalDate = soekerGrunnlag.fodselsdato!!.toNorwegianLocalDate()

            // Corresponds to part 4
            val gjeldendePrivatAfp = getPrivatAfp(privatAfpBeregningResultatListe, knekkpunktDato)

            val gjeldendeLivsvarigOffentligAfp: AfpOffentligLivsvarig? =
                getLivsvarigOffentligAfp(
                    resultatListe = spec.afpOffentligLivsvarigBeregningsresultat?.afpYtelseListe.orEmpty(),
                    knekkpunktDato
                )

            val livsvarigOffentligAfpGrunnlag = gjeldendeLivsvarigOffentligAfp?.let {
                AfpOffentligLivsvarigGrunnlag(
                    sistRegulertG = it.sistRegulertG ?: 0,
                    bruttoPerAr = it.bruttoPerAr,
                    uttaksdato = it.uttaksdato
                )
            }

            // Corresponds to part 5
            if (aarsaker.contains(KnekkpunktAarsak.UTG)) { // UTG = 'Endring av uttaksgrad'
                // CR195877 21.03.2011 OJB2812: When simulation is called from eksterne ordninger for a 2025-bruker, then we
                // should force a Kap 19 simulation even for pure Kap 20 brukere (2025) since eksterne ordninger currently
                // aren't able to parse a Kap 20 result. But - the vilkårsprøving must be done with a 2025-beregning. This
                // "hack" should be removed as soon as they're able to receive Kap 20 results.
                val sisteBeregningForVilkarsproving = sisteBeregning2011Tp ?: sisteBeregning

                val vilkaarsproevingSpec = vilkaarsproevingSpec(
                    livsvarigOffentligAfpGrunnlag,
                    gjeldendePrivatAfp,
                    knekkpunktDato,
                    kravhode,
                    spec.forsteVirkAfpPrivat,
                    sisteBeregningForVilkarsproving,
                    forrigeVedtakListe,
                    soekerFoersteVirkning,
                    avdoedFoersteVirkning,
                    spec.sakId,
                    spec.ignoreAvslag
                )

                val vilkaarsproevingResult = vilkaarsproever.vilkaarsproevKrav(vilkaarsproevingSpec)

                if (spec.onlyVilkaarsproeving)
                    return AlderspensjonBeregnerResult(beregningResultatListe, pensjonBeholdningPeriodeListe)

                vedtakListe = vilkaarsproevingResult.first
                kravhode = vilkaarsproevingResult.second
                setKravlinjeFoersteVirkning(vedtakListe, soekerGrunnlag.forsteVirkningsdatoGrunnlagListe)
                forrigeVedtakListe = vedtakListe
                vedtakListeAllePerioder.addAll(forrigeVedtakListe)
            }

            // Når vi kommer fra PSELV skal flagget settes i henhold til brukers løpende pensjon (ref. PK-15060)
            if (!simuleringSpec.isTpOrigSimulering && !simuleringSpec.epsKanOverskrives) {
                forrigeAlderspensjonBeregningResultat?.let {
                    if (it.beregningsinformasjon?.epsMottarPensjon == true) {
                        simuleringSpec.epsHarPensjon = true //TODO make simuleringSpec immutable?
                    }
                }
            }

            for (vedtak in vedtakListeAllePerioder) {
                if (vedtak.kravlinjeForsteVirk == null) { // ref. github.com/navikt/pensjon-pen/pull/11703
                    vedtak.fastsettForstevirkKravlinje(
                        vedtakListe = vedtakListeAllePerioder,
                        virkningListe = kravhode.sakForsteVirkningsdatoListe
                    )
                }
            }

            // Corresponds to part 6
            val gjeldendeBeregningsresultat = beregner.beregnAlderspensjon(
                kravhode,
                vedtakListe = forrigeVedtakListe,
                virkningDato = knekkpunktDato,
                sisteAldersberegning2011 = sisteBeregning,
                privatAfp = gjeldendePrivatAfp,
                livsvarigOffentligAfpGrunnlag,
                simuleringSpec,
                sakId = spec.sakId,
                isFoersteUttak = sisteBeregning == null,
                ignoreAvslag = spec.ignoreAvslag
            )

            // Corresponds to part 7
            forrigeAlderspensjonBeregningResultat = gjeldendeBeregningsresultat
            setEpsMottarPensjon(forrigeAlderspensjonBeregningResultat, simuleringSpec)
            beregningResultatListe.add(gjeldendeBeregningsresultat)

            // Corresponds to part 8
            sisteBeregning =
                getSisteAldersberegning(kravhode, forrigeVedtakListe, forrigeAlderspensjonBeregningResultat)

            // Corresponds to part 9
            // 21.03.2011: When simulation is called from eksterne ordninger for a 2025-bruker, then we
            // should force a Kap 19 simulation even for pure Kap 20 brukere (2025) since eksterne ordninger currently
            // aren't able to parse a Kap 20 result. But - we are required to do a 2025-beregning as well due to the
            // vilkårsprøving. This "hack" should be removed as soon as they're able to receive Kap 20 results.
            if (isCriteriaForDoingKap20ForSimulerForTpFullfilled(simuleringSpec, foedselsdato, knekkpunktDato)) {
                simuleringSpec.simulerForTp = false

                val gjeldendeBeregningsresultatTp = beregner.beregnAlderspensjon(
                    kravhode,
                    vedtakListe = forrigeVedtakListe,
                    virkningDato = knekkpunktDato,
                    sisteAldersberegning2011 = sisteBeregning, // <---- NB: sisteBeregning2011Tp in VilkarsprovOgBeregnAlderHelper
                    privatAfp = gjeldendePrivatAfp,
                    livsvarigOffentligAfpGrunnlag,
                    simuleringSpec = simuleringSpec,
                    sakId = spec.sakId,
                    isFoersteUttak = sisteBeregning2011Tp == null,
                    ignoreAvslag = spec.ignoreAvslag
                )

                sisteBeregning2011Tp =
                    getSisteAldersberegning(kravhode, forrigeVedtakListe, gjeldendeBeregningsresultatTp)
                simuleringSpec.simulerForTp = true
            }

            // Corresponds to part 10
            if (shouldAddPensjonBeholdningPerioder(spec.isHentPensjonsbeholdninger, kravhode.regelverkTypeEnum)) {
                val beholdningListe: MutableList<Pensjonsbeholdning> =
                    context.beregnOpptjening(
                        beholdningTom = knekkpunktDato,
                        persongrunnlag = soekerGrunnlag
                    ) // NB: modified in VilkarsprovOgBeregnAlderHelper 2024-08-14

                val folketrygdBeholdningKravhode: Kravhode =
                    periodiserGrunnlagAndModifyKravhode(knekkpunktDato, kravhode, beholdningListe, spec.sakType)

                val vilkarsvedtak: VilkarsVedtak = vilkaarsproever.innvilgetVedtak(
                    kravlinje = folketrygdBeholdningKravhode.findHovedKravlinje(spec.kravGjelder),
                    virkningFom = knekkpunktDato
                )

                val beregningResultat = beregner.beregnAlderspensjon(
                    kravhode = folketrygdBeholdningKravhode,
                    vedtakListe = mutableListOf(vilkarsvedtak),
                    virkningDato = knekkpunktDato,
                    sisteAldersberegning2011 = null, // null in legacy
                    privatAfp = gjeldendePrivatAfp,
                    livsvarigOffentligAfpGrunnlag,
                    simuleringSpec = simuleringSpec,
                    sakId = spec.sakId,
                    isFoersteUttak = true,
                    ignoreAvslag = spec.ignoreAvslag
                )

                if (beregningResultat is BeregningsResultatAlderspensjon2016) {
                    val beholdninger =
                        beregningResultat.beregningsResultat2025!!.beregningKapittel20!!.beholdningerForForsteuttak!!
                    pensjonBeholdningPeriodeListe.add(
                        beholdningPeriode(
                            virkningFom = beregningResultat.virkFom!!.toNorwegianLocalDate(),
                            beholdninger,
                            foedselsdato
                        )
                    )
                } else if (beregningResultat is BeregningsResultatAlderspensjon2025) {
                    val beholdninger = beregningResultat.beregningKapittel20!!.beholdningerForForsteuttak!!
                    pensjonBeholdningPeriodeListe.add(
                        beholdningPeriode(
                            virkningFom = beregningResultat.virkFom!!.toNorwegianLocalDate(),
                            beholdninger,
                            foedselsdato
                        )
                    )
                }
            }
        }

        return AlderspensjonBeregnerResult(beregningResultatListe, pensjonBeholdningPeriodeListe)
    }

    private fun oppdaterTrygdetidForSoekerOgAvdoed(
        kravhode: Kravhode,
        soekerGrunnlag: Persongrunnlag,
        avdoedGrunnlag: Persongrunnlag?,
        knekkpunktDato: LocalDate,
        soekerFoersteVirkning: LocalDate,
        avdoedFoersteVirkning: LocalDate?,
        aarsakListe: List<KnekkpunktAarsak>,
        sakId: Long?
    ) {
        // Corresponds to part 2
        if (aarsakListe.contains(KnekkpunktAarsak.TTBRUKER)) {
            val request = trygdetidRequest(
                kravhode = kravhode,
                persongrunnlag = soekerGrunnlag,
                knekkpunktDato = knekkpunktDato,
                soekerForsteVirkningFom = soekerFoersteVirkning,
                ytelseType = KravlinjeTypeEnum.AP,
                boddEllerArbeidetUtenlands = kravhode.boddEllerArbeidetIUtlandet
            )

            fastsettTrygdetidForPeriode(request, GrunnlagsrolleEnum.SOKER, soekerGrunnlag.gjelderUforetrygd, sakId)
        }

        if (aarsakListe.contains(KnekkpunktAarsak.TTAVDOD)) {
            val request = trygdetidRequest(
                kravhode = kravhode,
                persongrunnlag = avdoedGrunnlag!!,
                knekkpunktDato = knekkpunktDato,
                soekerForsteVirkningFom = avdoedFoersteVirkning!!, // TODO: check possible mismatch (soker vs avdod)
                ytelseType = KravlinjeTypeEnum.GJR,
                boddEllerArbeidetUtenlands = kravhode.boddArbeidUtlandAvdod
            )

            fastsettTrygdetidForPeriode(request, GrunnlagsrolleEnum.AVDOD, avdoedGrunnlag.gjelderUforetrygd, sakId)
        }
    }

    // VilkarsprovOgBeregnAlderHelper.fastsettTrygdetidForPeriode + FastsettTrygdetidCache.updateSisteGyldigeOpptjeningsaar
    private fun fastsettTrygdetidForPeriode(
        request: TrygdetidRequest,
        grunnlagRolle: GrunnlagsrolleEnum,
        kravGjelderUfoeretrygd: Boolean,
        sakId: Long?
    ) {
        val response =
            trygdetidFastsetter.fastsettTrygdetidForPeriode(request, grunnlagRolle, kravGjelderUfoeretrygd, sakId)

        request.persongrunnlag?.let {
            it.trygdetid = response.kapittel19
            it.trygdetidKapittel20 = response.kapittel20
        }
    }

    /**
     * NB: Ignoring "force a Kap 19 simulation" for eksterne ordninger
     */
    // VilkarsprovOgBeregnAlderHelper.getSisteAldersberegning
    private fun getSisteAldersberegning(
        kravhode: Kravhode,
        forrigeVedtakListe: List<VilkarsVedtak>,
        forrigeBeregningResultat: AbstraktBeregningsResultat?
    ): SisteBeregning? {
        val originalRegelverkType = kravhode.regelverkTypeEnum

        val sisteBeregning: SisteBeregning? =
            sisteBeregningCreator.opprettSisteBeregning(
                kravhode = kravhode,
                vedtakListe = forrigeVedtakListe,
                beregningResultat = forrigeBeregningResultat
            )

        kravhode.regelverkTypeEnum = originalRegelverkType
        return sisteBeregning
    }

    private companion object {
        private const val GARANTITILLEGG_MAX_ALDER = 67 //TODO normert?

        // VilkarsprovOgBeregnAlderHelper.getAfpLivsvarig
        private fun getPrivatAfp(
            resultatListe: MutableList<BeregningsResultatAfpPrivat>,
            knekkpunktDato: LocalDate
        ): AfpPrivatLivsvarig? =
            findValidForDate(resultatListe, knekkpunktDato)?.privatAfp()

        private fun getLivsvarigOffentligAfp(
            resultatListe: List<LivsvarigOffentligAfpYtelseMedDelingstall>,
            knekkpunktDato: LocalDate
        ): AfpOffentligLivsvarig? =
            resultatListe
                .filter { it.gjelderFom.isBeforeOrOn(knekkpunktDato) }
                .maxByOrNull { it.gjelderFom }
                ?.let {
                    AfpOffentligLivsvarig().apply {
                        bruttoPerAr = it.afpYtelsePerAar
                        uttaksdato = it.gjelderFom
                    }
                }

        private fun vilkaarsproevingSpec(
            livsvarigOffentligAfp: AfpOffentligLivsvarigGrunnlag?,
            privatAfp: AfpPrivatLivsvarig?,
            virkningDato: LocalDate,
            kravhode: Kravhode,
            privatAfpFoersteVirkning: LocalDate?,
            sisteBeregning: SisteBeregning?,
            forrigeVedtakListe: List<VilkarsVedtak>,
            soekerFoersteVirkning: LocalDate,
            avdoedFoersteVirkning: LocalDate?,
            sakId: Long?,
            ignoreAvslag: Boolean
        ) =
            VilkaarsproevingSpec(
                livsvarigOffentligAfp,
                privatAfp,
                virkningDato,
                kravhode,
                privatAfpFoersteVirkning,
                sisteBeregning,
                forrigeVedtakListe,
                soekerFoersteVirkning,
                avdoedFoersteVirkning,
                sakId,
                ignoreAvslag
            )

        // VilkarsprovOgBeregnAlderHelper.setEpsMottarPensjonOnForrigeBeregningsresultat
        private fun setEpsMottarPensjon(
            resultat: AbstraktBeregningsResultat?,
            spec: SimuleringSpec
        ) {
            if (!epsMottarPensjon(spec)) return

            when (resultat) {
                is BeregningsResultatAlderspensjon2011 -> {
                    resultat.beregningsInformasjonKapittel19?.let { it.epsMottarPensjon = true }
                }

                is BeregningsResultatAlderspensjon2016 -> {
                    resultat.beregningsResultat2011?.beregningsInformasjonKapittel19?.let {
                        it.epsMottarPensjon = true
                    }

                    resultat.beregningsResultat2025?.beregningsInformasjonKapittel20?.let {
                        it.epsMottarPensjon = true
                    }
                }

                is BeregningsResultatAlderspensjon2025 -> {
                    resultat.beregningsInformasjonKapittel20?.let { it.epsMottarPensjon = true }
                }
            }
        }

        // PEN: VilkarsprovOgBeregnAlderHelper.setKravlinjeForstevirkFromPersongrunnlag
        private fun setKravlinjeFoersteVirkning(
            vedtakListe: List<VilkarsVedtak>,
            foersteVirkningGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
        ) {
            vedtakListe.forEach {
                setKravlinjeFoersteVirkning(vedtak = it, foersteVirkningGrunnlagListe, KravlinjeTypeEnum.AP)
                setKravlinjeFoersteVirkning(vedtak = it, foersteVirkningGrunnlagListe, KravlinjeTypeEnum.GJR)
            }
        }

        // PEN: VilkarsprovOgBeregnAlderHelper.setKravlinjeForstevirkFromPersongrunnlagForType
        private fun setKravlinjeFoersteVirkning(
            vedtak: VilkarsVedtak,
            foersteVirkningGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>,
            kravlinjeType: KravlinjeTypeEnum
        ) {
            if (vedtak.kravlinjeTypeEnum == kravlinjeType) {
                foersteVirkningGrunnlagListe.firstOrNull { it.kravlinjeTypeEnum == kravlinjeType }?.let {
                    vedtak.kravlinjeForsteVirk = it.virkningsdato
                }
            }
        }

        private fun isCriteriaForDoingKap20ForSimulerForTpFullfilled(
            spec: SimuleringSpec,
            foedselsdato: LocalDate,
            knekkpunktDato: LocalDate
        ): Boolean {
            val heltUttakDato = spec.heltUttakDato

            if (!spec.simulerForTp || heltUttakDato == null) {
                return false
            }

            val erHeltUttakDatoFoerUbetingetPensjoneringDato =
                isBeforeByDay(heltUttakDato, ubetingetPensjoneringDato(foedselsdato), false)
            val erKnekkpunktDatoFoerHeltUttakDato = isBeforeByDay(knekkpunktDato, heltUttakDato, false)
            return erHeltUttakDatoFoerUbetingetPensjoneringDato && erKnekkpunktDatoFoerHeltUttakDato
        }

        private fun shouldAddPensjonBeholdningPerioder(
            hentPensjonBeholdninger: Boolean,
            regelverkType: RegelverkTypeEnum?
        ) =
            hentPensjonBeholdninger &&
                    EnumSet.of(RegelverkTypeEnum.N_REG_N_OPPTJ, RegelverkTypeEnum.N_REG_G_N_OPPTJ)
                        .contains(regelverkType)

        private fun beholdningPeriode(
            virkningFom: LocalDate,
            beholdninger: Beholdninger,
            foedselsdato: LocalDate
        ) =
            BeholdningPeriode(
                datoFom = virkningFom,
                pensjonsbeholdning = beholdninger.beholdning(BeholdningtypeEnum.PEN_B)?.totalbelop,
                garantipensjonsbeholdning = beholdninger.beholdning(BeholdningtypeEnum.GAR_PEN_B)?.totalbelop,
                garantitilleggsbeholdning = garantitilleggBeholdningTotalBeloep(
                    virkningFom,
                    beholdninger,
                    foedselsdato
                ),
                garantipensjonsniva = garantipensjonsniva(beholdninger)
            )

        private fun garantitilleggBeholdningTotalBeloep(
            virkningFom: LocalDate,
            beholdninger: Beholdninger,
            foedselsdato: LocalDate
        ): Double? =
            if (isBeforeByDay(getRelativeDateByYear(foedselsdato, GARANTITILLEGG_MAX_ALDER), virkningFom, false))
                beholdninger.beholdning(BeholdningtypeEnum.GAR_T_B)?.totalbelop
            else
                null

        private fun garantipensjonsniva(beholdninger: Beholdninger): GarantipensjonNivaa? {
            val garantipensjonBeholdning =
                beholdninger.beholdning(BeholdningtypeEnum.GAR_PEN_B) as? Garantipensjonsbeholdning
                    ?: return null

            val justertNivaa = garantipensjonBeholdning.justertGarantipensjonsniva?.garantipensjonsniva ?: return null

            return GarantipensjonNivaa(
                beloep = justertNivaa.belop,
                satsType = (justertNivaa.satsTypeEnum ?: GarantiPensjonsnivaSatsEnum.ORDINAER).name,
                sats = justertNivaa.sats,
                anvendtTrygdetid = justertNivaa.tt_anv
            )
        }

        private fun addAfpBeregningResultat(
            spec: AlderspensjonVilkaarsproeverBeregnerSpec,
            resultatListe: MutableList<BeregningsResultatAfpPrivat>
        ) {
            resultatListe.addAll(spec.afpPrivatBeregningsresultater)
            spec.gjeldendeAfpPrivatBeregningsresultat?.let(resultatListe::add)
        }

        private fun setForrigeBeregningResultVirkningTom(
            forrigeResultat: AbstraktBeregningsResultat,
            knekkpunktDato: LocalDate
        ) {
            forrigeResultat.virkTom = getRelativeDateByDays(knekkpunktDato, -1).toNorwegianDateAtNoon()
        }

        /**
         * Ref. FastsettTrygdetidCache.fastsettTrygdetidInPreg and RequestToReglerMapper.mapToTrygdetidRequest
         */
        private fun trygdetidRequest(
            kravhode: Kravhode,
            persongrunnlag: Persongrunnlag,
            knekkpunktDato: LocalDate,
            soekerForsteVirkningFom: LocalDate,
            ytelseType: KravlinjeTypeEnum,
            boddEllerArbeidetUtenlands: Boolean
        ) =
            TrygdetidRequest().apply {
                this.virkFom = knekkpunktDato.toNorwegianDateAtNoon()
                this.brukerForsteVirk = soekerForsteVirkningFom.toNorwegianDateAtNoon()
                this.hovedKravlinjeType = ytelseType
                this.persongrunnlag = persongrunnlag
                this.boddEllerArbeidetIUtlandet = boddEllerArbeidetUtenlands
                this.regelverkTypeEnum = kravhode.regelverkTypeEnum
                this.uttaksgradListe = kravhode.uttaksgradListe
                // Not set: virkTom, beregningsvilkarPeriodeListe
                // NB: grunnlagsrolle is only used for caching
            }

        private fun findValidForDate(list: MutableList<BeregningsResultatAfpPrivat>, date: LocalDate) =
            list.firstOrNull { isDateInPeriod(date, it.virkFom, it.virkTom) }

        private fun periodiserGrunnlag(kravhode: Kravhode): Kravhode {
            kravhode.persongrunnlagListe.forEach { periodiserDetaljer(it.personDetaljListe) }
            return kravhode
        }

        private fun periodiserDetaljer(detaljListe: MutableList<PersonDetalj>) {
            for (detalj in detaljListe) {
                if (detalj.bruk != true) {
                    detaljListe.remove(detalj)
                }
            }
        }
    }
}
