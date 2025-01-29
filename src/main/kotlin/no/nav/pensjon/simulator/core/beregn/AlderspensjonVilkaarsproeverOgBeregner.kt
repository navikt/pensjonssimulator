package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpYtelseMedDelingstall
import no.nav.pensjon.simulator.core.beholdning.BeholdningType
import no.nav.pensjon.simulator.core.beregn.PeriodiseringUtil.periodiserGrunnlagAndModifyKravhode
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Garantipensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.to.*
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsprovAlderspensjonResultat
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktAarsak
import no.nav.pensjon.simulator.core.knekkpunkt.TrygdetidFastsetter
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.ubetingetPensjoneringDato
import no.nav.pensjon.simulator.core.util.isBeforeOrOn
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.core.vilkaar.Vilkaarsproever
import no.nav.pensjon.simulator.core.vilkaar.VilkaarsproevingSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
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
    private val vilkaarsproever: Vilkaarsproever,
    private val trygdetidFastsetter: TrygdetidFastsetter,
    private val sisteBeregningCreator: SisteBeregningCreator,
    private val generelleDataHolder: GenerelleDataHolder
) {
    fun vilkaarsproevOgBeregnAlder(spec: AlderspensjonVilkaarsproeverBeregnerSpec): AlderspensjonBeregnerResult {
        val beregningResultatListe: MutableList<AbstraktBeregningsResultat> = mutableListOf()
        val knekkpunkter = spec.knekkpunkter
        val simuleringSpec = spec.simulering
        val soekerFoersteVirkning = spec.sokerForsteVirk
        val avdoedFoersteVirkning = spec.avdodForsteVirk
        var forrigeVedtakListe = spec.forrigeVilkarsvedtakListe
        var forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat? = spec.forrigeAlderBeregningsresultat
        var sisteBeregning = spec.sisteBeregning
        var kravhode = periodiserGrunnlag(spec.kravhode)
        val soekerGrunnlag = kravhode.hentPersongrunnlagForRolle(rolle = GrunnlagsrolleEnum.SOKER, checkBruk = false)!!
        val avdoedGrunnlag = kravhode.hentPersongrunnlagForRolle(rolle = GrunnlagsrolleEnum.AVDOD, checkBruk = false)
        var vedtakListe: List<VilkarsVedtak>
        val garantitilleggBeholdningGrunnlag = hentGarantiTilleggsbeholdningGrunnlag()
        var sisteBeregning2011Tp: SisteBeregning? = null
        val loependeAlderspensjonBeregningResultat = forrigeAlderspensjonBeregningResultat
        val privatAfpBeregningResultatListe =
            mutableListOf<BeregningsResultatAfpPrivat>().also { addAfpBeregningResultat(spec, it) }
        val pensjonBeholdningPeriodeListe: MutableList<BeholdningPeriode> = mutableListOf()
        val vedtakListeAllePerioder: MutableList<VilkarsVedtak> = mutableListOf()

        vedtakListeAllePerioder.addAll(forrigeVedtakListe)

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
            val forholdstallUtvalg = generelleDataHolder.getForholdstallUtvalg(knekkpunktDato, foedselsdato)
            val delingstallUtvalg = generelleDataHolder.getDelingstallUtvalg(knekkpunktDato, foedselsdato)

            // Corresponds to part 4
            val gjeldendePrivatAfp = getAfpPrivatLivsvarig(privatAfpBeregningResultatListe, knekkpunktDato)
            //val gjeldendeLivsvarigOffentligAfp: AfpOffentligLivsvarigGrunnlag? =
            val gjeldendeLivsvarigOffentligAfp: AfpOffentligLivsvarigDto? =
                getLivsvarigOffentligAfp(spec.afpOffentligLivsvarigBeregningsresultat?.afpYtelseListe, knekkpunktDato)
            val gjeldendeLivsvarigAfp = gjeldendePrivatAfp ?: gjeldendeLivsvarigOffentligAfp?.toAfpLivsvarig()

            // Corresponds to part 5
            if (aarsaker.contains(KnekkpunktAarsak.UTG)) {
                // CR195877 21.03.2011 OJB2812: When simulation is called from eksterne ordninger for a 2025-bruker, then we
                // should force a Kap 19 simulation even for pure Kap 20 brukere (2025) since eksterne ordninger currently
                // aren't able to parse a Kap 20 result. But - the vilkårsprøving must be done with a 2025-beregning. This
                // "hack" should be removed as soon as they're able to receive Kap 20 results.
                val sisteBeregningForVilkarsproving = sisteBeregning2011Tp ?: sisteBeregning

                val vilkaarsproevingSpec = vilkaarsproevingSpec(
                    gjeldendeLivsvarigOffentligAfp?.toVilkarsprovDto(),
                    gjeldendePrivatAfp,
                    knekkpunktDato,
                    kravhode,
                    spec.forsteVirkAfpPrivat,
                    forholdstallUtvalg,
                    delingstallUtvalg,
                    sisteBeregningForVilkarsproving,
                    forrigeVedtakListe,
                    garantitilleggBeholdningGrunnlag,
                    soekerFoersteVirkning,
                    avdoedFoersteVirkning,
                    spec.sakId,
                    spec.ignoreAvslag
                )

                val vilkaarsproevingResult = vilkaarsproever.vilkaarsproevKrav(vilkaarsproevingSpec)
                vedtakListe = vilkaarsproevingResult.first
                kravhode = vilkaarsproevingResult.second
                forrigeVedtakListe = vedtakListe
                vedtakListeAllePerioder.addAll(forrigeVedtakListe)
            }

            // Når vi kommer fra PSELV skal flagget settes i henhold til brukers løpende pensjon (ref. PK-15060)
            if (!simuleringSpec.isTpOrigSimulering) {
                forrigeAlderspensjonBeregningResultat?.let {
                    if (it.epsMottarPensjon) {
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
            val gjeldendeBeregningsresultat = beregnAlderspensjon(
                kravhode,
                forrigeVedtakListe,
                knekkpunktDato,
                forholdstallUtvalg,
                delingstallUtvalg,
                sisteBeregning,
                gjeldendeLivsvarigAfp,
                garantitilleggBeholdningGrunnlag,
                simuleringSpec,
                spec.sakId,
                isFoersteUttak = sisteBeregning == null,
                spec.ignoreAvslag
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

                val gjeldendeBeregningsresultatTp = beregnAlderspensjon(
                    kravhode = kravhode,
                    vedtakListe = forrigeVedtakListe,
                    virkningDato = knekkpunktDato,
                    forholdstallUtvalg = forholdstallUtvalg,
                    delingstallUtvalg = delingstallUtvalg,
                    sisteAldersberegning2011 = sisteBeregning, // <---- NB: sisteBeregning2011Tp in VilkarsprovOgBeregnAlderHelper
                    privatAfp = gjeldendeLivsvarigAfp,
                    garantitilleggBeholdningGrunnlag = garantitilleggBeholdningGrunnlag,
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

                val beregningResultat = beregnAlderspensjon(
                    kravhode = folketrygdBeholdningKravhode,
                    vedtakListe = mutableListOf(vilkarsvedtak),
                    virkningDato = knekkpunktDato,
                    forholdstallUtvalg = forholdstallUtvalg,
                    delingstallUtvalg = delingstallUtvalg,
                    sisteAldersberegning2011 = null, // null in legacy
                    privatAfp = gjeldendeLivsvarigAfp,
                    garantitilleggBeholdningGrunnlag = garantitilleggBeholdningGrunnlag,
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

    private fun hentGarantiTilleggsbeholdningGrunnlag(): GarantitilleggsbeholdningGrunnlag {
        val virkningDato = LocalDate.of(GARANTITILLEGGSBEHOLDNINGSGRUNNLAG_FODSELSAR + GARANTITILLEGG_MAX_ALDER, 2, 1)
        val foedselDato = LocalDate.of(GARANTITILLEGGSBEHOLDNINGSGRUNNLAG_FODSELSAR, 1, 1)

        return GarantitilleggsbeholdningGrunnlag().apply {
            //dt67_1962 = context.fetchDelingstallUtvalg(virkningDato, foedselDato).dt
            dt67_1962 = generelleDataHolder.getDelingstallUtvalg(virkningDato, foedselDato).dt
            ft67_1962 = generelleDataHolder.getForholdstallUtvalg(virkningDato, foedselDato).ft
            //ft67_1962 = context.fetchForholdstallUtvalg(virkningDato, foedselDato).ft
        }
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

    // VilkarsprovOgBeregnAlderHelper.beregnAP
    private fun beregnAlderspensjon(
        kravhode: Kravhode,
        vedtakListe: MutableList<VilkarsVedtak>,
        virkningDato: LocalDate,
        forholdstallUtvalg: ForholdstallUtvalg,
        delingstallUtvalg: DelingstallUtvalg,
        sisteAldersberegning2011: SisteBeregning?,
        privatAfp: AfpLivsvarig?,
        garantitilleggBeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag,
        simuleringSpec: SimuleringSpec,
        sakId: Long?,
        isFoersteUttak: Boolean,
        ignoreAvslag: Boolean
    ): AbstraktBeregningsResultat =
        if (isFoersteUttak) {
            val request = beregningCommonSpec(
                kravhode, vedtakListe, virkningDato, forholdstallUtvalg, delingstallUtvalg,
                privatAfp, garantitilleggBeholdningGrunnlag, simuleringSpec
            )

            beregnFoersteUttak(request, sakId, ignoreAvslag)
        } else {
            val request = revurderingCommonSpec(
                kravhode, vedtakListe, virkningDato, forholdstallUtvalg, delingstallUtvalg,
                sisteAldersberegning2011!!, privatAfp, garantitilleggBeholdningGrunnlag, simuleringSpec
            )

            beregnRevurdering(request, sakId)
        }

    // VilkarsprovOgBeregnAlderHelper.beregnForsteUttak
    //@Throws(PEN222BeregningstjenesteFeiletException::class)
    private fun beregnFoersteUttak(
        spec: AlderspensjonBeregningCommonSpec,
        sakId: Long?,
        ignoreAvslag: Boolean
    ): AbstraktBeregningsResultat {
        val regelverkType: RegelverkTypeEnum =
            spec.kravhode?.regelverkTypeEnum ?: throw RuntimeException("Undefined regelverkTypeEnum")

        // SIMDOM-ADD for 'simuler folketrygdbeholdning' (da avslag ignoreres):
        if (ignoreAvslag && spec.vilkarsvedtakListe.any { it.anbefaltResultatEnum != innvilgetResultat }) {
            spec.vilkarsvedtakListe.replaceAll(::innvilgetVedtak)
        }
        // end SIMDOM-ADD

        return when (regelverkType) {
            RegelverkTypeEnum.N_REG_G_OPPTJ ->
                context.beregnAlderspensjon2011FoersteUttak(beregning2011Request(spec), sakId)

            RegelverkTypeEnum.N_REG_G_N_OPPTJ ->
                context.beregnAlderspensjon2016FoersteUttak(beregning2016Request(spec), sakId)

            RegelverkTypeEnum.N_REG_N_OPPTJ ->
                // NB: No special handling for eksterne ordninger (tjenestepensjonsleverandører)
                context.beregnAlderspensjon2025FoersteUttak(beregning2025Request(spec), sakId)

            else -> throw RuntimeException("Unexpected regelverkType: $regelverkType")
        }
    }

    // VilkarsprovOgBeregnAlderHelper.beregnRevurdering
    private fun beregnRevurdering(
        spec: AlderspensjonRevurderingCommonSpec,
        sakId: Long?
    ): AbstraktBeregningsResultat {
        val regelverkType: RegelverkTypeEnum =
            spec.kravhode?.regelverkTypeEnum ?: throw RuntimeException("Undefined regelverkTypeEnum")

        return when (regelverkType) {
            RegelverkTypeEnum.N_REG_G_OPPTJ ->
                context.revurderAlderspensjon2011(revurdering2011Request(spec), sakId)

            RegelverkTypeEnum.N_REG_G_N_OPPTJ ->
                context.revurderAlderspensjon2016(revurdering2016Request(spec), sakId)

            RegelverkTypeEnum.N_REG_N_OPPTJ ->
                // NB: No special handling for eksterne ordninger (tjenestepensjonsleverandører)
                context.revurderAlderspensjon2025(revurdering2025Request(spec), sakId)

            else -> throw RuntimeException("Unexpected regelverkType: $regelverkType")
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
        private const val GARANTITILLEGG_MAX_ALDER = 67
        private const val GARANTITILLEGGSBEHOLDNINGSGRUNNLAG_FODSELSAR = 1962
        private const val EPS_PEN_PERSON_ID = -2L
        private val innvilgetResultat = VedtakResultatEnum.INNV

        // VilkarsprovOgBeregnAlderHelper.getAfpLivsvarig
        private fun getAfpPrivatLivsvarig(
            resultatListe: MutableList<BeregningsResultatAfpPrivat>,
            knekkpunktDato: LocalDate
        ): AfpLivsvarig? =
            findValidForDate(resultatListe, knekkpunktDato)?.hentLivsvarigDelIBruk()

        private fun getLivsvarigOffentligAfp(
            resultatListe: List<LivsvarigOffentligAfpYtelseMedDelingstall>?,
            knekkpunktDato: LocalDate
            //): AfpOffentligLivsvarigGrunnlag? =
        ): AfpOffentligLivsvarigDto? =
            resultatListe
                ?.filter { it.gjelderFom.isBeforeOrOn(knekkpunktDato) }
                ?.maxByOrNull { it.gjelderFom }
                //?.let { AfpOffentligLivsvarigGrunnlag(sistRegulertG = 0, bruttoPerAr = it.afpYtelsePerAar) }
                ?.let {
                    AfpOffentligLivsvarigDto(bruttoPerAr = it.afpYtelsePerAar).apply {
                        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.AFP_OFFENTLIG_LIVSVARIG
                    }
                }

        private fun vilkaarsproevingSpec(
            livsvarigOffentligAfp: AfpOffentligLivsvarigGrunnlag?,
            privatAfp: AfpLivsvarig?,
            virkningDato: LocalDate,
            kravhode: Kravhode,
            privatAfpFoersteVirkning: LocalDate?,
            forholdstallUtvalg: ForholdstallUtvalg,
            delingstallUtvalg: DelingstallUtvalg,
            sisteBeregning: SisteBeregning?,
            forrigeVedtakListe: List<VilkarsVedtak>,
            garantitilleggBeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag,
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
                forholdstallUtvalg,
                delingstallUtvalg,
                sisteBeregning,
                forrigeVedtakListe,
                garantitilleggBeholdningGrunnlag,
                soekerFoersteVirkning,
                avdoedFoersteVirkning,
                sakId,
                ignoreAvslag
            )

        // PEN: VilkarsprovOgBeregnAlderHelper.createInfoPavirkendeYtelse
        private fun paavirkendeYtelseInfo(virkningDato: LocalDate, kravhode: Kravhode): InfoPavirkendeYtelse {
            var epsGrunnlag = kravhode.findPersongrunnlag(PenPerson(EPS_PEN_PERSON_ID))
            val grunnlagRoller =
                EnumSet.of(GrunnlagsrolleEnum.EKTEF, GrunnlagsrolleEnum.SAMBO, GrunnlagsrolleEnum.PARTNER)

            // Hvis bruker har en "riktig" EPS på sitt persongrunnlag hentes denne
            if (epsGrunnlag == null) {
                var grunnlagsrolle: GrunnlagsrolleEnum? = null

                for (persongrunnlag in kravhode.persongrunnlagListe) {
                    for (persondetalj in persongrunnlag.personDetaljListe) {
                        val rolle = persondetalj.grunnlagsrolleEnum

                        if (grunnlagRoller.contains(rolle)) {
                            grunnlagsrolle = rolle
                        }
                    }
                }

                epsGrunnlag = grunnlagsrolle?.let { kravhode.hentPersongrunnlagForRolle(it, false) }
            }

            if (epsGrunnlag == null) {
                return InfoPavirkendeYtelse()
            }

            val kravlinje = alderspensjonKravlinje(epsGrunnlag.penPerson!!)
            val vedtak = innvilgetVedtak(kravlinje, virkningDato)
            return InfoPavirkendeYtelse().also { it.vilkarsvedtakEPSListe.add(vedtak) }
        }

        // VilkarsprovOgBeregnAlderHelper.createFerdigApKravlinje
        private fun alderspensjonKravlinje(gjelderPerson: PenPerson) =
            Kravlinje().apply {
                kravlinjeTypeEnum = KravlinjeTypeEnum.AP
                relatertPerson = gjelderPerson
                // NB apparently not used (no kravlinjeStatus field): setKravlinjeStatus(KravlinjeStatus.FERDIG)
            }

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

        // VilkarsprovOgBeregnAlderHelper.isEktefelleMottarPensjon
        private fun epsMottarPensjon(spec: SimuleringSpec): Boolean {
            if (isAlderWithGjenlevende(spec)) {
                return false
            }

            // The sivilstandsjekk is needed because ESB default sets epsPensjon to true when a samhandler is calling the simulering service.
            return if (EnumSet.of(SivilstatusType.SAMB, SivilstatusType.GIFT, SivilstatusType.REPA)
                    .contains(spec.sivilstatus)
            )
                spec.epsHarPensjon
            else
                false
        }

        private fun isAlderWithGjenlevende(spec: SimuleringSpec): Boolean =
            EnumSet.of(SimuleringType.ALDER_M_GJEN, SimuleringType.ENDR_ALDER_M_GJEN).contains(spec.type)

        private fun isCriteriaForDoingKap20ForSimulerForTpFullfilled(
            spec: SimuleringSpec,
            foedselDato: LocalDate,
            knekkpunktDato: LocalDate
        ): Boolean {
            val heltUttakDato = spec.heltUttakDato

            if (!spec.simulerForTp || heltUttakDato == null) {
                return false
            }

            val erHeltUttakDatoFoerUbetingetPensjoneringDato =
                isBeforeByDay(heltUttakDato, ubetingetPensjoneringDato(foedselDato), false)
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
            foedselDato: LocalDate
        ) =
            BeholdningPeriode(
                datoFom = virkningFom,
                pensjonsbeholdning = beholdninger.findBeholdningAvType(BeholdningType.PEN_B)?.totalbelop,
                garantipensjonsbeholdning = beholdninger.findBeholdningAvType(BeholdningType.GAR_PEN_B)?.totalbelop,
                garantitilleggsbeholdning = garantitilleggBeholdningTotalBeloep(virkningFom, beholdninger, foedselDato),
                garantipensjonsniva = garantipensjonsniva(beholdninger)
            )

        private fun garantitilleggBeholdningTotalBeloep(
            virkningFom: LocalDate,
            beholdninger: Beholdninger,
            foedselDato: LocalDate
        ): Double? =
            if (isBeforeByDay(getRelativeDateByYear(foedselDato, GARANTITILLEGG_MAX_ALDER), virkningFom, false))
                beholdninger.findBeholdningAvType(BeholdningType.GAR_T_B)?.totalbelop
            else
                null

        private fun garantipensjonsniva(beholdninger: Beholdninger): GarantipensjonNivaa? {
            val garantipensjonBeholdning =
                beholdninger.findBeholdningAvType(BeholdningType.GAR_PEN_B) as? Garantipensjonsbeholdning
                    ?: return null

            val justertNivaa = garantipensjonBeholdning.justertGarantipensjonsniva?.garantipensjonsniva ?: return null

            return GarantipensjonNivaa(
                beloep = justertNivaa.belop,
                satsType = justertNivaa.satsType!!.kode,
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
                this.ytelsesTypeEnum = ytelseType
                this.persongrunnlag = persongrunnlag
                this.boddEllerArbeidetIUtlandet = boddEllerArbeidetUtenlands
                this.regelverkTypeEnum = kravhode.regelverkTypeEnum
                this.uttaksgradListe = kravhode.uttaksgradListe
                // Not set: virkTom, beregningsvilkarPeriodeListe
                // NB: grunnlagsrolle is only used for caching
            }

        // VilkarsprovOgBeregnAlderHelper.createInnvilgetVilkarsvedtak
        private fun innvilgetVedtak(kravlinje: Kravlinje, virkningFom: LocalDate): VilkarsVedtak {
            val vedtakResultat = VedtakResultatEnum.INNV

            return VilkarsVedtak().apply {
                this.anbefaltResultatEnum = vedtakResultat
                this.vilkarsvedtakResultatEnum = vedtakResultat
                this.virkFom = virkningFom.toNorwegianDateAtNoon()
                this.virkTom = null
                this.kravlinje = kravlinje
                this.kravlinjeTypeEnum = kravlinje.kravlinjeTypeEnum
                this.penPerson = kravlinje.relatertPerson
                this.forsteVirk = virkningFom.toNorwegianDateAtNoon()
            }.also { it.finishInit() }
        }

        // SIMDOM-ADD
        private fun innvilgetVedtak(source: VilkarsVedtak) =
            /* TODO check if copy needed:
            VilkarsVedtak(source).also {
                it.anbefaltResultatEnum = innvilgetResultat
                it.vilkarsvedtakResultatEnum = innvilgetResultat
                it.begrunnelseEnum = null
                it.merknadListe = mutableListOf()
            }*/
            source.apply {
                anbefaltResultatEnum = innvilgetResultat
                vilkarsvedtakResultatEnum = innvilgetResultat
                begrunnelseEnum = null
                merknadListe = mutableListOf()
            } //.also { log.warn { "MUTATED VilkarsVedtak - innvilget" } }

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

        // PEN: VilkarsprovOgBeregnAlderHelper.buildBeregnApRequest
        private fun beregningCommonSpec(
            kravhode: Kravhode,
            vedtakListe: MutableList<VilkarsVedtak>,
            virkningFom: LocalDate,
            forholdstallUtvalg: ForholdstallUtvalg,
            delingstallUtvalg: DelingstallUtvalg,
            livsvarigAfp: AfpLivsvarig?,
            garantitilleggBeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag,
            simuleringSpec: SimuleringSpec
        ): AlderspensjonBeregningCommonSpec {
            val epsMottarPensjon = epsMottarPensjon(simuleringSpec)
            val paavirkendeYtelseInfo = if (epsMottarPensjon) paavirkendeYtelseInfo(virkningFom, kravhode) else null

            return AlderspensjonBeregningCommonSpec(
                kravhode = kravhode,
                vilkarsvedtakListe = vedtakListe,
                infoPavirkendeYtelse = paavirkendeYtelseInfo,
                virkFom = virkningFom,
                forholdstallUtvalg = forholdstallUtvalg,
                delingstallUtvalg = delingstallUtvalg,
                epsMottarPensjon = epsMottarPensjon,
                afpLivsvarig = livsvarigAfp,
                garantitilleggsbeholdningGrunnlag = garantitilleggBeholdningGrunnlag
            )
        }

        private fun beregning2011Request(spec: AlderspensjonBeregningCommonSpec) =
            BeregnAlderspensjon2011ForsteUttakRequest().apply {
                kravhode = spec.kravhode
                vilkarsvedtakListe = spec.vilkarsvedtakListe
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                virkTom = null // set to null in legacy SimuleringEtter2011Context.beregnAlderspensjon2011ForsteUttak
                forholdstallUtvalg = spec.forholdstallUtvalg
                ektefellenMottarPensjon = spec.epsMottarPensjon
                afpLivsvarig = spec.afpLivsvarig
            }

        private fun beregning2016Request(spec: AlderspensjonBeregningCommonSpec) =
            BeregnAlderspensjon2016ForsteUttakRequest().apply {
                kravhode = spec.kravhode
                vilkarsvedtakListe = spec.vilkarsvedtakListe
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                forholdstallUtvalg = spec.forholdstallUtvalg
                delingstallUtvalg = spec.delingstallUtvalg
                epsMottarPensjon = spec.epsMottarPensjon
                afpLivsvarig = spec.afpLivsvarig
                garantitilleggsbeholdningGrunnlag = spec.garantitilleggsbeholdningGrunnlag
            }

        private fun beregning2025Request(spec: AlderspensjonBeregningCommonSpec) =
            BeregnAlderspensjon2025ForsteUttakRequest().apply {
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                kravhode = spec.kravhode
                vilkarsvedtakListe = spec.vilkarsvedtakListe
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                forholdstallUtvalg = spec.forholdstallUtvalg
                delingstallUtvalg = spec.delingstallUtvalg
                epsMottarPensjon = spec.epsMottarPensjon
                afpLivsvarig = spec.afpLivsvarig
                garantitilleggsbeholdningGrunnlag = spec.garantitilleggsbeholdningGrunnlag
            }

        private fun revurderingCommonSpec(
            kravhode: Kravhode,
            vedtakListe: MutableList<VilkarsVedtak>,
            virkningFom: LocalDate,
            forholdstallUtvalg: ForholdstallUtvalg,
            delingstallUtvalg: DelingstallUtvalg,
            sisteAlderspensjonBeregning2011: SisteBeregning,
            livsvarigAfp: AfpLivsvarig?,
            garantitilleggBeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag,
            simuleringSpec: SimuleringSpec
        ): AlderspensjonRevurderingCommonSpec {
            val epsMottarPensjon = epsMottarPensjon(simuleringSpec)
            val paavirkendeYtelseInfo = if (epsMottarPensjon) paavirkendeYtelseInfo(virkningFom, kravhode) else null

            return AlderspensjonRevurderingCommonSpec(
                kravhode = kravhode,
                vilkarsvedtakListe = vedtakListe,
                infoPavirkendeYtelse = paavirkendeYtelseInfo,
                epsMottarPensjon = epsMottarPensjon,
                forholdstallUtvalg = forholdstallUtvalg,
                delingstallUtvalg = delingstallUtvalg,
                virkFom = virkningFom,
                forrigeAldersberegning = sisteAlderspensjonBeregning2011,
                afpLivsvarig = livsvarigAfp,
                garantitilleggsbeholdningGrunnlag = garantitilleggBeholdningGrunnlag
            )
        }

        private fun revurdering2011Request(spec: AlderspensjonRevurderingCommonSpec) =
            RevurderingAlderspensjon2011Request().apply {
                kravhode = spec.kravhode
                vilkarsvedtakListe = Vector(spec.vilkarsvedtakListe)
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                epsMottarPensjon = spec.epsMottarPensjon
                forholdstallUtvalg = spec.forholdstallUtvalg
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                virkTom = null
                forrigeAldersBeregning = spec.forrigeAldersberegning as? SisteAldersberegning2011
                afpLivsvarig = spec.afpLivsvarig
            }

        private fun revurdering2016Request(spec: AlderspensjonRevurderingCommonSpec) =
            RevurderingAlderspensjon2016Request().apply {
                kravhode = spec.kravhode
                vilkarsvedtakListe = ArrayList(spec.vilkarsvedtakListe)
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                epsMottarPensjon = spec.epsMottarPensjon
                forholdstallUtvalg = spec.forholdstallUtvalg
                delingstallUtvalg = spec.delingstallUtvalg
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                forrigeAldersBeregning = spec.forrigeAldersberegning as? SisteAldersberegning2016
                afpLivsvarig = spec.afpLivsvarig
                garantitilleggsbeholdningGrunnlag = spec.garantitilleggsbeholdningGrunnlag
            }.also {
                it.vilkarsvedtakListe.forEach(::prepareVedtak2016ForReglerCall)
                it.forrigeAldersBeregning?.let(::clearFormelMaps)
            }

        private fun revurdering2025Request(spec: AlderspensjonRevurderingCommonSpec) =
            RevurderingAlderspensjon2025Request().apply {
                kravhode = spec.kravhode
                vilkarsvedtakListe = ArrayList(spec.vilkarsvedtakListe)
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                epsMottarPensjon = spec.epsMottarPensjon
                forholdstallUtvalg = spec.forholdstallUtvalg
                delingstallUtvalg = spec.delingstallUtvalg
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                sisteAldersBeregning2011 = spec.forrigeAldersberegning as? SisteAldersberegning2011 // NB: 2011
                afpLivsvarig = spec.afpLivsvarig
                garantitilleggsbeholdningGrunnlag = spec.garantitilleggsbeholdningGrunnlag
            }.also {
                it.vilkarsvedtakListe.forEach(::prepareVedtak2025ForReglerCall)
                it.sisteAldersBeregning2011?.let(::prepareBeregningForReglerCall)
            }

        private fun prepareBeregningForReglerCall(beregning: SisteAldersberegning2011) {
            with(beregning) {
                pensjonUnderUtbetaling?.ytelseskomponenter?.forEach(Ytelseskomponent::roundNettoPerAr)
                beholdninger?.beholdninger?.forEach(::prepareBeholdningForReglerCall)
            }
        }

        private fun prepareBeholdningForReglerCall(beholdning: Beholdning) {
            when (beholdning) {
                is Pensjonsbeholdning -> beholdning.opptjening?.finishInit()
                is Garantipensjonsbeholdning -> beholdning.clearPensjonsbeholdning()
            }
        }

        private fun prepareVedtak2016ForReglerCall(vedtak: VilkarsVedtak) {
            with(vedtak) {
                finishInit()
                val alderspensjonVilkaarResultat = vilkarsprovresultat as? VilkarsprovAlderspensjonResultat ?: return

                alderspensjonVilkaarResultat.vilkarsprovInformasjon?.pensjonVedUttak?.ytelseskomponenter?.forEach(
                    Ytelseskomponent::roundNettoPerAr
                )

                val beregningResultat =
                    alderspensjonVilkaarResultat.beregningVedUttak as? BeregningsResultatAlderspensjon2016 ?: return

                with(beregningResultat) {
                    pensjonUnderUtbetaling?.ytelseskomponenter?.forEach(Ytelseskomponent::roundNettoPerAr)
                }
            }
        }

        private fun prepareVedtak2025ForReglerCall(vedtak: VilkarsVedtak) {
            with(vedtak) {
                finishInit()
                val alderspensjonVilkaarResultat = vilkarsprovresultat as? VilkarsprovAlderspensjonResultat ?: return

                alderspensjonVilkaarResultat.vilkarsprovInformasjon?.pensjonVedUttak?.ytelseskomponenter?.forEach(
                    Ytelseskomponent::roundNettoPerAr
                )

                val beregningResultat =
                    alderspensjonVilkaarResultat.beregningVedUttak as? BeregningsResultatAlderspensjon2025 ?: return

                with(beregningResultat) {
                    pensjonUnderUtbetaling?.ytelseskomponenter?.forEach(Ytelseskomponent::roundNettoPerAr)
                    beregningKapittel20?.beholdninger?.beholdninger?.forEach(::prepareBeholdningForReglerCall)
                    beregningKapittel20?.beholdningerForForsteuttak?.beholdninger?.forEach(::prepareBeholdningForReglerCall)
                    beregningsInformasjonKapittel20?.clearDelingstall()
                }
            }
        }

        private fun clearFormelMaps(beregning: SisteAldersberegning2016) {
            beregning.basispensjon?.let(::clearFormelMap)
            beregning.restpensjon?.let(::clearFormelMap)
            /* TODO check if relevant:
            beregning.pensjonUnderUtbetaling?.let(::clearFormelMaps)
            beregning.pensjonUnderUtbetaling2011?.let(::clearFormelMaps)
            beregning.pensjonUnderUtbetaling2011UtenGJR?.let(::clearFormelMaps)
            beregning.pensjonUnderUtbetaling2025?.let(::clearFormelMaps)
            beregning.pensjonUnderUtbetaling2025AltKonv?.let(::clearFormelMaps)*/
        }
        /* TODO: it as? FormelProvider = null always?
        private fun clearFormelMaps(pensjon: PensjonUnderUtbetaling) {
            pensjon.ytelseskomponenter.mapNotNull { it as? FormelProvider }.forEach(::clearFormelMap)
        }*/

        private fun clearFormelMap(pensjon: Basispensjon) {
            pensjon.tp?.formelMap?.clear()
        }
    }
}

private fun BeregningsResultatAfpPrivat.hentLivsvarigDelIBruk() =
    pensjonUnderUtbetaling?.ytelseskomponenter?.firstOrNull {
        it.ytelsekomponentTypeEnum == YtelseskomponentTypeEnum.AFP_LIVSVARIG && it is AfpLivsvarig
    } as AfpLivsvarig?

private fun Beholdninger.findBeholdningAvType(type: BeholdningType) =
    beholdninger.firstOrNull { type.name == it.beholdningsType?.kode }
