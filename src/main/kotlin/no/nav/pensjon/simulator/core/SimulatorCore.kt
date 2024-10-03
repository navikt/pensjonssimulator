package no.nav.pensjon.simulator.core

import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpPeriodeConverter
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpResult
import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpBeregner
import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpResult
import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSpec
import no.nav.pensjon.simulator.core.anonym.AnonymOutputMapper
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.beregn.AlderspensjonBeregnerResult
import no.nav.pensjon.simulator.core.beregn.AlderspensjonVilkaarsproeverBeregnerSpec
import no.nav.pensjon.simulator.core.beregn.AlderspensjonVilkaarsproeverOgBeregner
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktAarsak
import no.nav.pensjon.simulator.core.krav.KravGjelder
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.BeregningsmotorValidereException
import no.nav.pensjon.simulator.core.exception.ForLavtTidligUttakException
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktFinder
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktSpec
import no.nav.pensjon.simulator.core.knekkpunkt.TrygdetidFastsetter
import no.nav.pensjon.simulator.core.krav.KravhodeCreator
import no.nav.pensjon.simulator.core.krav.KravhodeSpec
import no.nav.pensjon.simulator.core.krav.KravhodeUpdateSpec
import no.nav.pensjon.simulator.core.krav.KravhodeUpdater
import no.nav.pensjon.simulator.core.out.OutputPensjonCombo
import no.nav.pensjon.simulator.core.result.ResultPreparerSpec
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimuleringResultPreparer
import no.nav.pensjon.simulator.core.trygd.ForKortTrygdetidException
import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoRepopulator
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelseGetter
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelseResult
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.Pid
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.System.currentTimeMillis
import java.time.LocalDate
import java.util.*

/**
 * Corresponds to AbstraktSimulerAPFra2011Command, SimulerFleksibelAPCommand, SimulerAFPogAPCommand, SimulerEndringAvAPCommand
 */
@Component
class SimulatorCore(
    private val context: SimulatorContext,
    private val kravhodeCreator: KravhodeCreator,
    private val kravhodeUpdater: KravhodeUpdater,
    private val knekkpunktFinder: KnekkpunktFinder,
    private val alderspensjonVilkaarsproeverOgBeregner: AlderspensjonVilkaarsproeverOgBeregner,
    private val privatAfpBeregner: PrivatAfpBeregner,
    private val generelleDataHolder: GenerelleDataHolder
) : UttakAlderDiscriminator {

    private val logger = LoggerFactory.getLogger(SimulatorCore::class.java)

    // AbstraktSimulerAPFra2011Command.execute + overrides in SimulerFleksibelAPCommand & SimulerAFPogAPCommand & SimulerEndringAvAPCommand
    @Throws(
        BeregningsmotorValidereException::class,
        ForKortTrygdetidException::class,
        ForLavtTidligUttakException::class
    )
    override fun simuler(spec: SimuleringSpec, flags: SimulatorFlags): SimulatorOutput {
        /*ANON
        val gjelderEndring = simulatorInput.gjelderEndring()

        if (gjelderEndring) {
            EndringValidator.validate(simulatorInput)
        }
        */

        val grunnbeloep: Int = fetchGrunnbeloep()

        logger.info("Simulator steg 1 - Hent løpende ytelser")

        //val personVirkningsdatoCombo: FoersteVirkningDatoCombo? = simulatorInput.pid?.let(context::fetchPersonVirkningsdatoCombo) // null if forenklet simulering
        val personVirkningDatoCombo: FoersteVirkningDatoCombo? = null
        val person: PenPerson? = null //ANON personVirkningsdatoCombo?.person
        val foedselDato: LocalDate? = null //ANON person?.let { toLocalDate(it.foedselsdato) }
        val ytelser: LoependeYtelser =
            fetchLoependeYtelser(spec, personVirkningDatoCombo?.foersteVirkningDatoListe.orEmpty())

        /*ANON
        if (gjelderEndring) {
            EndringValidator.validateRequestBasedOnLopendeYtelser(simulatorInput, ytelser.forrigeAlderspensjonBeregningsresultat)
        }
        */

        logger.info("Simulator steg 2 - Opprett kravhode")

        var kravhode: Kravhode = opprettKravhode(
            spec = KravhodeSpec(
                simulatorInput = spec,
                forrigeAlderspensjonBeregningResult = ytelser.forrigeAlderspensjonBeregningResultat,
                grunnbeloep = grunnbeloep
            ),
            person = person,
            virkningDatoGrunnlagListe = personVirkningDatoCombo?.foersteVirkningDatoGrunnlagListe.orEmpty()
        )

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        logger.info("Simulator steg 3 - Beregn AFP Privat")

        var privatAfpBeregningResultatListe: MutableList<BeregningsResultatAfpPrivat> = mutableListOf()
        var gjeldendePrivatAfpBeregningResultat: BeregningsResultatAfpPrivat? = null

        if (ytelser.privatAfpVirkningFom != null) {
            val response = beregnPrivatAfp(
                PrivatAfpSpec(
                    simulering = spec,
                    kravhode = kravhode,
                    virkningFom = ytelser.privatAfpVirkningFom,
                    forrigePrivatAfpBeregningResult = null, // forrigeAfpPrivatBeregningsresultat is null for SimulerFleksibelAPCommand & SimulerAFPogAPCommand (ref. line 129 in SimulerAFPogAPCommand)
                    gjelderOmsorg = kravhode.hentPersongrunnlagForSoker().gjelderOmsorg,
                    sakId = kravhode.sakId
                )
            )

            gjeldendePrivatAfpBeregningResultat = response.gjeldendeBeregningsresultatAfpPrivat
            privatAfpBeregningResultatListe = response.afpPrivatBeregningsresultatListe
        }

        logger.info("Simulator steg 4 - Oppdater kravhode før første knekkpunkt")

        kravhode = oppdaterKravhodeForFoersteKnekkpunkt(
            KravhodeUpdateSpec(
                kravhode = kravhode,
                simulering = spec,
                forrigeAlderspensjonBeregningResult = ytelser.forrigeAlderspensjonBeregningResultat
            )
        )

        logger.info("Simulator steg 5 - Finn knekkpunkter")

        val knekkpunktMap = finnKnekkpunkter(
            KnekkpunktSpec(
                kravhode = kravhode,
                simulering = spec,
                soekerVirkningFom = ytelser.soekerVirkningFom,
                avdoedVirkningFom = ytelser.avdoedVirkningFom,
                forrigeAlderspensjonBeregningResultatVirkningFom =
                ytelser.forrigeAlderspensjonBeregningResultat?.virkFom?.toLocalDate(),
                sakId = kravhode.sakId
            )
        )

        logger.info("Simulator steg 6 - Beregn AFP i offentlig sektor")

        val afpOffentligPre2025Result: Pre2025OffentligAfpResult? = null
        val livsvarigOffentligAfpBeregningResultat: LivsvarigOffentligAfpResult? = null
        /*ANON
        val afpOffentligPre2025Result: Pre2025OffentligAfpResult?
        val livsvarigOffentligAfpBeregningResultat: LivsvarigOffentligAfpResult?

        if (simulatorInput.gjelderAfpOffentligPre2025()) {
            afpOffentligPre2025Result = AfpBeregning(context).beregnAfpOffentlig(simulatorInput, kravhode, ytelser.forrigeAlderspensjonBeregningsresultat, grunnbelop)
            kravhode = afpOffentligPre2025Result.kravhode
            livsvarigOffentligAfpBeregningResultat = null
        } else if (gjelderEndring) {
            afpOffentligPre2025Result = simulatorInput.forsteUttakDato?.let { EndringAfpOffentligBeregning.beregnAfpOffentlig(kravhode, forsteUttakDato = it) }
            livsvarigOffentligAfpBeregningResultat = null
        } else {
            afpOffentligPre2025Result = null
            livsvarigOffentligAfpBeregningResultat =
                if (flags.inkluderAfpOffentligLivsvarig)
                    foedselsdato?.let {
                        beregnLivsvarigOffentligAfp(
                            pid = person.pid!!,
                            foedselsdato = it,
                            forventetInntekt = simulatorInput.forventetInntekt,
                            virkningsdato = simulatorInput.rettTilAfpOffentligFom ?: toLocalDate(simulatorInput.forsteUttakDato)
                        )
                    }
                else
                    null
        }
        */
        logger.info("Simulator steg 7 - Vilkårsprøv og beregn alderspensjon")

        val vilkaarsproevOgBeregnAlderspensjonResult = vilkaarsproevOgBeregnAlderspensjon(
            AlderspensjonVilkaarsproeverBeregnerSpec(
                kravhode = kravhode,
                knekkpunkter = knekkpunktMap,
                simulering = spec,
                sokerForsteVirk = ytelser.soekerVirkningFom,
                avdodForsteVirk = ytelser.avdoedVirkningFom,
                forrigeVilkarsvedtakListe = ytelser.forrigeVedtakListe,
                forrigeAlderBeregningsresultat = ytelser.forrigeAlderspensjonBeregningResultat,
                sisteBeregning = ytelser.sisteBeregning,
                afpPrivatBeregningsresultater = privatAfpBeregningResultatListe,
                gjeldendeAfpPrivatBeregningsresultat = gjeldendePrivatAfpBeregningResultat,
                forsteVirkAfpPrivat = ytelser.privatAfpVirkningFom,
                afpOffentligLivsvarigBeregningsresultat = livsvarigOffentligAfpBeregningResultat,
                isHentPensjonsbeholdninger = flags.inkluderPensjonBeholdninger,
                kravGjelder = kravhode.gjelder ?: KravGjelder.FORSTEG_BH,
                sakId = kravhode.sakId,
                sakType = kravhode.sakType,
                ignoreAvslag = flags.ignoreAvslag
            )
        )

        logger.info("Simulator steg 8 - Opprett output")

        val output: SimulatorOutput = opprettOutput(
            ResultPreparerSpec(
                simuleringSpec = spec,
                kravhode = kravhode,
                alderspensjonBeregningResultatListe = vilkaarsproevOgBeregnAlderspensjonResult.beregningsresultater,
                privatAfpBeregningResultatListe = privatAfpBeregningResultatListe,
                forrigeAlderspensjonBeregningResultat = ytelser.forrigeAlderspensjonBeregningResultat,
                forrigePrivatAfpBeregningResultat = null, // forrigeAfpPrivatBeregningsresultat is null for SimulerFleksibelAPCommand & SimulerAFPogAPCommand
                pre2025OffentligAfpBeregningResultat = afpOffentligPre2025Result?.simuleringResult,
                livsvarigOffentligAfpBeregningResultatListe = LivsvarigOffentligAfpPeriodeConverter.konverterTilArligeAfpOffentligLivsvarigPerioder(
                    livsvarigOffentligAfpBeregningResultat,
                    foedselDato?.monthValue
                ),
                grunnbeloep = grunnbeloep,
                pensjonBeholdningPeriodeListe = vilkaarsproevOgBeregnAlderspensjonResult.pensjonsbeholdningPerioder,
                outputSimulertBeregningsInformasjonForAllKnekkpunkter = flags.outputSimulertBeregningInformasjonForAllKnekkpunkter,
                sisteGyldigeOpptjeningAar = SISTE_GYLDIGE_OPPTJENING_AAR
            )
        )

        //return if (spec.erAnonym)
        //    OutputPensjonCombo(
        //        anonymSimuleringResult = AnonymOutputMapper.mapSimuleringResult(output)
        //    )
        //else
        //    OutputPensjonCombo(
        //        /*ANON
        //        pensjon = SimulatorAllPurposeResultTrimmer.trim(output, foedselsdato),
        //        */
        //        pensjon = null
        //    )
        return output
    }

    private fun fetchLoependeYtelser(
        spec: SimuleringSpec,
        soekerFoersteVirkningDatoListe: List<FoersteVirkningDato>
    ): LoependeYtelser {
        /*ANON
        if (spec.gjelderAfpOffentligPre2025()) {
            // SimulerAFPogAPCommand
            // hentLopendeYtelser sets brukersForsteVirk, avdodesForsteVirk, forrigeAlderBeregningsresultat, forrigeVilkarsvedtakListe, sisteBeregning
            // => these are null: forrigeAfpPrivatBeregningsresultat, forsteVirkAfpPrivat
            val ytelser: AfpLoependeYtelserResult = AfpLoependeYtelser(context).fetchLoependeYtelser(spec) // TODO use soekerFoersteVirkningDatoListe argument?

            return LoependeYtelser(
                sokerVirkningFom = ytelser.sokerVirkningFom!!,
                avdodVirkningFom = ytelser.avdodVirkningFom,
                afpPrivatVirkningFom = null,
                sisteBeregning = ytelser.sisteBeregning,
                forrigeAlderspensjonBeregningsresultat = ytelser.forrigeAlderspensjonBeregningsresultat,
                forrigeAfpPrivatBeregningsresultat = null,
                forrigeVilkarsvedtakListe = norskeVedtak(ytelser.forrigeVilkarsvedtakListe)
            )
        }

        if (spec.gjelderEndring()) {
            // SimulerEndringAvAPCommand
            val ytelser: EndringLoependeYtelserResult = EndringLoependeYtelser(context).fetchLoependeYtelser(spec)

            return LoependeYtelser(
                sokerVirkningFom = ytelser.alderspensjon.sokerVirkningFom!!,
                avdodVirkningFom = ytelser.alderspensjon.avdodVirkningFom,
                afpPrivatVirkningFom = ytelser.afpPrivat.virkningFom,
                sisteBeregning = ytelser.alderspensjon.sisteBeregning,
                forrigeAlderspensjonBeregningsresultat = ytelser.alderspensjon.forrigeBeregningsresultat,
                forrigeAfpPrivatBeregningsresultat = ytelser.afpPrivat.forrigeBeregningsresultat,
                forrigeVilkarsvedtakListe = norskeVedtak(ytelser.alderspensjon.forrigeVilkarsvedtakListe)
            )
        }
        */

        // Simuleringtype ALDER or ALDER_M_AFP_PRIVAT (SimulerFleksibelAPCommand)
        // hentLopendeYtelser sets brukersForsteVirk, avdodesForsteVirk, forsteVirkAfpPrivat
        // => these are null/empty: forrigeAlderBeregningsresultat, forrigeVilkarsvedtakListe, forrigeAfpPrivatBeregningsresultat, sisteBeregning
        val avdoedFoersteVirkningDatoListe: List<FoersteVirkningDato> =
            emptyList() //ANON spec.avdoed?.pid?.let(context::fetchForsteVirkningsdatoListe).orEmpty()

        val ytelser: LoependeYtelseResult = LoependeYtelseGetter.finnForsteVirkningsdatoer(
            spec,
            soekerFoersteVirkningDatoListe,
            avdoedFoersteVirkningDatoListe
        )

        return LoependeYtelser(
            soekerVirkningFom = ytelser.soekerFoersteVirkningDato!!,
            avdoedVirkningFom = ytelser.avdoedFoersteVirkningDato,
            privatAfpVirkningFom = ytelser.privatAfpFoersteVirkningDato,
            sisteBeregning = null,
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            forrigeVedtakListe = mutableListOf()
        )
    }

    private fun beregnLivsvarigOffentligAfp(
        pid: Pid,
        foedselDato: LocalDate,
        forventetInntekt: Int,
        virkningsdato: LocalDate,
    ): LivsvarigOffentligAfpResult? {
        /*ANON
        val fjorAarSomManglerOpptjeningIPopp = LocalDate.now().minusYears(1)
        val aaretOpptjeningStoppes: LocalDate = foedselDato.plusYears(OPPTJENING_TIL_AFP_OFFENTLIG_LIVSVARIG_STOPPES_VED_ALDER_AAR)

        val fremtidigInntektListe: List<FremtidigInntekt> =
            if (fjorAarSomManglerOpptjeningIPopp.isBefore(aaretOpptjeningStoppes)) {
                fjorAarSomManglerOpptjeningIPopp.datesUntil(aaretOpptjeningStoppes, Period.ofYears(1))
                    .map {
                        FremtidigInntekt(
                            belop = forventetInntekt,
                            fraOgMed = it.withMonth(1).withDayOfMonth(1)
                        )
                    }
                    .toList()
            } else emptyList()

        return context.simulerAfpOffentligLivsvarig(
            SimulerAFPOffentligLivsvarigRequest(
                fnr = pid.pid,
                foedselsdato = foedselDato,
                fremtidigeInntekter = fremtidigInntektListe,
                fom = virkningsdato
            )
        )
        */
        return null
    }

    override fun fetchFoedselDato(pid: Pid): LocalDate = generelleDataHolder.getFoedselDato(pid)

    private fun fetchGrunnbeloep(): Int {
        val grunnbeloepListe: List<SatsResultat> = context.fetchGrunnbeloepListe(LocalDate.now()).satsResultater
        return grunnbeloepListe.firstOrNull()?.verdi?.toInt() ?: 0
    }

    private fun opprettKravhode(
        spec: KravhodeSpec,
        person: PenPerson?,
        virkningDatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
    ): Kravhode {
        val start = currentTimeMillis()
        val kravhode = kravhodeCreator.opprettKravhode(spec, person, virkningDatoGrunnlagListe)
        logger.info("opprettKravhode tok {} ms", currentTimeMillis() - start)
        return kravhode
    }

    private fun oppdaterKravhodeForFoersteKnekkpunkt(spec: KravhodeUpdateSpec): Kravhode {
        val start = currentTimeMillis()
        val response = kravhodeUpdater.updateKravhodeForFoersteKnekkpunkt(spec)
        logger.info("oppdaterKravhodeForForsteKnekkpunkt tok {} ms", currentTimeMillis() - start)
        return response
    }

    private fun finnKnekkpunkter(spec: KnekkpunktSpec): SortedMap<LocalDate, MutableList<KnekkpunktAarsak>> {
        val start = currentTimeMillis()
        val response = knekkpunktFinder.finnKnekkpunkter(spec)
        logger.info("finnKnekkpunkter tok {} ms", currentTimeMillis() - start)
        return response
    }

    private fun beregnPrivatAfp(spec: PrivatAfpSpec): PrivatAfpResult {
        val start = currentTimeMillis()
        val response = privatAfpBeregner.beregnPrivatAfp(spec)
        logger.info("beregnAfpPrivat tok {} ms", currentTimeMillis() - start)
        return response
    }

    private fun vilkaarsproevOgBeregnAlderspensjon(request: AlderspensjonVilkaarsproeverBeregnerSpec): AlderspensjonBeregnerResult {
        val start = currentTimeMillis()
        val response = alderspensjonVilkaarsproeverOgBeregner.vilkaarsproevOgBeregnAlder(request)
        logger.info("vilkarsprovOgBeregnAlder tok {} ms", currentTimeMillis() - start)
        return response
    }

    private fun opprettOutput(request: ResultPreparerSpec): SimulatorOutput {
        val start = currentTimeMillis()
        val response = SimuleringResultPreparer.opprettOutput(request)
        logger.info("opprettOutput tok {} ms", currentTimeMillis() - start)
        return response
    }

    private companion object {

        // AbstraktSimulerAPFra2011Command.filterVilkarsVedtakListOnNOR
        private fun norskeVedtak(vedtakListe: List<VilkarsVedtak>): MutableList<VilkarsVedtak> =
            vedtakListe.filter { Land.NOR == it.kravlinje?.land }.toMutableList()
    }
}
